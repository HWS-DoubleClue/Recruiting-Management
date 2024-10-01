//
//
package com.doubleclue.dcem.recruiting.subjects;
//
import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;
import com.doubleclue.dcem.recruiting.entities.CandidateStatusEntity;

@SuppressWarnings("serial")
@ApplicationScoped
public class CandidateStatusSubject extends SubjectAbs {
	
	public CandidateStatusSubject() {

		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.IGNORE));	
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_HELPDESK },
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_HELPDESK },
				ActionSelection.ONE_OR_MORE));
				
		rawActions.add(new RawAction (DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));	
		
	}	

	@Override
	public String getModuleId() {
		return RecruitingModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 60;
	}

	
	@Override
	public String getIconName() {
		return "fa fa-solid fa-user-tie";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}


	@Override
	public Class<?> getKlass() {
		return CandidateStatusEntity.class;
	}

}
