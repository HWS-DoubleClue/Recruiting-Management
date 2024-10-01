package com.doubleclue.dcem.recruiting.restapi.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.restapi.impl.DeviceApiServiceImpl;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.jpa.JpaSelectProducer;
import com.doubleclue.dcem.core.logic.BranchLocationLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.recruiting.entities.CandidateEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyLanguageEntity;
import com.doubleclue.dcem.recruiting.logic.CandidateLogic;
import com.doubleclue.dcem.recruiting.logic.VacancyLogic;
import com.doubleclue.dcem.recruiting.restapi.model.ApiRecruitingFile;
import com.doubleclue.dcem.recruiting.restapi.model.MixInSkills;
import com.doubleclue.dcem.recruiting.subjects.CandidateSubject;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ApplicationScoped
public class ApiRecruitingImpl {

	private static Logger logger = LogManager.getLogger(DeviceApiServiceImpl.class);

	// static DcemAction dcemActionView = new DcemAction(RecruitingModule.MODULE_ID, VacancySubject.SUBJECT_NAME, DcemConstants.ACTION_VIEW);
	// static DcemAction dcemActionAPI = new DcemAction(RecruitingModule.MODULE_ID, VacancySubject.SUBJECT_NAME, DcemConstants.ACTION_REST_API);
	static final String NO_ACCES_RIGHTS = "No Access rights for this method";

	@Inject
	EntityManager entityManager;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	BranchLocationLogic branchLocationLogic;

	@Inject
	VacancyLogic vacancyLogic;

	@Inject
	CandidateLogic candidateLogic;
	
	@Inject 
	CandidateSubject candidateSubject;

	ObjectMapper objectMapper;

	@PostConstruct
	public void init() {
		objectMapper = new ObjectMapper();
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.addMixIn(SkillsEntity.class, MixInSkills.class);
	}

	public Response getVacancies(SupportedLanguage language, List<ApiFilterItem> filters, Integer offset, Integer maxResults) {
		List<EntityInterface> entities = getEntities(filters, offset, maxResults, VacancyEntity.class);
		List<VacancyEntity> vacancies = castVacancyEntityList(entities);
		if (language != null) {
			for (VacancyEntity vacancyEntity : vacancies) {
				if (updateVacancyLanguage(vacancyEntity, language) == false) {
					continue;
				}
			}
		}
		try {
			return Response.ok().entity(objectMapper.writeValueAsString(entities)).build();
		} catch (JsonProcessingException e) {
			logger.error("REST-API getVacancies - " + e.toString());
			return Response.serverError().entity(e.toString()).build();
		}
	}

	public Response getVacancy(int vacancyId, SupportedLanguage language) throws WebApplicationException {
		VacancyEntity vacancyEntity;
		try {
			vacancyEntity = vacancyLogic.getVacancyById(vacancyId);
			if (updateVacancyLanguage(vacancyEntity, language) == false) {
				return Response.noContent().build();
			}
			return Response.ok().entity(vacancyEntity).build();
		} catch (Exception e) {
			return Response.serverError().build();
		}
	}

	private boolean updateVacancyLanguage(VacancyEntity vacancyEntity, SupportedLanguage language) {
		if (language != null && vacancyEntity.getDefaultLanguage() != language) {
			VacancyLanguageEntity languageEntity = vacancyEntity.getLanguage(language);
			if (languageEntity == null) {
				return false;
			}
			vacancyLogic.detachVacancy(vacancyEntity);
			vacancyEntity.setDefaultTitle(languageEntity.getTitle());
			vacancyEntity.setDefaultDescription(languageEntity.getDescription());
			vacancyEntity.setLanguages(null);
		}
		return true;
	}

	public Response getCandidates(List<ApiFilterItem> filters, int offset, int maxResults) {
		List<EntityInterface> list = getEntities(filters, offset, maxResults, CandidateEntity.class);
		try {
			String text = objectMapper.writeValueAsString(list);
			return Response.ok().entity(text).build();
		} catch (JsonProcessingException e) {
			logger.error("REST-API getVacancies - " + e.toString());
			return Response.serverError().build();
		}
	}

	public Response addCandidate(CandidateEntity  candidateEntity) {
		DcemAction dcemAction = new DcemAction(candidateSubject, DcemConstants.ACTION_ADD);
		try {
			candidateEntity.setId(null);
			candidateLogic.addOrUpdateCandidateWithFiles(candidateEntity, dcemAction, null, null);
		} catch (Exception e) {
			logger.error("REST-API addCandidate - " + e.toString(), e);
			return Response.serverError().build();
		}
		return null;
	}

	public Response patchCandidate(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	public Response getCandidateFileList(int candidateId, String email) {
		CandidateEntity candidateEntity = getCandidate(candidateId, email);
		List<ApiRecruitingFile> list = new ArrayList<>();
		for (CloudSafeEntity cloudSafeEntity : candidateEntity.getFiles()) {
			list.add(new ApiRecruitingFile(cloudSafeEntity));
		}
		return Response.ok().entity(list).build();
	}

	private List<EntityInterface> getEntities(List<ApiFilterItem> filters, int offset, int page, Class<?> entityClass) throws WebApplicationException {
		try {
			JpaSelectProducer<EntityInterface> jpaSelectProducer = new JpaSelectProducer<EntityInterface>(entityManager, entityClass);
			return jpaSelectProducer.selectCriteriaQueryFilters(filters, offset, page, null);
		} catch (Exception exp) {
			logger.info("REST_API", exp);
			throw new WebApplicationException("", exp, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	public Response getEntityResults(List<ApiFilterItem> filters, int offset, int maxResults, Class<?> entityClass) {
		List<EntityInterface> list = getEntities(filters, offset, maxResults, entityClass);
		return Response.ok().entity(list).build();
	}

	public Response addFileToCandidate(int candidateId, String email, String fileName, StreamingOutput streamingOutput) {
		CandidateEntity candidateEntity = getCandidate(candidateId, email);
		//TODO
		return null;
	}

	private <T> List<VacancyEntity> castVacancyEntityList(List list) {
		return (List<VacancyEntity>) list;
	}

	private CandidateEntity getCandidate(int candidateId, String email) {
		if (email == null) {
			throw new WebApplicationException("email is null", Response.Status.BAD_REQUEST);
		}
		CandidateEntity candidateEntity = candidateLogic.getCandidate(candidateId);
		if (candidateEntity == null) {
			throw new WebApplicationException("Invalid Candidate", Response.Status.BAD_REQUEST);
		}
		if (candidateEntity.getEmail().equals(email) == false) {
			throw new WebApplicationException("No Match for Id and EMail", Response.Status.BAD_REQUEST);
		}
		return candidateEntity;
	}

}
