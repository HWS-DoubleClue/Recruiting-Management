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
import com.doubleclue.dcem.recruiting.subjects. CareerLevelEntitySubject;

@SuppressWarnings("serial")
@Named("careerLevelEntityView")
@SessionScoped
public class CareerLevelEntityView extends DcemView {

	@Inject
	private CareerLevelEntitySubject careerLevelEntitySubject;

	@Inject
	private CareerLevelEntityDialog careerLevelEntityDialog;  // small letters


	@Inject
	DcemApplicationBean applicationBean;

	@PostConstruct
	private void init() {
		subject = careerLevelEntitySubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, operatorSessionBean.getLocale());		
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, careerLevelEntityDialog, "/modules/recruiting/CareerLevelEntityDialog.xhtml");
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, careerLevelEntityDialog, "/modules/recruiting/CareerLevelEntityDialog.xhtml");
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, careerLevelEntityDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);		
	}

	@Override
	public void reload() {
		
	}

    @Override
	public Object createActionObject() {
		return super.createActionObject();
	}
}
