package com.doubleclue.dcem.recruiting.logic;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.recruiting.entities.VacancyStatusEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyStatusEntity_;

@ApplicationScoped
public class VacancyStatusLogic {

	private Logger logger = LogManager.getLogger(VacancyStatusLogic.class);

	@Inject
	AdminModule adminModule;

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@DcemTransactional
	public void addOrUpdate(VacancyStatusEntity vacancyStatusEntity, DcemAction dcemAction) throws DcemException {
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			em.persist(vacancyStatusEntity);
		} else {
			em.merge(vacancyStatusEntity);
		}
		auditingLogic.addAudit(dcemAction, vacancyStatusEntity.toString());
	}

	@DcemTransactional
	public void addStatus(VacancyStatusEntity vacancyStatusEntity) throws DcemException {
		em.persist(vacancyStatusEntity);
	}

	public int getVacanyStatusCount() {
		TypedQuery<Long> query;
		query = em.createNamedQuery(VacancyStatusEntity.GET_COUNT_OF_ALL_VACANCY_STATUS, Long.class);
		return query.getSingleResult().intValue();
	}

	public List<VacancyStatusEntity> getAllNonDeprecatedVacancyStatus(boolean forHeadOf) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<VacancyStatusEntity> query = cb.createQuery(VacancyStatusEntity.class);
		Root<VacancyStatusEntity> root = query.from(VacancyStatusEntity.class);
		query.select(root);
		Predicate wherePredicate = cb.equal(root.get(VacancyStatusEntity_.deprecated), false);
		if (forHeadOf) {
			Predicate predicateForHeadOf = cb.equal(root.get(VacancyStatusEntity_.lockedForHeadOfs), !forHeadOf);
			wherePredicate = cb.and(wherePredicate, predicateForHeadOf);
		}
		query.where(wherePredicate);
		return em.createQuery(query).getResultList();
	}

}
