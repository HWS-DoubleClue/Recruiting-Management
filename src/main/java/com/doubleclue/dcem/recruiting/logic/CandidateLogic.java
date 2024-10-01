package com.doubleclue.dcem.recruiting.logic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.logic.CloudSafeDto;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.as.DcemUploadFile;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DepartmentEntity;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.compare.CompareUtils;
import com.doubleclue.dcem.recruiting.entities.CandidateEntity;
import com.doubleclue.dcem.recruiting.entities.CandidateEntity_;
import com.doubleclue.dcem.recruiting.entities.FeedbackEntity;
import com.doubleclue.dcem.recruiting.entities.HistoryEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity_;
import com.doubleclue.dcem.recruiting.entities.enums.EntityType;
import com.doubleclue.dcem.recruiting.entities.enums.Operation;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity_;
import com.doubleclue.dcem.skills.logic.SkillsLevelLogic;
import com.doubleclue.dcem.skills.logic.SkillsMergeDTO;

@ApplicationScoped
@Named("candidateLogic")
public class CandidateLogic {

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private EntityManager em;

	@Inject
	private CloudSafeLogic cloudSafeLogic;

	@Inject
	private HistoryLogic historyLogic;

	@Inject
	private SkillsLevelLogic skillsLevelLogic;

	@Inject
	private VacancyLogic vacancyLogic;

	@Inject
	private UserLogic userLogic;

	@DcemTransactional
	public void addOrUpdateCandidateWithFiles(CandidateEntity candidate, DcemAction dcemAction, List<DcemUploadFile> uploadedFiles,
			List<CloudSafeEntity> deletedFiles) throws Exception {
		candidate.setModifiedBy(operatorSessionBean.getDcemUser());
		candidate.setLastModifiedOn(LocalDateTime.now());
		List<CloudSafeEntity> oldFiles = new ArrayList<CloudSafeEntity>();
		HistoryEntity history = new HistoryEntity(candidate.getModifiedBy(), candidate.getLastModifiedOn(), EntityType.Candidate);
		if (candidate.getSkills() != null && candidate.getSkills().isEmpty() == false) {
			SortedSet<SkillsLevelEntity> skills = skillsLevelLogic.updateSkillsLevelEntities(candidate.getSkills());
			candidate.setSkills(skills);
		}
		SortedSet<VacancyEntity> updatedVacanciesForCompare = new TreeSet<VacancyEntity>();
		SortedSet<VacancyEntity> vacanciesForTotalCountUpdate = new TreeSet<VacancyEntity>();
		if (candidate.getVacancies() != null && candidate.getVacancies().isEmpty() == false) {
			for (VacancyEntity vacancy : candidate.getVacancies()) {
				updatedVacanciesForCompare.add(vacancyLogic.getVacancyById(vacancy.getId()));
			}
		}
		candidate.setVacancies(updatedVacanciesForCompare);
		if (candidate.getId() == null) {
			candidate.setRegisteredOn(LocalDate.now());
			em.persist(candidate);
			history.setOperation(Operation.Create);
			history.setInformation(CompareUtils.compareObjects(null, candidate));
		} else {
			CandidateEntity oldCandidate = em.find(candidate.getClass(), candidate.getId());
			history.setOperation(Operation.Edit);
			history.setInformation(CompareUtils.compareObjects(oldCandidate, candidate));
			oldFiles = oldCandidate.getFiles().stream().collect(Collectors.toList());
			vacanciesForTotalCountUpdate.addAll(oldCandidate.getVacancies());
		
			em.detach(oldCandidate);
			em.merge(candidate);
			em.flush();
			if (deletedFiles != null && deletedFiles.isEmpty() == false) {
				List<CloudSafeDto> deletedDbFiles = cloudSafeLogic.deleteCloudSafeFiles(deletedFiles, null, false);
				cloudSafeLogic.deleteCloudSafeFilesContent(deletedDbFiles);
			}
		}
		CandidateEntity candidateWithFiles = updateCandidateFiles(candidate, uploadedFiles, dcemAction);
		history.setInformation(history.getInformation().concat(RecruitingUtils.compareFiles(oldFiles, candidateWithFiles.getFiles())));
		em.merge(candidateWithFiles);
		history.setReference(candidate.getId().intValue());
		historyLogic.addHistory(history);
		vacanciesForTotalCountUpdate.addAll(candidate.getVacancies());
		vacancyLogic.updateTotalCandidates(vacanciesForTotalCountUpdate);
	}

	private CandidateEntity updateCandidateFiles(CandidateEntity candidate, List<DcemUploadFile> uploadedFiles, DcemAction dcemAction) throws Exception {
		if (Objects.isNull(uploadedFiles) || uploadedFiles.isEmpty()) {
			return candidate;
		}
		uploadedFiles.forEach(file -> {
			file.fileName = RecruitingUtils.createFileNameForDB(file.fileName, candidate, EntityType.Candidate);
		});
		DcemUser dcemUser = userLogic.getUser(RecruitingConstants.RECRUITING_SERVICE_OPERATOR);
		List<CloudSafeEntity> savedFiles = cloudSafeLogic.saveMultipleFiles(uploadedFiles, dcemUser, null, null, false, false, null, null, null,
				CloudSafeOwner.USER);
		if (Objects.isNull(candidate.getFiles()) == true) {
			candidate.setFiles(savedFiles);
		} else {
			candidate.getFiles().addAll(savedFiles);
		}
		return candidate;
	}

	@DcemTransactional
	public void updateCandidateFeedback(FeedbackEntity feedback) throws Exception {
		HistoryEntity history = new HistoryEntity(feedback.getUser(), feedback.getPostedOn(), EntityType.Feedback);
		if (feedback.getId() == null) {
			em.persist(feedback);
			history.setOperation(Operation.Create);
			history.setInformation(CompareUtils.compareObjects(null, feedback));
		} else {
			FeedbackEntity oldFeedback = em.find(FeedbackEntity.class, feedback.getId());
			history.setOperation(Operation.Edit);
			history.setInformation(CompareUtils.compareObjects(oldFeedback, feedback));
			em.detach(oldFeedback);
			em.merge(feedback);
		}
		updateCandidateRating(feedback.getCandidate());
		history.setReference(feedback.getId().intValue());
		historyLogic.addHistory(history);
	}

	@DcemTransactional
	public void deleteCandidateFeedback(FeedbackEntity feedback) throws Exception {
		CandidateEntity candidateToUpdate = feedback.getCandidate();
		em.remove(em.contains(feedback) ? feedback : em.find(FeedbackEntity.class, feedback.getId()));
		updateCandidateRating(candidateToUpdate);
	}

	@DcemTransactional
	private void updateCandidateRating(CandidateEntity candidateEntity) {
		TypedQuery<FeedbackEntity> query = em.createNamedQuery(FeedbackEntity.GET_FEEDBACK_OF_CANDIDATE, FeedbackEntity.class);
		query.setParameter(1, candidateEntity);
		List<FeedbackEntity> candidateFeedbacks = query.getResultList();

		float newRating = candidateFeedbacks.size() == 0 ? 0
				: (float) candidateFeedbacks.stream().mapToInt(r -> r.getRating()).sum() / candidateFeedbacks.size();
		BigDecimal round = new BigDecimal(newRating).setScale(1, RoundingMode.CEILING);

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaUpdate<CandidateEntity> updateCriteria = cb.createCriteriaUpdate(CandidateEntity.class);
		Root<CandidateEntity> root = updateCriteria.from(CandidateEntity.class);
		updateCriteria.set(root.get(CandidateEntity_.ranking), round.floatValue());
		updateCriteria.where(cb.equal(root.get("id"), candidateEntity.getId()));
		em.createQuery(updateCriteria).executeUpdate();
	}

	public CandidateEntity getFullCandidate(CandidateEntity candidateOrg) throws Exception {
		CandidateEntity candidateEntity = em.find(CandidateEntity.class, candidateOrg.getId());
		CandidateEntity candidateWithFiles = getCandidateWithLazyProperty(candidateEntity, CandidateEntity.GRAPH_FILES);
		CandidateEntity candidateWithFeedback = getCandidateWithLazyProperty(candidateEntity, CandidateEntity.GRAPH_FEEDBACK);
		CandidateEntity candidateWithVacancies = getCandidateWithLazyProperty(candidateEntity, CandidateEntity.GRAPH_VACANCIES);
		CandidateEntity candidateWithWatchers = getCandidateWithLazyProperty(candidateEntity, CandidateEntity.GRAPH_WATCHERS);
		CandidateEntity candidateWithSkills = getCandidateWithLazyProperty(candidateEntity, CandidateEntity.GRAPH_SKILLS);

		for (CloudSafeEntity file : candidateWithFiles.getFiles()) { // for Lazy initialization
			file.getName();
		}
		candidateEntity.setFiles(candidateWithFiles.getFiles());

		for (FeedbackEntity feedback : candidateWithFeedback.getFeedback()) { // for Lazy initialization
			feedback.getId();
		}
		candidateEntity.setFeedback(candidateWithFeedback.getFeedback());

		for (VacancyEntity vacancy : candidateWithVacancies.getVacancies()) { // for Lazy initialization
			vacancy.getDefaultTitle();
		}
		candidateEntity.setVacancies(candidateWithVacancies.getVacancies());

		for (DcemUser dcemUser : candidateWithWatchers.getWatchers()) { // for Lazy initialization
			dcemUser.getDepartmentName();
		}
		candidateEntity.setWatchers(candidateWithWatchers.getWatchers());

		for (SkillsLevelEntity skillLevel : candidateWithSkills.getSkills()) {
			skillLevel.getSkill().getName();
		}
		candidateEntity.setSkills(candidateWithSkills.getSkills());

		return candidateEntity;
	}

	public void detacheCandidate(CandidateEntity candidate) {
		em.detach(candidate);
	}

	public CandidateEntity getCandidateWithLazyProperty(CandidateEntity candidate, String entityGraphName) {
		EntityGraph<?> entityGraph = em.getEntityGraph(entityGraphName);
		Map<String, Object> properties = new HashMap<>();
		properties.put("javax.persistence.fetchgraph", entityGraph);
		return em.find(CandidateEntity.class, candidate.getId(), properties);
	}

	public CandidateEntity getCandidate(int id) {
		return em.find(CandidateEntity.class, id);
	}

	private List<CandidateEntity> getCandidatesBySkillsLevels(List<SkillsLevelEntity> skillLevelEntities) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CandidateEntity> query = cb.createQuery(CandidateEntity.class);
		Root<CandidateEntity> root = query.from(CandidateEntity.class);
		Join<CandidateEntity, SkillsLevelEntity> candidateSkills = root.join(CandidateEntity_.skills, JoinType.LEFT);
		Predicate skillsPredicate = candidateSkills.in(skillLevelEntities);
		root.fetch(CandidateEntity_.skills, JoinType.LEFT);
		query.select(root).where(skillsPredicate).distinct(true);
		return em.createQuery(query).getResultList();
	}

	@DcemTransactional
	public void deleteSkillsFromCandidates(List<SkillsLevelEntity> skillsLevelEntities) throws Exception {
		List<CandidateEntity> candidateEntities = getCandidatesBySkillsLevels(skillsLevelEntities);
		for (CandidateEntity candidateEntity : candidateEntities) {
			candidateEntity.getSkills().removeAll(skillsLevelEntities);
			em.merge(candidateEntity);
		}
	}

	private List<CandidateEntity> getCandidiatesBySkill(SkillsEntity mergingSkill) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CandidateEntity> query = cb.createQuery(CandidateEntity.class);
		Root<CandidateEntity> root = query.from(CandidateEntity.class);
		Join<CandidateEntity, SkillsLevelEntity> candidateSkills = root.join(CandidateEntity_.skills, JoinType.LEFT);
		Predicate skillsPredicate = cb.equal(candidateSkills.get(SkillsLevelEntity_.skill), mergingSkill);
		root.fetch(CandidateEntity_.skills, JoinType.LEFT);
		query.select(root).where(skillsPredicate).distinct(true);
		return em.createQuery(query).getResultList();
	}

	@DcemTransactional
	public void mergeSkillsInCandidates(SkillsMergeDTO skillsMergeDTO) throws Exception {
		List<CandidateEntity> candidateEntities = getCandidiatesBySkill(skillsMergeDTO.getMergingSkill());
		for (CandidateEntity candidateEntity : candidateEntities) {
			skillsLevelLogic.mergeSkillInSkillLevelEntities(skillsMergeDTO.getMergingSkill(), skillsMergeDTO.getMainSkill(), candidateEntity.getSkills());
			em.merge(candidateEntity);
		}
	}

	@DcemTransactional
	public void deleteCandidates(List<CandidateEntity> candidates) throws Exception {
		SortedSet<VacancyEntity> vacanciesForTotalCountUpdate = new TreeSet<VacancyEntity>();
		for (CandidateEntity candidate : candidates) {
			HistoryEntity history = new HistoryEntity(operatorSessionBean.getDcemUser(), LocalDateTime.now(), EntityType.Candidate);
			history.setOperation(Operation.Delete);
			history.setReference(candidate.getId().intValue());
			history.setInformation(RecruitingConstants.DELETED_HISTORY);
			candidate = em.merge(candidate);
			em.remove(candidate);
			historyLogic.addHistory(history);
			vacanciesForTotalCountUpdate.addAll(candidate.getVacancies());
		}
		vacancyLogic.updateTotalCandidates(vacanciesForTotalCountUpdate);
	}

	@DcemTransactional
	public void deleteDcemUserFromCandidates(DcemUser dcemUser) throws Exception {
		TypedQuery<CandidateEntity> query = em.createNamedQuery(CandidateEntity.GET_ALL_CANDIDATES_WITH_DCEMUSER_REFERENCES, CandidateEntity.class);
		query.setParameter(1, dcemUser);
		List<CandidateEntity> candidates = query.getResultList();
		for (CandidateEntity candidate : candidates) {
			if (Objects.equals(candidate.getRecommendedBy(), dcemUser)) {
				candidate.setRecommendedBy(null);
			}
			if (Objects.equals(candidate.getModifiedBy(), dcemUser)) {
				candidate.setModifiedBy(null);
			}
			if (candidate.getWatchers().contains(dcemUser)) {
				candidate.getWatchers().remove(dcemUser);
			}
			em.merge(candidate);
		}
	}

	@DcemTransactional
	public void deleteDcemUserFeedbacks(DcemUser dcemUser) throws Exception {
		TypedQuery<FeedbackEntity> query = em.createNamedQuery(FeedbackEntity.GET_FEEDBACKS_BY_DCEMUSER, FeedbackEntity.class);
		query.setParameter(1, dcemUser);
		List<FeedbackEntity> feedbacks = query.getResultList();
		for (FeedbackEntity feedback : feedbacks) {
			em.remove(feedback);
		}
	}

	public List<DepartmentEntity> getDepartmentsAppliedFor(CandidateEntity candidateEntity) {
	    CriteriaBuilder cb = em.getCriteriaBuilder();
	    CriteriaQuery<DepartmentEntity> query = cb.createQuery(DepartmentEntity.class);
	    
	    Root<CandidateEntity> candidate = query.from(CandidateEntity.class);
	    Join<CandidateEntity, VacancyEntity> vacancy = candidate.join(CandidateEntity_.vacancies);
	    
	    query.select(vacancy.get(VacancyEntity_.DEPARTMENT))
	         .where(cb.equal(candidate, candidateEntity))
	         .distinct(true);
	    
	    return em.createQuery(query).getResultList();
	}

}