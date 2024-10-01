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
import com.doubleclue.dcem.recruiting.subjects.VacancyStatusSubject;

@SuppressWarnings("serial")
@Named("vacancyStatusView")
@SessionScoped
public class VacancyStatusView extends DcemView {

	@Inject
	private VacancyStatusSubject vacancyStatusEntitySubject;

	@Inject
	private VacancyStatusDialog vacancyStatusEntityDialog;

	@Inject
	DcemApplicationBean applicationBean;

	@PostConstruct
	private void init() {
		subject = vacancyStatusEntitySubject;

		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, operatorSessionBean.getLocale());
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, vacancyStatusEntityDialog, RecruitingConstants.URL_VACANCY_STATUS);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, vacancyStatusEntityDialog, RecruitingConstants.URL_VACANCY_STATUS);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, vacancyStatusEntityDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
	}

	@Override
	public void reload() {
	}

}
