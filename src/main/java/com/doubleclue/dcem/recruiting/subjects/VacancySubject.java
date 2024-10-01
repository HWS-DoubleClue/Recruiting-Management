package com.doubleclue.dcem.recruiting.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity;
import com.doubleclue.dcem.recruiting.logic.RecruitingConstants;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;

@ApplicationScoped
public class VacancySubject extends SubjectAbs {

	private static final long serialVersionUID = 1L;

	public static final String SUBJECT_NAME = "Vacancy";

	public VacancySubject() {
		setShowIfHeadOf(true);

		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_COPY, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_OR_MORE));

		RawAction rawActionArchive = new RawAction(RecruitingConstants.ACTION_ARCHIVE_VACANCY,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_OR_MORE);
		rawActionArchive.setIcon("fa fa-solid fa-folder");
		rawActionArchive.setActionType(ActionType.EL_METHOD);
		rawActionArchive.setElMethodExpression("#{vacancyView.actionArchiveVacancy()}");
		rawActions.add(rawActionArchive);

		RawAction rawActionActivate = new RawAction(RecruitingConstants.ACTION_ACTIVATE_VACANCY,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_OR_MORE);
		rawActionActivate.setIcon("fa fa-solid fa-folder-open");
		rawActionActivate.setActionType(ActionType.EL_METHOD);
		rawActionActivate.setElMethodExpression("#{vacancyView.actionActivateVacancy()}");
		rawActions.add(rawActionActivate);

		RawAction membersAction = new RawAction(RecruitingConstants.ACTION_SHOW_CANDIDATES,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_ONLY);
		membersAction.setActionType(ActionType.DIALOG);
		membersAction.setIcon("fa fa-people-group");
		rawActions.add(membersAction);

		RawAction showAction = new RawAction(DcemConstants.ACTION_SHOW, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY);
		showAction.setActionType(ActionType.DIALOG);
		showAction.setIcon("fa fa-file-pdf");
		rawActions.add(showAction);

		RawAction historyAction = new RawAction(RecruitingConstants.ACTION_SHOW_HISTORY,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_ONLY);
		historyAction.setActionType(ActionType.DIALOG);
		historyAction.setIcon("fa fa-clock");
		rawActions.add(historyAction);

		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_USER }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_REVEAL, null));
	};

	@Override
	public String getModuleId() {
		return RecruitingModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 10;
	}

	@Override
	public String getIconName() {
		return "fa fa-door-open";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return VacancyEntity.class;
	}

	@Override
	public boolean forceAction(DcemUser dcemUser, DcemAction dcemAction) {
		return false;
	}
}
