package com.doubleclue.dcem.recruiting.logic;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;

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

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.BranchLocation;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.utils.compare.CompareUtils;
import com.doubleclue.dcem.recruiting.entities.CandidateEntity;
import com.doubleclue.dcem.recruiting.entities.CareerLevelEntity;
import com.doubleclue.dcem.recruiting.entities.HistoryEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity_;
import com.doubleclue.dcem.recruiting.entities.VacancyLanguageEntity;
import com.doubleclue.dcem.recruiting.entities.enums.EntityType;
import com.doubleclue.dcem.recruiting.entities.enums.Operation;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity_;
import com.doubleclue.dcem.skills.logic.SkillsLevelLogic;
import com.doubleclue.dcem.skills.logic.SkillsMergeDTO;

@ApplicationScoped
@Named("vacancyLogic")
public class VacancyLogic {

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private EntityManager em;

	@Inject
	private HistoryLogic historyLogic;

	@Inject
	private RecruitingEmailLogic recruitingEmailLogic;

	@Inject
	private SkillsLevelLogic skillsLevelLogic;

	public VacancyEntity getVacancyById(int id) throws Exception {
		return em.find(VacancyEntity.class, id);
	}

	@DcemTransactional
	public VacancyEntity getFullVacancyById(VacancyEntity vacancy) throws Exception {
		return getFullVacancy(getVacancyById(vacancy.getId()));
	}

	@DcemTransactional
	public VacancyEntity getFullVacancy(VacancyEntity vacancy) throws Exception {
		VacancyEntity vacancyWithSkills = getVacancyWithLazyProperty(vacancy, VacancyEntity.GRAPH_SKILLS);
		VacancyEntity vacancyWithWatchers = getVacancyWithLazyProperty(vacancy, VacancyEntity.GRAPH_WATCHERS);
		VacancyEntity vacancyWithCandidates = getVacancyWithLazyProperty(vacancy, VacancyEntity.GRAPH_CANDIDATES);
		VacancyEntity vacancyWithLanguages = getVacancyWithLazyProperty(vacancy, VacancyEntity.GRAPH_LANGUAGES);
		VacancyEntity vacancyWithCareerLevel = getVacancyWithLazyProperty(vacancy, VacancyEntity.GRAPH_CAREER_LEVEL);
		VacancyEntity vacancyWithBranchLocation = getVacancyWithLazyProperty(vacancy, VacancyEntity.GRAPH_BRANCH_LOCATIONS);

		for (SkillsLevelEntity skillLevel : vacancyWithSkills.getSkills()) {
			skillLevel.getSkill().getName();
		}
		vacancy.setSkills(vacancyWithSkills.getSkills());

		for (DcemUser dcemUser : vacancyWithWatchers.getWatchers()) {
			dcemUser.getDepartmentName();
		}
		vacancy.setWatchers(vacancyWithWatchers.getWatchers());

		for (CandidateEntity candidate : vacancyWithCandidates.getCandidates()) {
			candidate.getFirstName();
		}
		vacancy.setCandidates(vacancyWithCandidates.getCandidates());

		for (VacancyLanguageEntity lang : vacancyWithLanguages.getLanguages()) {
			lang.getLanguage();
		}
		vacancy.setLanguages(vacancyWithLanguages.getLanguages());

		for (CareerLevelEntity careerLevel : vacancyWithCareerLevel.getCareerLevels()) {
			careerLevel.getCareerLevel();
		}
		vacancy.setCareerLevels(vacancyWithCareerLevel.getCareerLevels());

		for (BranchLocation branchLocation : vacancyWithBranchLocation.getBranchLocations()) {
			branchLocation.getCountry();
		}
		vacancy.setBranchLocations(vacancyWithBranchLocation.getBranchLocations());
		return vacancy;
	}

	private VacancyEntity getVacancyWithLazyProperty(VacancyEntity vacancy, String graphName) {
		EntityGraph<?> entityGraph = em.getEntityGraph(graphName);
		Map<String, Object> properties = new HashMap<>();
		properties.put("javax.persistence.fetchgraph", entityGraph);
		return em.find(VacancyEntity.class, vacancy.getId(), properties);
	}

	@DcemTransactional
	public void addOrUpdateVacancy(VacancyEntity vacancyEntity, DcemAction dcemAction) throws Exception {
		vacancyEntity.setModifiedBy(operatorSessionBean.getDcemUser());
		vacancyEntity.setLastModifiedOn(LocalDateTime.now());

		HistoryEntity history = new HistoryEntity(vacancyEntity.getModifiedBy(), vacancyEntity.getLastModifiedOn(), EntityType.Vacancy);

		SortedSet<SkillsLevelEntity> skills = skillsLevelLogic.updateSkillsLevelEntities(vacancyEntity.getSkills());
		vacancyEntity.setSkills(skills);

		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD) || dcemAction.getAction().equals(DcemConstants.ACTION_COPY)) {
			history.setOperation(Operation.Create);
			em.persist(vacancyEntity);
			history.setInformation(CompareUtils.compareObjects(null, vacancyEntity));
		} else {
			VacancyEntity oldVacancy = em.find(vacancyEntity.getClass(), vacancyEntity.getId());
			history.setOperation(Operation.Edit);
			history.setInformation(CompareUtils.compareObjects(oldVacancy, vacancyEntity));
			em.detach(oldVacancy);
			vacancyEntity = em.merge(vacancyEntity);
		}
		history.setReference(vacancyEntity.getId().intValue());
		if (history.getInformation() != null && history.getInformation().isBlank() == false) {
			historyLogic.addHistory(history);
			recruitingEmailLogic.notifyWatcher(vacancyEntity.getWatchers(), vacancyEntity);
		}
	}

	@DcemTransactional
	public void updateTotalCandidates(SortedSet<VacancyEntity> vacancylist) {
		for (VacancyEntity vacancyEntity : vacancylist) {
			TypedQuery<Integer> query = em.createNamedQuery(VacancyEntity.GET_TOTAL_CANDIDATES, Integer.class);
			query.setParameter(1, vacancyEntity.getId());
			int newTotal = query.getSingleResult();

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaUpdate<VacancyEntity> updateCriteria = cb.createCriteriaUpdate(VacancyEntity.class);
			Root<VacancyEntity> root = updateCriteria.from(VacancyEntity.class);
			updateCriteria.set(root.get(VacancyEntity_.totalCandidates), newTotal);
			updateCriteria.where(cb.equal(root.get(VacancyEntity_.ID), vacancyEntity.getId()));
			em.createQuery(updateCriteria).executeUpdate();
		}
	}
	
	@DcemTransactional
	public void toggleVacancyArchive(List<VacancyEntity> vacancyList, boolean archived) {
			List<Integer> vacancyIds = vacancyList.stream().map(v -> v.getId()).toList();
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaUpdate<VacancyEntity> updateCriteria = cb.createCriteriaUpdate(VacancyEntity.class);
			Root<VacancyEntity> root = updateCriteria.from(VacancyEntity.class);
			updateCriteria.set(root.get(VacancyEntity_.ARCHIVED), archived);
			updateCriteria.where(root.get(VacancyEntity_.ID).in(vacancyIds));
			em.createQuery(updateCriteria).executeUpdate();
	}

	public List<VacancyEntity> getTitleAndIdOfAllActiveVacancies() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<VacancyEntity> query = cb.createQuery(VacancyEntity.class);
		Root<VacancyEntity> root = query.from(VacancyEntity.class);
		query.select(cb.construct(VacancyEntity.class, root.get(VacancyEntity_.id), root.get(VacancyEntity_.defaultTitle)));
		query.where(cb.isFalse(root.get(VacancyEntity_.archived)));
		query.orderBy(cb.asc(root.get(VacancyEntity_.defaultTitle)));
		return em.createQuery(query).getResultList();
	}

	public void detachVacancy(VacancyEntity vacancyEntity) {
		em.detach(vacancyEntity);
	}

	public void detachVacancies(List<VacancyEntity> list) {
		for (VacancyEntity vacancyEntity : list) {
			em.detach(vacancyEntity);
		}
		return;
	}

	public void detachVacancyLanguage(VacancyLanguageEntity vacancyLanguageEntity) {
		em.detach(vacancyLanguageEntity);
	}

	private List<VacancyEntity> getVacanciesBySkillsLevels(List<SkillsLevelEntity> skillLevelEntities) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<VacancyEntity> query = cb.createQuery(VacancyEntity.class);
		Root<VacancyEntity> root = query.from(VacancyEntity.class);
		Join<VacancyEntity, SkillsLevelEntity> skillsLevel = root.join(VacancyEntity_.skills, JoinType.LEFT);
		Predicate skillsLevelPredicate = skillsLevel.in(skillLevelEntities);
		root.fetch(VacancyEntity_.skills, JoinType.LEFT);
		query.select(root).where(skillsLevelPredicate).distinct(true);
		return em.createQuery(query).getResultList();
	}

	@DcemTransactional
	public void deleteSkillsFromVancancies(List<SkillsLevelEntity> skillsLevelEntities) throws Exception {
		List<VacancyEntity> vacancyEntities = getVacanciesBySkillsLevels(skillsLevelEntities);
		for (VacancyEntity vacancyEntity : vacancyEntities) {
			vacancyEntity.getSkills().removeAll(skillsLevelEntities);
			em.merge(vacancyEntity);
		}
	}

	private List<VacancyEntity> getVacanciesBySkill(SkillsEntity skillsEntity) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<VacancyEntity> query = cb.createQuery(VacancyEntity.class);
		Root<VacancyEntity> root = query.from(VacancyEntity.class);
		Join<VacancyEntity, SkillsLevelEntity> skillsLevel = root.join(VacancyEntity_.skills, JoinType.LEFT);
		Predicate skillsLevelPredicate = cb.equal(skillsLevel.get(SkillsLevelEntity_.skill), skillsEntity);
		root.fetch(VacancyEntity_.skills, JoinType.LEFT);
		query.select(root).where(skillsLevelPredicate).distinct(true);
		return em.createQuery(query).getResultList();
	}

	@DcemTransactional
	public void mergeSkillsInVancancies(SkillsMergeDTO skillsMergeDTO) throws Exception {
		List<VacancyEntity> vacancyEntities = getVacanciesBySkill(skillsMergeDTO.getMergingSkill());
		for (VacancyEntity vacancyEntity : vacancyEntities) {
			skillsLevelLogic.mergeSkillInSkillLevelEntities(skillsMergeDTO.getMergingSkill(), skillsMergeDTO.getMainSkill(), vacancyEntity.getSkills());
			em.merge(vacancyEntity);
		}
	}

	public void deleteBranchLocations(List<BranchLocation> branchLocations) {
		List<VacancyEntity> vacancyEntities = getVacanciesByBranchLocations(branchLocations);
		for (VacancyEntity vacancyEntity : vacancyEntities) {
			vacancyEntity.getBranchLocations().removeAll(branchLocations);
			em.merge(vacancyEntity);
		}
	}

	private List<VacancyEntity> getVacanciesByBranchLocations(List<BranchLocation> branchLocations) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<VacancyEntity> query = cb.createQuery(VacancyEntity.class);
		Root<VacancyEntity> root = query.from(VacancyEntity.class);
		Join<VacancyEntity, BranchLocation> branchJoin = root.join(VacancyEntity_.branchLocations, JoinType.INNER);
		Predicate predicate = branchJoin.in(branchLocations);
		root.fetch(VacancyEntity_.branchLocations, JoinType.INNER);
		query.select(root).where(predicate).distinct(true);
		return em.createQuery(query).getResultList();
	}

	@DcemTransactional
	public void deleteDcemUserVacancyReferences(DcemUser dcemUser) {
		TypedQuery<VacancyEntity> query = em.createNamedQuery(VacancyEntity.GET_VACANCIES_WITH_DCEMUSER_REFERENCES, VacancyEntity.class);
		query.setParameter(1, dcemUser);
		List<VacancyEntity> vacancies = query.getResultList();
		for (VacancyEntity vacancy : vacancies) {
			if (Objects.equals(vacancy.getDecisionMaker(), dcemUser)) {
				vacancy.setDecisionMaker(null);
			}
			if (Objects.equals(vacancy.getClerk(), dcemUser)) {
				vacancy.setClerk(null);
			}
			if (Objects.equals(vacancy.getModifiedBy(), dcemUser)) {
				vacancy.setModifiedBy(null);
			}
			if (vacancy.getWatchers().contains(dcemUser)) {
				vacancy.getWatchers().remove(dcemUser);
			}
			em.merge(vacancy);
		}
	}

	public void deleteVacancies(List<VacancyEntity> vacancies) throws Exception {
		for (VacancyEntity vacancy : vacancies) {
			HistoryEntity history = new HistoryEntity(operatorSessionBean.getDcemUser(), LocalDateTime.now(), EntityType.Vacancy);
			history.setOperation(Operation.Delete);
			history.setReference(vacancy.getId().intValue());
			history.setInformation(RecruitingConstants.DELETED_HISTORY);
			vacancy = em.merge(vacancy);
			em.remove(vacancy);
			historyLogic.addHistory(history);
		}
	}
}