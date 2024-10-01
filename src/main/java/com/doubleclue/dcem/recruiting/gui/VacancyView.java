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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

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
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity_;
import com.doubleclue.dcem.recruiting.entities.VacancyStatusEntity_;
import com.doubleclue.dcem.recruiting.logic.RecruitingConstants;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;
import com.doubleclue.dcem.recruiting.logic.RecruitingOperatorDepartments;
import com.doubleclue.dcem.recruiting.logic.VacancyLogic;
import com.doubleclue.dcem.recruiting.subjects.VacancySubject;

@Named("vacancyView")
@SessionScoped
public class VacancyView extends DcemView implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(VacancyView.class);

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private VacancyDialog vacancyDialog;

	@Inject
	private ShowVacancyDialog showVacancyDialog;

	@Inject
	private ShowAppliedCandidatesDialog appliedCandidatesDialog;

	@Inject
	private HistoryDialog historyDialog;

	@Inject
	private VacancySubject vacancySubject;

	@Inject
	private VacancyLogic vacancyLogic;

	@Inject
	private MfaLoginView loginView;

	@Inject
	private RecruitingOperatorDepartments recruitingOperatorDepartments;

	private ResourceBundle resourceBundle;

	private boolean viewManager;

	@PostConstruct
	public void init() {
		vacancyDialog.setParentView(this);
		showVacancyDialog.setParentView(this);
		appliedCandidatesDialog.setParentView(this);
		resourceBundle = JsfUtils.getBundle(RecruitingModule.RESOURCE_NAME, operatorSessionBean.getLocale());

		subject = vacancySubject;

		DcemAction dcemAction = new DcemAction(vacancySubject, DcemConstants.ACTION_MANAGE);
		viewManager = operatorSessionBean.isPermission(dcemAction);

		forceAddAutoViewActionForHeadOfs(DcemConstants.ACTION_ADD, resourceBundle, vacancyDialog, RecruitingConstants.URL_VACANCY);
		forceAddAutoViewActionForHeadOfs(DcemConstants.ACTION_COPY, resourceBundle, vacancyDialog, RecruitingConstants.URL_VACANCY);
		forceAddAutoViewActionForHeadOfs(DcemConstants.ACTION_EDIT, resourceBundle, vacancyDialog, RecruitingConstants.URL_VACANCY);

		// show always
		dcemAction = new DcemAction(vacancySubject, RecruitingConstants.ACTION_SHOW_CANDIDATES);
		autoViewActions.add(new AutoViewAction(dcemAction, appliedCandidatesDialog, resourceBundle,
				subject.getRawAction(RecruitingConstants.ACTION_SHOW_CANDIDATES), RecruitingConstants.URL_VACANCY_SHOW_CANDIDATES, null));
		dcemAction = new DcemAction(vacancySubject, DcemConstants.ACTION_SHOW);
		autoViewActions.add(new AutoViewAction(dcemAction, showVacancyDialog, resourceBundle, subject.getRawAction(DcemConstants.ACTION_SHOW),
				RecruitingConstants.URL_VACANCY_SHOW, null));

		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, vacancyDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(RecruitingConstants.ACTION_ARCHIVE_VACANCY, resourceBundle, null, null);
		addAutoViewAction(RecruitingConstants.ACTION_ACTIVATE_VACANCY, resourceBundle, null, null);

		forceAddAutoViewActionForHeadOfs(RecruitingConstants.ACTION_SHOW_HISTORY, resourceBundle, historyDialog, RecruitingConstants.URL_HISTORY);
	}

	public void forceAddAutoViewActionForHeadOfs(String actionName, ResourceBundle resourceBundle, DcemDialog dcemDialog, String xhtmlPage) {
		boolean access = addAutoViewAction(actionName, resourceBundle, dcemDialog, xhtmlPage);
		if (access == false && recruitingOperatorDepartments.isHeadOf() == true) {
			DcemAction dcemAction = new DcemAction(vacancySubject, actionName);
			autoViewActions.add(new AutoViewAction(dcemAction, dcemDialog, resourceBundle, subject.getRawAction(actionName), xhtmlPage, null));
		}
	}

	@Override
	public void reload() {
		Map<String, String> mapParam = loginView.getShareUrlParams();
		loginView.setMgtUrlParams(null);
		if (mapParam != null) {
			String vacancyId = mapParam.get(VacancyEntity_.ID);
			if (vacancyId != null) {
				ViewVariable viewVariable = getDisplayViewVariable(VacancyEntity_.ID);
				viewVariable.setFilterValue(vacancyId);
				viewVariable.setFilterOperator(FilterOperator.EQUALS);
			}
		} else { // Clear the filter on reload
			ViewVariable viewVariable = getDisplayViewVariable(VacancyEntity_.ID);
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
		// restrict to published
		Predicate publishPredicate = criteriaBuilder.equal(root.get(VacancyEntity_.STATUS).get(VacancyStatusEntity_.PUBLISHED), true);
		// Is user watcher?
		Root<VacancyEntity> vacancyRoot = (Root<VacancyEntity>) root;
		Predicate watcherPredicate = criteriaBuilder.isMember(operatorSessionBean.getDcemUser().getId(), vacancyRoot.get(VacancyEntity_.WATCHERS));

		// Is user head of department?
		Join<VacancyEntity, DepartmentEntity> joinDepartment = vacancyRoot.join(VacancyEntity_.department, JoinType.LEFT);
		Predicate hodPredicate = criteriaBuilder.equal(joinDepartment.get(DepartmentEntity_.headOf), operatorSessionBean.getDcemUser());

		// Is user head of parent department?
		Join<DepartmentEntity, DepartmentEntity> joinParentDepartment = joinDepartment.join(DepartmentEntity_.parentDepartment, JoinType.LEFT);
		Predicate hodParentPredicate = criteriaBuilder.equal(joinParentDepartment.get(DepartmentEntity_.headOf), operatorSessionBean.getDcemUser());

		Predicate predicate = criteriaBuilder.or(publishPredicate, watcherPredicate, hodPredicate, hodParentPredicate);
		predicates.add(predicate);
		return predicates;
	}

	public void actionArchiveVacancy() {
		DcemAction dcemAction = operatorSessionBean.getPermission(new DcemAction(subject, RecruitingConstants.ACTION_ARCHIVE_VACANCY));
		try {
			actionToggleArchiveVacancy(true, dcemAction);
			JsfUtils.addInfoMessage(resourceBundle, "vacancy.archiveSuccess");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
			logger.error("Could not archive vacancies", e);
		}
	}

	public void actionActivateVacancy() {
		DcemAction dcemAction = operatorSessionBean.getPermission(new DcemAction(subject, RecruitingConstants.ACTION_ACTIVATE_VACANCY));
		try {
			actionToggleArchiveVacancy(false, dcemAction);
			JsfUtils.addInfoMessage(resourceBundle, "vacancy.reactivateSuccess");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
			logger.error("Could not unarchive vacancies", e);
		}
	}

	public void actionToggleArchiveVacancy(boolean archived, DcemAction dcemAction) throws Exception {
		List<VacancyEntity> vacancyList = new ArrayList<VacancyEntity>();
		for (Object vacancyObj : autoViewBean.getSelectedItems()) {
			VacancyEntity vacancy = (VacancyEntity) vacancyObj;
			if (vacancy.isArchived() != archived) {
				vacancy.setArchived(archived);
				vacancyList.add(vacancy);
			}
		}
		vacancyLogic.toggleVacancyArchive(vacancyList, archived);
		PrimeFaces.current().ajax().update("autoForm:pTable");
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public void setResourceBundle(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}
}
