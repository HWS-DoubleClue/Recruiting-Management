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
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.recruiting.entities.CareerLevelEntity;
import com.doubleclue.dcem.recruiting.logic.CareerLevelLogic;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;

@Named("careerLevelEntityDialog")
@SessionScoped
public class CareerLevelEntityDialog extends DcemDialog {

	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(CareerLevelEntityDialog.class);

	@Inject
	private CareerLevelLogic careerLevelLogic;
	
	@Inject
	OperatorSessionBean operatorSessionBean;
	
	@Override
	public boolean actionOk() throws Exception {
		CareerLevelEntity careerLevelEntity = (CareerLevelEntity)this.getActionObject();
		careerLevelLogic.addOrUpdateCareerLevel( careerLevelEntity, this.getAutoViewAction().getDcemAction());
		return true;
	}
	
	@Override
	public void actionConfirm() throws Exception {
		try {
			jpaLogic.deleteEntities(autoViewBean.getSelectedItems(), getAutoViewAction().getDcemAction());
		} catch (DcemException semExp) {
			if (semExp.getErrorCode() == DcemErrorCodes.CONSTRAIN_VIOLATION_DB) {
				JsfUtils.addErrorMessage(RecruitingModule.RESOURCE_NAME, "error.careerLevelDeletion");
			} else {
				logger.info("Couldn't Delete", semExp);
				JsfUtils.addErrorMessage(semExp.toString());
			}
		}
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
	}

	public void leaving() {
	}
}
