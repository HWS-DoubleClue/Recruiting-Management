package com.doubleclue.dcem.recruiting.logic;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.ActionLogic;
import com.doubleclue.dcem.core.logic.CreateTenant;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.recruiting.entities.CandidateStatusEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyStatusEntity;

@ApplicationScoped
@Named("recruitingModule")
public class RecruitingModule extends DcemModule {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	private CreateTenant createTenant;

	@Inject
	private TemplateLogic templateLogic;

	@Inject
	private ActionLogic actionLogic;

	@Inject
	private UserLogic userLogic;

	@Inject
	private RoleLogic roleLogic;

	@Inject
	private RecruiterLogic recruiterLogic;

	@Inject
	private HistoryLogic historyLogic;

	@Inject
	private CandidateLogic candidateLogic;

	@Inject
	private VacancyLogic vacancyLogic;

	@Inject
	private VacancyStatusLogic vacancyStatusLogic;

	@Inject
	private CandidateStatusLogic candidateStatusLogic;

	public final static String MODULE_ID = "recruiting";
	public final static String MODULE_NAME = "Recruiting";
	public final static String RESOURCE_NAME = "com.doubleclue.dcem.recruiting.resources.Messages";
	public final static int MODULE_RANK = 120;

	public final static String TEMPLATES_PACKAGE = "com/doubleclue/dcem/recruiting/templates";

	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

	@Override
	public int getDbVersion() {
		return 2;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public String getId() {
		return MODULE_ID;
	}

	@Override
	public int getRank() {
		return MODULE_RANK;
	}

	@Override
	public ModulePreferences getDefaultPreferences() {
		return new RecruitingPreferences();
	}

	@Override
	public RecruitingPreferences getModulePreferences() {
		return ((RecruitingPreferences) super.getModulePreferences());
	}

	@Override
	public void initializeDb(DcemUser superAdmin) throws DcemException {
		createTenant.updateTemplates(TEMPLATES_PACKAGE);
	}

	@Override
	@DcemTransactional
	public void initializeTenant(TenantEntity tenantEntity) throws DcemException {
		DcemAction action = new DcemAction(MODULE_ID, "Candidate", DcemConstants.ACTION_REVEAL);
		actionLogic.addDcemActionIfNotExists(action);
		String tenantName = tenantEntity.getName();
		logger.debug("Start initializeTenant " + tenantName);
		RecruitingTenantData tenantData = new RecruitingTenantData();
		super.initializeTenant(tenantEntity, tenantData);
		String templateNames[] = new String[] { RecruitingConstants.VACANCY_DISPLAY_TEMPLATE, RecruitingConstants.VACANCY_WATCHER_TEMPLATE,
				RecruitingConstants.CANDIDATE_DISPLAY_TEMPLATE, RecruitingConstants.CANDIDATE_WATCHER_TEMPLATE };
		SupportedLanguage languages[] = new SupportedLanguage[] { SupportedLanguage.English, SupportedLanguage.German };
		templateLogic.getUpdateTemplateByName(getClass(), templateNames, languages, RecruitingModule.TEMPLATES_PACKAGE);
		DcemUser dcemUser = userLogic.getUser(RecruitingConstants.RECRUITING_SERVICE_OPERATOR);
		if (dcemUser == null) {
			DcemRole role = roleLogic.getDcemRole(DcemConstants.SYSTEM_ROLE_USER);
			dcemUser = new DcemUser(RecruitingConstants.RECRUITING_SERVICE_OPERATOR, null, RecruitingConstants.RECRUITING_SERVICE_OPERATOR, role);
			userLogic.addOrUpdateUserWoAuditing(dcemUser);
		}

		if (vacancyStatusLogic.getVacanyStatusCount() == 0) {
			int order = 0;
			for (int i = 0; i < RecruitingConstants.DEFAULT_VACANCY_STATUS.length; i++) {
				VacancyStatusEntity vacancyStatusEntity = new VacancyStatusEntity(RecruitingConstants.DEFAULT_VACANCY_STATUS[i], order);
				vacancyStatusLogic.addStatus(vacancyStatusEntity);
				order = order + 10;
			}
			for (int i = 0; i < RecruitingConstants.DEFAULT_VACANCY_STATUS_NOT_FOR_HEAD_OFS.length; i++) {
				VacancyStatusEntity vacancyStatusEntity = new VacancyStatusEntity(RecruitingConstants.DEFAULT_VACANCY_STATUS_NOT_FOR_HEAD_OFS[i], order, true,
						false);
				if (Objects.equals(RecruitingConstants.PUBLISHED, vacancyStatusEntity.getStatus())) {
					vacancyStatusEntity.setPublished(true);
				}
				vacancyStatusLogic.addStatus(vacancyStatusEntity);
				order = order + 10;
			}
		}

		if (candidateStatusLogic.getStatusCount() == 0) {
			for (int i = 0; i < RecruitingConstants.CANDIDATE_STANDARD_STATUS.length; i++) {
				CandidateStatusEntity candidateStatusEntity = new CandidateStatusEntity();
				candidateStatusEntity.setStatus(RecruitingConstants.CANDIDATE_STANDARD_STATUS[i]);
				candidateStatusEntity.setRank(10 * i);
				candidateStatusLogic.addStatus(candidateStatusEntity);
			}
		}
	}

	@Override
	@DcemTransactional
	public void deleteUserFromDbPre(DcemUser dcemUser) throws DcemException {
		try {
			historyLogic.deleteRecruitingHistoryOfDcemUser(dcemUser);
			recruiterLogic.deleteDcemUserFromRecruiter(dcemUser);
			candidateLogic.deleteDcemUserFeedbacks(dcemUser);
			candidateLogic.deleteDcemUserFromCandidates(dcemUser);
			vacancyLogic.deleteDcemUserVacancyReferences(dcemUser);
		} catch (Exception e) {
			logger.error("", e);
		}
	}
}
