package com.doubleclue.dcem.recruiting.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.recruiting.entities.CandidateEntity;
import com.doubleclue.dcem.recruiting.logic.RecruitingConstants;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;

@ApplicationScoped
public class CandidateSubject extends SubjectAbs {

	private static final long serialVersionUID = 1L;

	public CandidateSubject() {
		setShowIfHeadOf(true);
		
		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_COPY, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_OR_MORE));

		RawAction showAction = new RawAction(DcemConstants.ACTION_SHOW, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY);
		showAction.setActionType(ActionType.DIALOG);
		showAction.setIcon("fa fa-file-pdf");
		rawActions.add(showAction);

		RawAction reviewAction = new RawAction(RecruitingConstants.ACTION_GIVE_FEEDBACK,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_ONLY);
		reviewAction.setActionType(ActionType.DIALOG);
		reviewAction.setIcon("fa fa-comment-dots");
		rawActions.add(reviewAction);

		RawAction deleteReviewAction = new RawAction(RecruitingConstants.ACTION_DELETE_FEEDBACK,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_ONLY);
		rawActions.add(deleteReviewAction);

		RawAction historyAction = new RawAction(RecruitingConstants.ACTION_SHOW_HISTORY,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_ONLY);
		historyAction.setActionType(ActionType.DIALOG);
		historyAction.setIcon("fa fa-clock");
		rawActions.add(historyAction);

		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_USER }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_REVEAL, null));
	}

	@Override
	public String getModuleId() {
		return RecruitingModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 20;
	}

	@Override
	public String getIconName() {
		return "fa fa-people-group";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return CandidateEntity.class;
	}

	
	@Override
	public boolean forceAction(DcemUser dcemUser, DcemAction dcemAction) {
		return false;
	}
}
