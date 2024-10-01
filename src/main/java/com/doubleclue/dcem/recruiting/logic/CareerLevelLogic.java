package com.doubleclue.dcem.recruiting.logic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.utils.compare.CompareUtils;
import com.doubleclue.dcem.recruiting.entities.CareerLevelEntity;
import com.doubleclue.dcem.recruiting.entities.CareerLevelEntity_;
import com.doubleclue.dcem.recruiting.entities.HistoryEntity;
import com.doubleclue.dcem.recruiting.entities.enums.EntityType;
import com.doubleclue.dcem.recruiting.entities.enums.Operation;

@ApplicationScoped
@Named("careerLevelLogic")
public class CareerLevelLogic {

	@Inject
	private EntityManager em;

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private HistoryLogic historyLogic;

	@DcemTransactional
	public void addOrUpdateCareerLevel(CareerLevelEntity careerLevelEntity, DcemAction action) throws Exception {
		HistoryEntity history = new HistoryEntity(operatorSessionBean.getDcemUser(), LocalDateTime.now(), EntityType.CareerLevel);
		if (action.getAction().equals(DcemConstants.ACTION_ADD)) {
			em.persist(careerLevelEntity);
			history.setOperation(Operation.Create);
			history.setInformation(CompareUtils.compareObjects(null, careerLevelEntity));
		} else {
			CareerLevelEntity oldCareerLevelEntity = em.find(CareerLevelEntity.class, careerLevelEntity.getId());
			history.setOperation(Operation.Edit);
			history.setInformation(CompareUtils.compareObjects(oldCareerLevelEntity, careerLevelEntity));
			em.detach(oldCareerLevelEntity);
			em.merge(careerLevelEntity);
		}
		history.setReference(careerLevelEntity.getId().intValue());
		historyLogic.addHistory(history);
	}

	public List<String> getFilteredCareerLevelNames(String filter, int maxResults) throws Exception {
		TypedQuery<String> query = em.createNamedQuery(CareerLevelEntity.GET_FILTERED_CAREER_LEVEL_NAMES, String.class);
		return query.setParameter(1, "%" + filter.toLowerCase() + "%").setMaxResults(maxResults).getResultList();
	}

	public SortedSet<CareerLevelEntity> getCareerLevelsByNames(List<String> careerLevelNames) throws Exception {
		TypedQuery<CareerLevelEntity> query = em.createNamedQuery(CareerLevelEntity.GET_CAREER_LEVEL_BY_NAMES, CareerLevelEntity.class);
		return new TreeSet<>(query.setParameter(1, careerLevelNames).getResultList());
	}

	public List<CareerLevelEntity> getAllCareerLevels() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CareerLevelEntity> query = cb.createQuery(CareerLevelEntity.class);
		Root<CareerLevelEntity> careerLevelRoot = query.from(CareerLevelEntity.class);
		query.select(careerLevelRoot);
		query.orderBy(cb.asc(careerLevelRoot.get(CareerLevelEntity_.careerLevel)));
		return em.createQuery(query).getResultList();
	}

}
