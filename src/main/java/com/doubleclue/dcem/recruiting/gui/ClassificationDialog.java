package com.doubleclue.dcem.recruiting.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.recruiting.entities.ClassificationEntity;
import com.doubleclue.dcem.recruiting.entities.enums.ClassificationType;
import com.doubleclue.dcem.recruiting.logic.ClassificationLogic;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;
import com.doubleclue.utils.StringUtils;

@Named("classificationDialog")
@SessionScoped
public class ClassificationDialog extends DcemDialog implements Serializable {

	private static final long serialVersionUID = -5260409111884751270L;

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private ClassificationLogic classLogic;

	private List<ClassificationType> selectedTypes;
	private ClassificationEntity classificationEntity;
	private ResourceBundle resourceBundle;

	@PostConstruct
	public void init() {
		resourceBundle = JsfUtils.getBundle(RecruitingModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	@Override
	public boolean actionOk() throws Exception {
		classificationEntity.setName(classificationEntity.getName().trim());
		if (classificationEntity.getName().isEmpty() || StringUtils.isValidNameId(classificationEntity.getName()) == false) {
			JsfUtils.addErrorMessage(resourceBundle, "classDialog.error.nameNotValid");
			return false;
		}
		classificationEntity.setTypes(new TreeSet<>(selectedTypes));
		classLogic.addOrUpdateClassification(classificationEntity, getAutoViewAction().getDcemAction());
		return true;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		classificationEntity = (ClassificationEntity) this.getActionObject();
		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_ADD)) {
			selectedTypes = new ArrayList<>();
		} else if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_EDIT)) {
			selectedTypes = new ArrayList<>(classificationEntity.getTypes());
		}
	}
	
	@Override
	public String getHeight() {
		return "32vh";
	}

	@Override
	public String getWidth() {
		return "35vw";
	}

	public List<ClassificationType> getSelectedTypes() {
		return selectedTypes;
	}

	public void setSelectedTypes(List<ClassificationType> selectedTypes) {
		this.selectedTypes = selectedTypes;
	}

	public List<SelectItem> getClassTypeList() {
		return Stream.of(ClassificationType.values()).map(c -> new SelectItem(c, JsfUtils.getStringSafely(resourceBundle, "classificationType." + c.name())))
				.collect(Collectors.toList());
	}

	public ClassificationEntity getClassificationEntity() {
		return classificationEntity;
	}

	public void setClassificationEntity(ClassificationEntity classificationEntity) {
		this.classificationEntity = classificationEntity;
	}
}
