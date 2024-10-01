package com.doubleclue.dcem.recruiting.logic;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.recruiting.entities.HistoryEntity;
import com.doubleclue.dcem.recruiting.entities.enums.EntityType;

@ApplicationScoped
@Named("historyLogic")
public class HistoryLogic {
	@Inject
	private EntityManager em;

	@DcemTransactional
	public void addHistory(HistoryEntity history) throws Exception {
		if (history.getInformation().trim().isEmpty() == false) {
			em.persist(history);
		}
	}

	public List<HistoryEntity> getHistoryListByType(EntityType type, int reference) {
		TypedQuery<HistoryEntity> query = em.createNamedQuery(HistoryEntity.GET_SINGLE_BY_TYPE, HistoryEntity.class);
		query.setParameter(1, type);
		query.setParameter(2, reference);
		return query.getResultList();
	}

	@DcemTransactional
	public void deleteRecruitingHistoryOfDcemUser(DcemUser dcemUser) throws Exception {
		List<HistoryEntity> entries = em.createNamedQuery(HistoryEntity.GET_ALL_ENTRIES_BY_DCEMUSER, HistoryEntity.class).setParameter(1, dcemUser).getResultList();
		for (HistoryEntity entry : entries) {
			em.remove(entry);
		}
	}
}
