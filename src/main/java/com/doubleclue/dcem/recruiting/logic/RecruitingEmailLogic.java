package com.doubleclue.dcem.recruiting.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.tasks.EmailTask;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.recruiting.entities.CandidateEntity;
import com.doubleclue.dcem.recruiting.entities.CandidateEntity_;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity_;
import com.doubleclue.dcem.recruiting.gui.CandidateView;
import com.doubleclue.dcem.recruiting.gui.VacancyView;
import com.doubleclue.dcem.core.gui.ViewNavigator;

@ApplicationScoped
@Named("recruitingEmailLogic")
public class RecruitingEmailLogic {

	private Logger logger = LogManager.getLogger(RecruitingEmailLogic.class);

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	DcemApplicationBean dcemApplicationBean;

	@Inject
	EntityManager em;

	@Inject
	RecruitingModule recruitingModule;

	@Inject
	UserLogic userLogic;

	@Inject
	TaskExecutor taskExecutor;
	
	@Inject
    ViewNavigator viewNavigator;

    @Inject
    VacancyView vacancyView;
    
    @Inject
    CandidateView candidateView;

	public void notifyWatcher(List<DcemUser> recipients, VacancyEntity vacancy) throws Exception {
        if (recruitingModule.getModulePreferences().isDisableEmailNotifications()) {
            return;
        }
        try {
            if (recipients.isEmpty() == false) {
                String vacancyUrl = "";
                try {
                    Map<String, String> paramMap = new HashMap<>();
                    paramMap.put(VacancyEntity_.ID, String.valueOf(vacancy.getId()));
                    vacancyUrl = viewNavigator.createUrlLink(vacancyView, paramMap);
                } catch (Exception e) {
                    logger.error("Could not create vacancy-url", e);
                    vacancyUrl = viewNavigator.createUrlLink(vacancyView, null);
                }
                Map<String, Object> data = new HashMap<>();
                data.put("vacancyFullname", vacancy.toString());
                data.put("vacancyUrl", vacancyUrl);
                taskExecutor.execute(new EmailTask(recipients, data, RecruitingConstants.VACANCY_WATCHER_TEMPLATE,
                        RecruitingConstants.EMAIL_VACANCY_MODIFIED_SUBJECT_BUNDLE_KEY, null));
            }
        } catch (Exception e) {
            logger.error("Could not notify watchers for vacancy: " + vacancy.toString(), e);
        }
    }

	public void notifyWatcher(List<DcemUser> recipients, CandidateEntity candidate) throws Exception {
	    if (recruitingModule.getModulePreferences().isDisableEmailNotifications()) {
	        return;
	    }
	    try {
	        if (recipients.isEmpty() == false) {
	            String candidateUrl = "";
	            try {
	                Map<String, String> paramMap = new HashMap<>();
	                paramMap.put(CandidateEntity_.ID, String.valueOf(candidate.getId()));
	                candidateUrl = viewNavigator.createUrlLink(candidateView, paramMap);
	            } catch (Exception e) {
	                logger.error("Could not create candidate-url", e);
	                candidateUrl = viewNavigator.createUrlLink(candidateView, null);
	            }
	            Map<String, Object> data = new HashMap<>();
	            data.put("candidateName", candidate.getFullName());
	            data.put("candidateUrl", candidateUrl);
	            taskExecutor.execute(new EmailTask(recipients, data, RecruitingConstants.CANDIDATE_WATCHER_TEMPLATE,
	                    RecruitingConstants.EMAIL_CANDIDATE_MODIFIED_SUBJECT_BUNDLE_KEY, null));
	        }
	    } catch (Exception e) {
	        logger.error("Could not notify watchers for candidate: " + candidate.toString(), e);
	    }
	}


}
