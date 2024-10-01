package com.doubleclue.dcem.recruiting.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.recruiting.logic.RecruitingConstants;
import com.doubleclue.dcem.recruiting.subjects.CandidateStatusSubject;

@SuppressWarnings("serial")
@Named("candidateStatusView")
@SessionScoped
public class CandidateStatusView extends DcemView {

	@Inject
	private CandidateStatusSubject candidateStatusSubject;

	@Inject
	private CandidateStatusDialog candidateStatusDialog;


	@Inject
	DcemApplicationBean applicationBean;

	@PostConstruct
	private void init() {
		subject = candidateStatusSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, operatorSessionBean.getLocale());		
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, candidateStatusDialog, RecruitingConstants.URL_CANDIDATE_STATUS);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, candidateStatusDialog, RecruitingConstants.URL_CANDIDATE_STATUS);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, candidateStatusDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);		
	}

	@Override
	public void reload() {
		
	}

}
