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
import com.doubleclue.dcem.recruiting.subjects.ClassificationSubject;

@Named("classificationView")
@SessionScoped
public class ClassificationView extends DcemView implements Serializable{
	
	private static final long serialVersionUID = 5146420430665171600L;

	@Inject
	private ClassificationSubject classificationSubject;
	
	@Inject
	private ClassificationDialog classificationDialog;
	
	@PostConstruct
	public void init() {
		classificationDialog.setParentView(this);
		subject = classificationSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(RecruitingModule.RESOURCE_NAME, operatorSessionBean.getLocale());

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, classificationDialog, RecruitingConstants.URL_CLASSIFICATION);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, classificationDialog, RecruitingConstants.URL_CLASSIFICATION);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, classificationDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
	}
	
	@Override
	public void reload() { }
}
