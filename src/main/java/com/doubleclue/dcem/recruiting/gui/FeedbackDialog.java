package com.doubleclue.dcem.recruiting.gui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import com.doubleclue.dcem.recruiting.entities.FeedbackEntity;
import com.doubleclue.dcem.recruiting.logic.CandidateLogic;
import com.doubleclue.dcem.recruiting.logic.CandidateStatusLogic;
import com.doubleclue.dcem.recruiting.logic.RecruitingConstants;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;
import com.doubleclue.dcem.recruiting.logic.RecruitingOperatorDepartments;
import com.doubleclue.dcem.recruiting.logic.RecruitingUtils;
import com.doubleclue.dcem.recruiting.subjects.CandidateSubject;

@SessionScoped
@Named("feedbackDialog")
public class FeedbackDialog extends DcemDialog {

	private static final long serialVersionUID = 4013353902474559928L;

	private static Logger logger = LogManager.getLogger(FeedbackDialog.class);

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private CandidateSubject candidateSubject;

	@Inject
	private CandidateLogic candidateLogic;

	@Inject
	private CandidateStatusLogic candidateStatusLogic;
	
	@Inject
	private RecruitingOperatorDepartments recruitingOperatorDepartments;

	private ResourceBundle resourceBundle;
	private CandidateEntity candidate;
	private FeedbackEntity operatorFeedback;
	private List<FeedbackEntity> feedbackList;
	private boolean newFeedback;
	private boolean deletePermission;
	private List<CandidateStatusEntity> candidateStatusList;
	private String selectedStatus;

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		String action = this.getAutoViewAction().getDcemAction().getAction();
		resourceBundle = JsfUtils.getBundle(RecruitingModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		candidate = (CandidateEntity) this.getActionObject();
		
		boolean hasActionPermission = operatorSessionBean.isPermission(autoViewAction.getDcemAction());
		List<DepartmentEntity> departmentsAppliedFor = candidateLogic.getDepartmentsAppliedFor(candidate);
		boolean isHeadOfAnyDepartmentAppliedFor = recruitingOperatorDepartments.isHeadOfAnyDepartmentIn(departmentsAppliedFor);
		CandidateEntity candidateWithWatchers = candidateLogic.getCandidateWithLazyProperty(candidate, CandidateEntity.GRAPH_WATCHERS);
		boolean isWatcher = candidateWithWatchers.getWatchers().contains(operatorSessionBean.getDcemUser());
		if (hasActionPermission == false && isHeadOfAnyDepartmentAppliedFor == false && isWatcher == false) {
			throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS, action);
		}
		candidate = candidateLogic.getCandidateWithLazyProperty(candidate, CandidateEntity.GRAPH_FEEDBACK);
		feedbackList = new ArrayList<FeedbackEntity>(candidate.getFeedback());
		for (FeedbackEntity feedback : feedbackList) {
			feedback.getUser().getPhoto(); // avoid lazy
		}
		operatorFeedback = feedbackList.stream().filter(feedback -> feedback.getUser().equals(operatorSessionBean.getDcemUser())).findFirst().orElse(null);
		if (operatorFeedback != null) {
			newFeedback = false;
			selectedStatus = operatorFeedback.getCandidateStatus().getStatus();
		} else {
			newFeedback = true;
			operatorFeedback = new FeedbackEntity();
			selectedStatus = null;
		}
		deletePermission = operatorSessionBean.isPermission(new DcemAction(candidateSubject, RecruitingConstants.ACTION_DELETE_FEEDBACK));
		candidateStatusList = candidateStatusLogic.getAllCandidateStatuses();
	}

	public void actionAddFeedback() {
		if (operatorFeedback.getRating() < 0 || operatorFeedback.getRating() > 5) {
			JsfUtils.addErrorMessage(resourceBundle, "feedbackDialog.error.ratingRequired");
			return;
		}
		operatorFeedback.setText(operatorFeedback.getText().trim());
		if (operatorFeedback.getText().isEmpty()) {
			JsfUtils.addErrorMessage(resourceBundle, "feedbackDialog.error.feedbackRequired");
			return;
		}
		try {
			operatorFeedback.setUser(operatorSessionBean.getDcemUser());
			operatorFeedback.setPostedOn(LocalDateTime.now());
			operatorFeedback.setCandidate(candidate);
			CandidateStatusEntity selectedCandidateStatus = candidateStatusList.stream().filter(s -> s.getStatus().equals(selectedStatus)).findFirst()
					.orElse(null);
			operatorFeedback.setCandidateStatus(selectedCandidateStatus);

			candidateLogic.updateCandidateFeedback(operatorFeedback);
			if (newFeedback) {
				feedbackList.add(operatorFeedback);
				newFeedback = false;
			}
			return;
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "feedbackDialog.error.unableToAddFeedback");
			logger.error("Unable to add feedback for candidate " + candidate.getFullName(), e);
			return;
		}
	}

	@Override
	public void leavingDialog() {
		candidate = null;
		operatorFeedback = null;
		feedbackList = null;
	}

	public void actionDeleteFeedback(FeedbackEntity feedback) {
		try {
			candidateLogic.deleteCandidateFeedback(feedback);
			feedbackList.remove(feedback);
			if (feedback.equals(operatorFeedback)) {
				newFeedback = true;
				operatorFeedback = new FeedbackEntity();
			}
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "feedbackDialog.error.unableToDeleteFeedback");
			logger.error("Unable to delete feedback for candidate " + candidate.getFullName(), e);
		}
	}

	public void actionReset() {
		operatorFeedback.setRating(0);
		operatorFeedback.setText("");
		operatorFeedback.setCandidateStatus(null);
	}

	public String dateTimeFormatter(LocalDateTime dateTime) {
		return dateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM).withLocale(operatorSessionBean.getLocale()));
	}

	public String statusFormatter(CandidateStatusEntity status) {
		return RecruitingUtils.getCandidateStatusNameFromTextResource(status, operatorSessionBean.getLocale());
	}

	public CandidateEntity getCandidate() {
		return candidate;
	}

	public void setCandidate(CandidateEntity candidate) {
		this.candidate = candidate;
	}

	public List<FeedbackEntity> getFeedbackList() {
		return feedbackList;
	}

	public void setFeedbackList(List<FeedbackEntity> feedbackList) {
		this.feedbackList = feedbackList;
	}

	@Override
	public String getHeight() {
		return "86vh";
	}

	@Override
	public String getWidth() {
		return "50vw";
	}

	public FeedbackEntity getOperatorFeedback() {
		return operatorFeedback;
	}

	public void setOperatorFeedback(FeedbackEntity operatorFeedback) {
		this.operatorFeedback = operatorFeedback;
	}

	public boolean isNewFeedback() {
		return newFeedback;
	}

	public void setNewFeedback(boolean newFeedback) {
		this.newFeedback = newFeedback;
	}

	public boolean isDeletePermission() {
		return deletePermission;
	}

	public void setDeletePermission(boolean deletePermission) {
		this.deletePermission = deletePermission;
	}

	public List<CandidateStatusEntity> getCandidateStatusList() {
		return candidateStatusList;
	}

	public void setCandidateStatusList(List<CandidateStatusEntity> candidateStatusList) {
		this.candidateStatusList = candidateStatusList;
	}

	public String getSelectedStatus() {
		return selectedStatus;
	}

	public void setSelectedStatus(String selectedStatus) {
		this.selectedStatus = selectedStatus;
	}

}
