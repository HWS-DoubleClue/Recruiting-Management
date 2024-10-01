package com.doubleclue.dcem.recruiting.gui;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.recruiting.entities.CandidateEntity;
import com.doubleclue.dcem.recruiting.entities.HistoryEntity;
import com.doubleclue.dcem.recruiting.entities.RecruiterEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity;
import com.doubleclue.dcem.recruiting.entities.enums.EntityType;
import com.doubleclue.dcem.recruiting.entities.enums.Operation;
import com.doubleclue.dcem.recruiting.logic.HistoryLogic;
import com.doubleclue.dcem.recruiting.logic.RecruitingConstants;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;

@SessionScoped
@Named("historyDialog")
public class HistoryDialog extends DcemDialog implements Serializable {

	private static final long serialVersionUID = 1L;
//	private static Logger logger = LogManager.getLogger(HistoryDialog.class);

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private HistoryLogic historyLogic;

	// variables
	private ResourceBundle resourceBundle;
	private List<HistoryEntity> historyList;

	private EntityType type;
	private int reference;

	// public functions
	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) {
		resourceBundle = JsfUtils.getBundle(RecruitingModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		String action = this.getAutoViewAction().getDcemAction().getAction();

		if (action.equals(RecruitingConstants.ACTION_SHOW_HISTORY)) {
			Object selectedObject = this.autoViewBean.getSelectedItems().get(0);

			if (selectedObject.getClass() == CandidateEntity.class) {
				CandidateEntity candidateEntity = (CandidateEntity) selectedObject;
				type = EntityType.Candidate;
				reference = candidateEntity.getId().intValue();
			}

			if (selectedObject.getClass() == RecruiterEntity.class) {
				RecruiterEntity recruiterEntity = (RecruiterEntity) selectedObject;
				type = EntityType.Recruiter;
				reference = recruiterEntity.getId().intValue();
			}

			if (selectedObject.getClass() == VacancyEntity.class) {
				VacancyEntity vacancyEntity = (VacancyEntity) selectedObject;
				type = EntityType.Vacancy;
				reference = vacancyEntity.getId().intValue();
			}
			historyList = null;
		}
	}

	@Override
	public void leavingDialog() {
		historyList = null;
	}

	public String datetimeFormatter(LocalDateTime timestamp) {
		return timestamp.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM).withLocale(operatorSessionBean.getLocale()));
	}

	public String languageFormatter(Operation operation) {
		return JsfUtils.getStringSafely(resourceBundle, "operation." + operation.name());
	}

	public List<List<String>> historyFormatter(String history) {
		return null;
	}

	// getters and setters
	public List<HistoryEntity> getHistoryList() {

		if (historyList == null) {
			try {
				historyList = historyLogic.getHistoryListByType(type, reference);
			} catch (Exception e) {
				JsfUtils.addErrorMessage(e.toString());
			}
		}
		return historyList;
	}

	public void setHistoryList(List<HistoryEntity> historyList) {
		this.historyList = historyList;
	}

	public Object[] getOperationList() {
		return Stream.of(Operation.values()).map(c -> JsfUtils.getStringSafely(resourceBundle, "operation." + c.name())).toArray();
	}

	@Override
	public String getHeight() {
		return "86vh";
	}

	@Override
	public String getWidth() {
		return "55vw";
	}
}
