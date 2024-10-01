package com.doubleclue.dcem.recruiting.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.recruiting.entities.HistoryEntity;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;

@ApplicationScoped
public class HistorySubject extends SubjectAbs {
	
	private static final long serialVersionUID = 1L;

	public HistorySubject() { 
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_REVEAL, null));
	}

	@Override
	public String getModuleId() {
		return  RecruitingModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 999;
	}

	@Override
	public String getIconName() {
		return "fa fa-clock";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}
	
	@Override
	public Class<?> getKlass() {
		return HistoryEntity.class;
	}

}
