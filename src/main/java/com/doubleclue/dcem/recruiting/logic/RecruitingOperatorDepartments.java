package com.doubleclue.dcem.recruiting.logic;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.logic.DepartmentLogic;
import com.doubleclue.dcem.core.entities.DepartmentEntity;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;

@SessionScoped
@Named("recruitingOperatorDepartments")
public class RecruitingOperatorDepartments implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private DepartmentLogic departmentLogic;

	private Boolean headOf;

	private List<DepartmentEntity> headedDepartments;

	@PostConstruct
	public void init() {
		loadDepartment();
	}

	private void loadDepartment() {
		headedDepartments = departmentLogic.getDepartmentsByHeadOf(operatorSessionBean.getDcemUser());
		headOf = headedDepartments.isEmpty() == false;
	}

	public boolean isHeadOfAnyDepartmentIn(List<DepartmentEntity> departments) {
		for (DepartmentEntity department : departments) {
			if (isHeadOfDepartment(department)) {
				return true;
			}
		}
		return false;
	}

	public boolean isHeadOfDepartment(DepartmentEntity departmentEntity) {
		if (departmentEntity == null) {
			return false;
		}
		if (headedDepartments == null) {
			loadDepartment();
		}
		for (DepartmentEntity headedDepartment : headedDepartments) {
			if (headedDepartment.getId() == departmentEntity.getId()) {
				return true;
			}
		}
		return false;
	}

	public boolean isHeadOf() {
		if (headOf == null) {
			loadDepartment();
		}
		return headOf;
	}

	public List<DepartmentEntity> getHeadedDepartments() {
		if (headedDepartments == null) {
			loadDepartment();
		}
		return headedDepartments;
	}

}
