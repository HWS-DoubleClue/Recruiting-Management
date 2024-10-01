package com.doubleclue.dcem.recruiting.logic;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.recruiting.entities.CandidateStatusEntity;

@ApplicationScoped
public class CandidateStatusLogic {

	private Logger logger = LogManager.getLogger(CandidateStatusLogic.class);

	@Inject
	AdminModule adminModule;

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@DcemTransactional
	public void addOrUpdate(CandidateStatusEntity candidateStatusEntity, DcemAction dcemAction) throws DcemException {
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			em.persist(candidateStatusEntity);
		} else {
			em.merge(candidateStatusEntity);
		}
		auditingLogic.addAudit(dcemAction, candidateStatusEntity.toString()); // to be improved
	}

	@DcemTransactional
	public void addStatus(CandidateStatusEntity candidateStatusEntity) throws DcemException {
		em.persist(candidateStatusEntity);
	}

	public Integer getStatusCount() {
		Long statusCount = em.createNamedQuery(CandidateStatusEntity.COUNT_ALL, Long.class).getSingleResult();
		return statusCount.intValue();
	}

	public List<CandidateStatusEntity> getAllCandidateStatuses() {
		return em.createNamedQuery(CandidateStatusEntity.GET_ALL, CandidateStatusEntity.class).getResultList();
	}

	public CandidateStatusEntity getCandidateStatusById(int id) {
		return em.find(CandidateStatusEntity.class, id);
	}
}
