package com.doubleclue.dcem.recruiting.logic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.logic.CloudSafeDto;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.as.DcemUploadFile;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.compare.CompareUtils;
import com.doubleclue.dcem.recruiting.entities.HistoryEntity;
import com.doubleclue.dcem.recruiting.entities.RecruiterEntity;
import com.doubleclue.dcem.recruiting.entities.enums.EntityType;
import com.doubleclue.dcem.recruiting.entities.enums.Operation;

@ApplicationScoped
@Named("recruiterLogic")
public class RecruiterLogic {

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private EntityManager em;

	@Inject
	private CloudSafeLogic cloudSafeLogic;

	@Inject
	private HistoryLogic historyLogic;

	@Inject
	private UserLogic userLogic;

	@DcemTransactional
	public void addOrUpdateRecruiterWithFiles(RecruiterEntity recruiter, DcemAction dcemAction, List<DcemUploadFile> uploadedFiles,
			List<CloudSafeEntity> deletedFiles) throws Exception {
		recruiter.setModifiedBy(operatorSessionBean.getDcemUser());
		recruiter.setLastModifiedOn(LocalDateTime.now());
		List<CloudSafeEntity> oldFiles = new ArrayList<CloudSafeEntity>();

		HistoryEntity history = new HistoryEntity(recruiter.getModifiedBy(), recruiter.getLastModifiedOn(), EntityType.Recruiter);

		if (recruiter.getId() == null) {
			em.persist(recruiter);
			history.setOperation(Operation.Create);
			history.setInformation(CompareUtils.compareObjects(null, recruiter));
		} else {
			RecruiterEntity oldRecruiter = em.find(recruiter.getClass(), recruiter.getId());
			history.setOperation(Operation.Edit);
			history.setInformation(CompareUtils.compareObjects(oldRecruiter, recruiter));
			oldFiles = oldRecruiter.getFiles().stream().collect(Collectors.toList());
			em.detach(oldRecruiter);
			em.merge(recruiter);
			em.flush();
			if (deletedFiles != null && deletedFiles.isEmpty() == false) {
				List<CloudSafeDto> deletedDbFiles = cloudSafeLogic.deleteCloudSafeFiles(deletedFiles, null, false);
				cloudSafeLogic.deleteCloudSafeFilesContent(deletedDbFiles);
			}
		}
		RecruiterEntity recruiterWithFiles = updateRecruiterFiles(recruiter, uploadedFiles, dcemAction);
		history.setInformation(history.getInformation().concat(RecruitingUtils.compareFiles(oldFiles, recruiterWithFiles.getFiles())));
		em.merge(recruiterWithFiles);
		history.setReference(recruiter.getId().intValue());
		historyLogic.addHistory(history);
	}

	private RecruiterEntity updateRecruiterFiles(RecruiterEntity recruiter, List<DcemUploadFile> uploadedFiles, DcemAction dcemAction) throws Exception {
		if (Objects.isNull(uploadedFiles) || uploadedFiles.isEmpty()) {
			return recruiter;
		}
		uploadedFiles.forEach(file -> {
			file.fileName = RecruitingUtils.createFileNameForDB(file.fileName, recruiter, EntityType.Recruiter);
		});
		DcemUser dcemUser = userLogic.getUser(RecruitingConstants.RECRUITING_SERVICE_OPERATOR);
		List<CloudSafeEntity> savedFiles = cloudSafeLogic.saveMultipleFiles(uploadedFiles, dcemUser, null, null, false, false, null, null, null,
				CloudSafeOwner.USER);
		if(Objects.isNull(recruiter.getFiles()) == true){

			recruiter.setFiles(savedFiles);
		} else {
			recruiter.getFiles().addAll(savedFiles);
		}
		return recruiter;
	}

	public RecruiterEntity getFullRecruiter(RecruiterEntity recruiter) throws Exception {
		RecruiterEntity recruiterEntity = em.find(RecruiterEntity.class, recruiter.getId());
		RecruiterEntity recruiterWithFiles = getRecruiterWithLazyProperty(recruiterEntity, RecruiterEntity.GRAPH_FILES);
		for (CloudSafeEntity file : recruiterWithFiles.getFiles()) { // for Lazy initialization
			file.getName();
		}
		recruiterEntity.setFiles(recruiterWithFiles.getFiles());
		return recruiterEntity;
	}

	public List<RecruiterEntity> getAllRecruiters() {
		TypedQuery<RecruiterEntity> query = em.createNamedQuery(RecruiterEntity.GET_ALL, RecruiterEntity.class);
		return query.getResultList();
	}

	public void detachRecruiter(RecruiterEntity recruiter) {
		em.detach(recruiter);
	}

	private RecruiterEntity getRecruiterWithLazyProperty(RecruiterEntity recruiter, String entityGraphName) {
		EntityGraph<?> entityGraph = em.getEntityGraph(entityGraphName);
		Map<String, Object> properties = new HashMap<>();
		properties.put("javax.persistence.fetchgraph", entityGraph);
		return em.find(RecruiterEntity.class, recruiter.getId(), properties);
	}

	@DcemTransactional
	public void deleteDcemUserFromRecruiter(DcemUser dcemUser) throws Exception {
		List<RecruiterEntity> recruiters = em.createNamedQuery(RecruiterEntity.GET_RECRUITERS_BY_DCEMUSER, RecruiterEntity.class).setParameter(1, dcemUser)
				.getResultList();
		for (RecruiterEntity recruiter : recruiters) {
			recruiter.setModifiedBy(null);
			em.merge(recruiter);
		}
	}

	@DcemTransactional
	public void deleteRecruiter(List<RecruiterEntity> recruiters) throws Exception {
		for (RecruiterEntity recruiter : recruiters) {
			HistoryEntity history = new HistoryEntity(operatorSessionBean.getDcemUser(), LocalDateTime.now(), EntityType.Recruiter);
			history.setOperation(Operation.Delete);
			history.setReference(recruiter.getId().intValue());
			history.setInformation(RecruitingConstants.DELETED_HISTORY);
			recruiter = em.merge(recruiter);
			em.remove(recruiter);
			historyLogic.addHistory(history);
		}
	}
}
