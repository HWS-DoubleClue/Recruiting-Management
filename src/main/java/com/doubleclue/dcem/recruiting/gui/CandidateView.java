package com.doubleclue.dcem.recruiting.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import com.doubleclue.dcem.admin.gui.MfaLoginView;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DepartmentEntity;
import com.doubleclue.dcem.core.entities.DepartmentEntity_;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewVariable;
import com.doubleclue.dcem.core.jpa.FilterOperator;
import com.doubleclue.dcem.recruiting.entities.CandidateEntity;
import com.doubleclue.dcem.recruiting.entities.CandidateEntity_;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity_;
import com.doubleclue.dcem.recruiting.logic.RecruitingConstants;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;
import com.doubleclue.dcem.recruiting.logic.RecruitingOperatorDepartments;
import com.doubleclue.dcem.recruiting.subjects.CandidateSubject;

@Named("candidateView")
@SessionScoped
public class CandidateView extends DcemView implements Serializable {

	private static final long serialVersionUID = 3718034686907226977L;

	@Inject
	private CandidateDialog candidateDialog;

	@Inject
	private DisplayCandidateDialog displayCandidateDialog;

	@Inject
	private FeedbackDialog feedbackDialog;

	@Inject
	private HistoryDialog historyDialog;

	@Inject
	private CandidateSubject candidateSubject;

	@Inject
	private MfaLoginView loginView;
	
	@Inject
	private RecruitingOperatorDepartments recruitingOperatorDepartments;
	
	private ResourceBundle resourceBundle;

	private boolean viewManager;

	@PostConstruct
	public void init() {
		candidateDialog.setParentView(this);
		displayCandidateDialog.setParentView(this);
		feedbackDialog.setParentView(this);

		subject = candidateSubject;
		resourceBundle = JsfUtils.getBundle(RecruitingModule.RESOURCE_NAME, operatorSessionBean.getLocale());

		DcemAction dcemAction = new DcemAction(candidateSubject, DcemConstants.ACTION_MANAGE);
		viewManager = operatorSessionBean.isPermission(dcemAction);

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, candidateDialog, RecruitingConstants.URL_CANDIDATE);
		addAutoViewAction(DcemConstants.ACTION_COPY, resourceBundle, candidateDialog, RecruitingConstants.URL_CANDIDATE);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, candidateDialog, RecruitingConstants.URL_CANDIDATE);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, candidateDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);

		dcemAction = new DcemAction(subject, DcemConstants.ACTION_SHOW);
		autoViewActions.add(new AutoViewAction(dcemAction, displayCandidateDialog, resourceBundle,
				subject.getRawAction(DcemConstants.ACTION_SHOW), RecruitingConstants.URL_CANDIDATE_SHOW, null));
		dcemAction = new DcemAction(subject,RecruitingConstants.ACTION_GIVE_FEEDBACK);
		autoViewActions.add(new AutoViewAction(dcemAction, feedbackDialog, resourceBundle,
				subject.getRawAction(RecruitingConstants.ACTION_GIVE_FEEDBACK), RecruitingConstants.URL_CANDIDATE_GIVE_FEEDBACK, null));
		
		addAutoViewAction(RecruitingConstants.ACTION_SHOW_HISTORY, resourceBundle, historyDialog, RecruitingConstants.URL_HISTORY);
	}
	
	public void forceAddAutoViewActionForHeadOfs(String actionName, ResourceBundle resourceBundle, DcemDialog dcemDialog, String xhtmlPage) {
		boolean access = addAutoViewAction(actionName, resourceBundle, dcemDialog, xhtmlPage);
		if (access == false && recruitingOperatorDepartments.isHeadOf() == true) {
			DcemAction dcemAction = new DcemAction(candidateSubject, actionName);
			autoViewActions.add(new AutoViewAction(dcemAction, dcemDialog, resourceBundle, subject.getRawAction(actionName), xhtmlPage, null));
		}
	}

	@Override
	public void reload() {
	    Map<String, String> mapParam = loginView.getShareUrlParams(); 
	    loginView.setMgtUrlParams(null);
	    if (mapParam != null) {
	        String vacancyId = mapParam.get(CandidateEntity_.ID); 
	        if (vacancyId != null) {
	            ViewVariable viewVariable = getDisplayViewVariable(CandidateEntity_.ID); 
	            viewVariable.setFilterValue(vacancyId); 
	            viewVariable.setFilterOperator(FilterOperator.EQUALS);
	        }
		} else { // Clear the filter on reload
			ViewVariable viewVariable = getDisplayViewVariable(CandidateEntity_.ID);
			viewVariable.setFilterValue(null);
			viewVariable.setFilterOperator(FilterOperator.NONE);
		}
	}

	public boolean isViewManager() {
		return viewManager;
	}

	@Override
	public List<Predicate> getPredicates(CriteriaBuilder criteriaBuilder, Root<?> root) {
		ArrayList<Predicate> predicates = new ArrayList<Predicate>();
		if (viewManager == true) {
			return predicates;
		}
		// Is user watcher?
		Root<CandidateEntity> candidateRoot = (Root<CandidateEntity>) root;
		Predicate watcherPredicate = criteriaBuilder.isMember(operatorSessionBean.getDcemUser().getId(), candidateRoot.get(CandidateEntity_.WATCHERS));

		// Subquery for Head of Department -- workaround for missing "distinct"
		CriteriaQuery<Integer> criteriaQuery = criteriaBuilder.createQuery(Integer.class);
		Subquery<Integer> hodSubquery = criteriaQuery.subquery(Integer.class);
		
		Root<CandidateEntity> hodSubqueryRoot = hodSubquery.from(CandidateEntity.class);
		Join<CandidateEntity, VacancyEntity> hodJoinVacancy = hodSubqueryRoot.join(CandidateEntity_.vacancies, JoinType.LEFT);
		Join<VacancyEntity, DepartmentEntity> hodJoinDepartment = hodJoinVacancy.join(VacancyEntity_.department, JoinType.LEFT);
		Join<DepartmentEntity, DepartmentEntity> hodParentJoinParentDepartment = hodJoinDepartment.join(DepartmentEntity_.parentDepartment, JoinType.LEFT);

		Predicate hodPredicate = criteriaBuilder.equal(hodJoinDepartment.get(DepartmentEntity_.headOf), operatorSessionBean.getDcemUser());
		Predicate hodParentPredicate = criteriaBuilder.equal(hodParentJoinParentDepartment.get(DepartmentEntity_.headOf), operatorSessionBean.getDcemUser());
		
		hodSubquery.select(hodSubqueryRoot.get(CandidateEntity_.id));
		hodSubquery.where(criteriaBuilder.or(hodPredicate, hodParentPredicate));
		
		Predicate departmentPredicate = candidateRoot.get(CandidateEntity_.id).in(hodSubquery);
		Predicate predicate = criteriaBuilder.or(watcherPredicate, departmentPredicate);
		predicates.add(predicate);
		return predicates;
	}

}
