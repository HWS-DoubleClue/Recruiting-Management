package com.doubleclue.dcem.recruiting.logic;

import java.time.LocalDateTime;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.utils.compare.CompareUtils;
import com.doubleclue.dcem.recruiting.entities.ClassificationEntity;
import com.doubleclue.dcem.recruiting.entities.HistoryEntity;
import com.doubleclue.dcem.recruiting.entities.enums.ClassificationType;
import com.doubleclue.dcem.recruiting.entities.enums.EntityType;
import com.doubleclue.dcem.recruiting.entities.enums.Operation;

@Named("classificationLogic")
@ApplicationScoped
public class ClassificationLogic {

	@Inject
	private EntityManager em;
	
	@Inject
	private OperatorSessionBean operatorSessionBean;
	
	@Inject
	private HistoryLogic historyLogic;
	
	@DcemTransactional
	public void addOrUpdateClassification(ClassificationEntity classificationEntity, DcemAction action) throws Exception {
		HistoryEntity history = new HistoryEntity(operatorSessionBean.getDcemUser(), LocalDateTime.now(), EntityType.Classification);
		if (action.getAction().equals(DcemConstants.ACTION_ADD)) {
			em.persist(classificationEntity);
			history.setOperation(Operation.Create);
			history.setInformation(CompareUtils.compareObjects(null, classificationEntity));
		} else {
			ClassificationEntity oldclassificationEntity = em.find(ClassificationEntity.class, classificationEntity.getId());
			history.setOperation(Operation.Edit);
			history.setInformation(CompareUtils.compareObjects(oldclassificationEntity, classificationEntity));
			em.detach(oldclassificationEntity);
			em.merge(classificationEntity);
		}		
		history.setReference(classificationEntity.getId().intValue());
		historyLogic.addHistory(history);
	}
	
	public List<ClassificationEntity> getAllClassificationsByType(ClassificationType type){
		TypedQuery<ClassificationEntity> query = em.createNamedQuery(ClassificationEntity.GET_ALL_BY_TYPE, ClassificationEntity.class);
		query.setParameter(1, type);
		return query.getResultList();
	}

	public ClassificationEntity getByName(String name) {
		TypedQuery<ClassificationEntity> query = em.createNamedQuery(ClassificationEntity.GET_BY_NAME, ClassificationEntity.class);
		query.setParameter(1, name);
		return query.getSingleResult();
	}

	public ClassificationEntity getById(int id) {
		return em.find(ClassificationEntity.class, id);
	}
}
