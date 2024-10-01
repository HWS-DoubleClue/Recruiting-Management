package com.doubleclue.dcem.recruiting.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DepartmentEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.recruiting.entities.CandidateEntity;
import com.doubleclue.dcem.recruiting.entities.CandidateStatusEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity;
import com.doubleclue.dcem.recruiting.logic.CandidateLogic;
import com.doubleclue.dcem.recruiting.logic.CandidateStatusLogic;
import com.doubleclue.dcem.recruiting.logic.RecruitingConstants;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;
import com.doubleclue.dcem.recruiting.logic.RecruitingOperatorDepartments;
import com.doubleclue.dcem.recruiting.logic.RecruitingPdfLogic;
import com.doubleclue.dcem.recruiting.logic.RecruitingUtils;
import com.doubleclue.dcem.recruiting.logic.VacancyLogic;
import com.doubleclue.dcem.recruiting.subjects.CandidateSubject;

@SessionScoped
@Named("appliedCandidatesDialog")
public class ShowAppliedCandidatesDialog extends DcemDialog implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(ShowAppliedCandidatesDialog.class);

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private CandidateLogic candidateLogic;

	@Inject
	private RecruitingPdfLogic recruitingPdfLogic;

	@Inject
	private CandidateStatusLogic candidateStatusLogic;
	
	@Inject
	private RecruitingOperatorDepartments recruitingOperatorDepartments;

	@Inject
	private VacancyLogic vacancyLogic;
	
	@Inject
	private CandidateSubject candidateSubject;

	private ResourceBundle resourceBundle;
	private VacancyEntity vacancyEntity;
	private CandidateEntity selectedCandidate;
	private List<CandidateEntity> candidatesList;
	private List<CandidateStatusEntity> statusList;
	private List<SelectItem> statusListAsSelectItem;

	@PostConstruct
	public void init() {
		resourceBundle = JsfUtils.getBundle(RecruitingModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		String action = this.getAutoViewAction().getDcemAction().getAction();
		statusList = candidateStatusLogic.getAllCandidateStatuses();
		vacancyEntity = (VacancyEntity) this.getActionObject();
		vacancyEntity = vacancyLogic.getFullVacancy(vacancyEntity);

		boolean hasActionPermission = operatorSessionBean.isPermission(autoViewAction.getDcemAction());
		boolean isHeadOf = recruitingOperatorDepartments.isHeadOfDepartment(vacancyEntity.getDepartment());
		boolean isWatcher = vacancyEntity.getWatchers().contains(operatorSessionBean.getDcemUser());
		if (hasActionPermission == false && isHeadOf == false && isWatcher == false) {
			throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS, action);
		}
		
		candidatesList = new ArrayList<>(vacancyEntity.getCandidates());
		statusListAsSelectItem = new ArrayList<SelectItem>();
		for (CandidateStatusEntity status : statusList) {
			String statusLabel = RecruitingUtils.getCandidateStatusNameFromTextResource(status, operatorSessionBean.getLocale());
			statusListAsSelectItem.add(new SelectItem(status, statusLabel));
		}
	}


	public boolean filterByCandidateStatus(Object value, Object filter, Locale locale) {
		CandidateStatusEntity status = (CandidateStatusEntity) value;
		if (filter instanceof String[] filterArray) {
			for (String filterStr : filterArray) {
				if (filterStr.equals(status.getStatus()))
					return true;
			}
			return false;
		}
		return true;
	}

	public String actionLoadCandidateInformation() {
		try {
			selectedCandidate = candidateLogic.getFullCandidate(selectedCandidate);
			
			DcemAction dcemAction = new DcemAction(candidateSubject, DcemConstants.ACTION_SHOW);
			boolean hasFullCandidateViewPermission = operatorSessionBean.isPermission(dcemAction);
			selectedCandidate.setRestrictedViewRights(hasFullCandidateViewPermission == false);
			return recruitingPdfLogic.getTemplateAsHtml(selectedCandidate, RecruitingConstants.CANDIDATE_DISPLAY_TEMPLATE,
					operatorSessionBean.getDcemUser().getLanguage());
		} catch (Exception e) {
			logger.error("Failed to write candidate {} to template {}", selectedCandidate.toString(), RecruitingConstants.VACANCY_DISPLAY_TEMPLATE, e);
			JsfUtils.addErrorMessage(resourceBundle, "error.downloadPdf");
			return null;
		}
	}

	public String languageFormatter(CandidateStatusEntity status) {
		return RecruitingUtils.getCandidateStatusNameFromTextResource(status, operatorSessionBean.getLocale());
	}

	public int ratingFormatter(float rating) {
		return Math.round(rating);
	}

	public VacancyEntity getVacancyEntity() {
		return vacancyEntity;
	}

	public CandidateEntity getSelectedCandidate() {
		return selectedCandidate;
	}

	public void setSelectedCandidate(CandidateEntity selectedCandidate) {
		this.selectedCandidate = selectedCandidate;
	}

	public List<CandidateEntity> getCandidatesList() {
		return candidatesList;
	}

	@Override
	public String getHeight() {
		return "86vh";
	}

	@Override
	public String getWidth() {
		return "82vw";
	}

	public List<SelectItem> getStatusListAsSelectItem() {
		return statusListAsSelectItem;
	}

	public void setStatusListAsSelectItem(List<SelectItem> statusListAsSelectItem) {
		this.statusListAsSelectItem = statusListAsSelectItem;
	}
}
