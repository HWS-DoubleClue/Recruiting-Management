package com.doubleclue.dcem.recruiting.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.recruiting.entities.VacancyStatusEntity;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;
import com.doubleclue.dcem.recruiting.logic.VacancyStatusLogic;

@Named("vacancyStatusDialog")
@SessionScoped
public class VacancyStatusDialog extends DcemDialog {

	private Logger logger = LogManager.getLogger(VacancyStatusDialog.class);

	@Inject
	private VacancyStatusLogic vacancyStatusEntityLogic;

	@Inject
	JpaLogic jpaLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private static final long serialVersionUID = 1L;

	@Override
	public boolean actionOk() throws Exception {
		VacancyStatusEntity vacancyStatusEntity = (VacancyStatusEntity) this.getActionObject();
		vacancyStatusEntityLogic.addOrUpdate(vacancyStatusEntity, this.getAutoViewAction().getDcemAction());
		return true;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
	}

	@Override
	public void actionConfirm() throws Exception {
		try {
			jpaLogic.deleteEntities(autoViewBean.getSelectedItems(), getAutoViewAction().getDcemAction());
		} catch (DcemException semExp) {
			if (semExp.getErrorCode() == DcemErrorCodes.CONSTRAIN_VIOLATION_DB) {
				JsfUtils.addErrorMessage(RecruitingModule.RESOURCE_NAME, "error.vacancyStatusDeletion");
			} else {
				logger.info("Couldn't Delete", semExp);
				JsfUtils.addErrorMessage(semExp.toString());
			}
		}
	}

	public void leaving() {
	}
}
