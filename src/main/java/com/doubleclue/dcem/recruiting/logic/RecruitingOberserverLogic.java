package com.doubleclue.dcem.recruiting.logic;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.BranchLocation;
import com.doubleclue.dcem.skills.entities.SkillsEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.doubleclue.dcem.skills.logic.SkillsFireEvents;
import com.doubleclue.dcem.skills.logic.SkillsMergeDTO;

@ApplicationScoped
@Named("recruitingObserverLogic")
public class RecruitingOberserverLogic implements SkillsFireEvents {

	private static final Logger logger = LogManager.getLogger(RecruitingOberserverLogic.class);

	@Inject
	private VacancyLogic vacancyLogic;

	@Inject
	private CandidateLogic candidateLogic;

	public void deleteSkillsListener(@Observes List<SkillsEntity> skillsEntities) throws Exception {
	}

	public void deleteSkillsLevelListener(@Observes List<SkillsLevelEntity> skillsLevelEntities) throws Exception {
		try {
			vacancyLogic.deleteSkillsFromVancancies(skillsLevelEntities);
			candidateLogic.deleteSkillsFromCandidates(skillsLevelEntities);
		} catch (Exception e) {
			logger.error("Could not delete SkillsLevel in Recruiting", e); // logging is not useful if handled at fired event
			throw new Exception("Could not delete SkillsLevel in Recruiting", e);
		}
	}

	@Override
	public void mergeSkillsListener(@Observes SkillsMergeDTO skillsMergeDTO) throws Exception {
		try {
			vacancyLogic.mergeSkillsInVancancies(skillsMergeDTO);
			candidateLogic.mergeSkillsInCandidates(skillsMergeDTO);
		} catch (Exception e) {
			logger.error("Could not merge Skills in Recruiting", e); // logging is not useful if handled at fired event
			throw new Exception("Could not merge Skills in Recruiting", e);
		}
	}

	public void deleteBranchLocations(@Observes List<BranchLocation> branchLocations) throws Exception {
		try {
			vacancyLogic.deleteBranchLocations(branchLocations);
		} catch (Exception e) {
			logger.error("Could not delete BranchLocations in Recruiting", e);
			throw new Exception("Could not delete BranchLocations in Recruiting", e);
		}
	}
}