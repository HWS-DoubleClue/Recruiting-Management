package com.doubleclue.dcem.recruiting.gui;

import java.io.Serializable;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.recruiting.logic.RecruitingConstants;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;
import com.doubleclue.dcem.recruiting.subjects.RecruiterSubject;

@Named("recruiterView")
@SessionScoped
public class RecruiterView extends DcemView implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Inject
	private RecruiterDialog recruiterDialog;
	
	@Inject
	private HistoryDialog historyDialog;
	
	@Inject
	private RecruiterSubject recruiterSubject;
	
	@PostConstruct
	public void init() {
		recruiterDialog.setParentView(this);
		subject = recruiterSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(RecruitingModule.RESOURCE_NAME, operatorSessionBean.getLocale());
			
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, recruiterDialog, RecruitingConstants.URL_RECRUITER);
		addAutoViewAction(DcemConstants.ACTION_COPY, resourceBundle, recruiterDialog, RecruitingConstants.URL_RECRUITER);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, recruiterDialog, RecruitingConstants.URL_RECRUITER);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, recruiterDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(RecruitingConstants.ACTION_SHOW_HISTORY, resourceBundle, historyDialog, "/modules/recruiting/historyDialog.xhtml");
	}

	@Override
	public void reload() { }
}
