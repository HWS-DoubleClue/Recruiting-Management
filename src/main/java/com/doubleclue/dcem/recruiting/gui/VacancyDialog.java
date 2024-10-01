package com.doubleclue.dcem.recruiting.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.CellEditEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.doubleclue.dcem.admin.logic.DepartmentLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.BranchLocation;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DepartmentEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.logic.BranchLocationLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.recruiting.entities.CareerLevelEntity;
import com.doubleclue.dcem.recruiting.entities.ClassificationEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyLanguageEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyStatusEntity;
import com.doubleclue.dcem.recruiting.entities.enums.ClassificationType;
import com.doubleclue.dcem.recruiting.entities.enums.VacancyPriority;
import com.doubleclue.dcem.recruiting.entities.enums.VacancyType;
import com.doubleclue.dcem.recruiting.exceptions.RecruitingErrorCodes;
import com.doubleclue.dcem.recruiting.exceptions.RecruitingException;
import com.doubleclue.dcem.recruiting.logic.CareerLevelLogic;
import com.doubleclue.dcem.recruiting.logic.ClassificationLogic;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;
import com.doubleclue.dcem.recruiting.logic.RecruitingOperatorDepartments;
import com.doubleclue.dcem.recruiting.logic.RecruitingUtils;
import com.doubleclue.dcem.recruiting.logic.VacancyLogic;
import com.doubleclue.dcem.recruiting.logic.VacancyStatusLogic;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.doubleclue.dcem.skills.gui.OnCellEditInterfaces;
import com.doubleclue.dcem.skills.gui.SkillsTreeTable;

@SessionScoped
@Named("vacancyDialog")
public class VacancyDialog extends DcemDialog implements Serializable, OnCellEditInterfaces {

	private static final long serialVersionUID = 2479943394469254660L;

	private static Logger logger = LogManager.getLogger(VacancyDialog.class);

	@Inject
	private RecruitingModule module;

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private SkillsTreeTable skillsTreeTable;

	@Inject
	private DepartmentLogic departmentLogic;

	@Inject
	private ClassificationLogic classificationLogic;

	@Inject
	private VacancyLogic vacancyLogic;

	@Inject
	private CareerLevelLogic careerLevelLogic;

	@Inject
	private BranchLocationLogic branchLocationLogic;

	@Inject
	private UserLogic userLogic;

	@Inject
	private RecruitingOperatorDepartments recruitingOperatorDepartments;

	@Inject
	private VacancyStatusLogic vacancyStatusLogic;

	private ResourceBundle resourceBundle;

	private VacancyEntity vacancyEntity;
	private String selectedType;
	private String selectedLanguage;
	private String selectedStatus;
	private String selectedPriority;

	private List<VacancyStatusEntity> statusList;
	private List<SelectItem> statusListAsSelectItem;

	private String selectedClassification;
	private List<ClassificationEntity> classificationList;

	private SortedSet<SkillsLevelEntity> skills;

	private VacancyLanguageEntity toDeleteLanguage;
	private List<VacancyLanguageEntity> vacancyLanguages;

	private String langTitle;
	private String description;

	private int probationTime;
	private int hoursPerWeek;

	private DcemUser decisionMaker;
	private DcemUser clerk;
	private DcemUser newWatcher;
	private List<DcemUser> watchers;

	private String selectedDepartment = "";
	private String currency;

	private List<BranchLocation> branchLocations;
	private List<SelectItem> branchLocationsAsSelectItem;
	private List<Integer> selectedBranchLocationIds;

	private List<CareerLevelEntity> careerLevels;
	private List<SelectItem> careerLevelsAsSelectItem;
	private List<Integer> selectedCareerLevelIds;

	private boolean hasActionPermission;

	@PostConstruct
	public void init() {
		resourceBundle = JsfUtils.getBundle(RecruitingModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	@Override
	public boolean actionOk() throws Exception {
		if (selectedDepartment == null || selectedDepartment.isBlank()) {
		    if (hasActionPermission) {
		        vacancyEntity.setDepartment(null);
		    } else {
		        JsfUtils.addErrorMessage(resourceBundle, "vacancyDialog.error.addYourDepartment");
		        return false;
		    }
		} else {
		    DepartmentEntity departmentEntity = departmentLogic.getDepartmentByName(selectedDepartment.trim());
		    if (departmentEntity == null) {
		        JsfUtils.addErrorMessage(resourceBundle, "vacancyDialog.error.invalidDepartment");
		        return false;
		    }
		    if (hasActionPermission == false && recruitingOperatorDepartments.isHeadOfDepartment(departmentEntity) == false) {
		        JsfUtils.addErrorMessage(resourceBundle, "vacancyDialog.error.addYourDepartment");
		        return false;
		    }
		    vacancyEntity.setDepartment(departmentEntity);
		}
		
		if (clerk != null) { // avoid lazy if transaction fails
			clerk = userLogic.getUser(clerk.getId());
			clerk.getPhoto();
			clerk.getDepartmentName();
			vacancyEntity.setClerk(clerk);
		}
		if (decisionMaker != null) { // avoid lazy if transaction fails
			decisionMaker = userLogic.getUser(decisionMaker.getId());
			decisionMaker.getPhoto();
			decisionMaker.getDepartmentName();
			vacancyEntity.setDecisionMaker(decisionMaker);
		}

		if (selectedClassification != null) {
			ClassificationEntity classificationEntity = classificationList.stream().filter(c -> c.getName().equals(selectedClassification)).findFirst()
					.orElse(null);
			vacancyEntity.setClassification(classificationEntity);
		}
		vacancyEntity.setType(VacancyType.valueOf(selectedType));

		VacancyStatusEntity vacancyStatusEntity = statusList.stream().filter(s -> s.getStatus().equals(selectedStatus)).findFirst().orElse(null);
		vacancyEntity.setStatus(vacancyStatusEntity);

		vacancyEntity.setPriority(VacancyPriority.valueOf(selectedPriority));
		vacancyEntity.setDecisionMaker(decisionMaker);
		if (decisionMaker != null && watchers.contains(decisionMaker) == false) {
			watchers.add(decisionMaker);
		}
		vacancyEntity.setClerk(clerk);
		if (clerk != null && watchers.contains(clerk) == false) {
			watchers.add(clerk);
		}

		SortedSet<VacancyLanguageEntity> vacancyLanguagesSorted = new TreeSet<VacancyLanguageEntity>();
		for (VacancyLanguageEntity vacancyLang : vacancyLanguages) {
			if (vacancyLang.isDefaultLanguage()) {
				vacancyEntity.setDefaultTitle(vacancyLang.getTitle());
				vacancyEntity.setDefaultDescription(vacancyLang.getDescription());
				vacancyEntity.setDefaultLanguage(vacancyLang.getLanguage());
			} else
				vacancyLanguagesSorted.add(vacancyLang);
		}
		vacancyEntity.setLanguages(vacancyLanguagesSorted);

		SortedSet<BranchLocation> vacancyBranchLocations = new TreeSet<BranchLocation>();
		for (BranchLocation branch : branchLocations) {
			if (selectedBranchLocationIds.contains(branch.getId())) {
				vacancyBranchLocations.add(branch);
			}
		}
		vacancyEntity.setBranchLocations(vacancyBranchLocations);

		SortedSet<CareerLevelEntity> vacancyCareerLevels = new TreeSet<CareerLevelEntity>();
		for (CareerLevelEntity careerLevel : careerLevels) {
			if (selectedCareerLevelIds.contains(careerLevel.getId())) {
				vacancyCareerLevels.add(careerLevel);
			}
		}
		vacancyEntity.setCareerLevels(vacancyCareerLevels);

		skills = skillsTreeTable.getSkillAvailableList(null, null);
		vacancyEntity.setSkills(skills);
		vacancyEntity.setWatchers(watchers);
		vacancyEntity.setCurrency(currency);
		vacancyLogic.addOrUpdateVacancy(vacancyEntity, getAutoViewAction().getDcemAction());
		return true;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		String action = this.getAutoViewAction().getDcemAction().getAction();
		hasActionPermission = operatorSessionBean.isPermission(autoViewAction.getDcemAction());

		if (action.equals(DcemConstants.ACTION_DELETE)) {
			List<Object> vacanciesAsObjects = this.autoViewBean.getSelectedItems();
			boolean hasArchivedVacancy = vacanciesAsObjects.stream().anyMatch(obj -> ((VacancyEntity) obj).isArchived());
			if (hasArchivedVacancy) {
				throw new RecruitingException(RecruitingErrorCodes.CANNOT_DELETE_ARCHIVED_VACANCY, "");
			}
			return;
		}

		vacancyEntity = (VacancyEntity) dcemView.getActionObject();
		vacancyLanguages = new ArrayList<VacancyLanguageEntity>();
		if (action.equals(DcemConstants.ACTION_ADD)) {
			vacancyEntity.setStartingFrom(LocalDate.now());
			SupportedLanguage defaultSupportedLanguage = operatorSessionBean.getDcemUser().getLanguage().equals(SupportedLanguage.English_UK)
					? SupportedLanguage.English
					: operatorSessionBean.getDcemUser().getLanguage();
			VacancyLanguageEntity defaultLanguage = new VacancyLanguageEntity(vacancyEntity, defaultSupportedLanguage, true);
			vacancyLanguages.add(defaultLanguage);
			if (operatorSessionBean.getDcemUser().getDepartment() != null) {
				vacancyEntity.setDecisionMaker(operatorSessionBean.getDcemUser().getDepartment().getHeadOf());
			}
			vacancyEntity.setDepartment(operatorSessionBean.getDcemUser().getDepartment());
		} else {
			vacancyEntity = vacancyLogic.getFullVacancyById(vacancyEntity);
			VacancyLanguageEntity defaultLanguage = new VacancyLanguageEntity(vacancyEntity);
			vacancyLanguages.add(defaultLanguage);
		}

		if (action.equals(DcemConstants.ACTION_EDIT)) {
			if (vacancyEntity.isArchived()) {
				throw new RecruitingException(RecruitingErrorCodes.CANNOT_CHANGE_ARCHIVED_VACANCY, "");
			}
			if (hasActionPermission == false) {
				if (recruitingOperatorDepartments.isHeadOfDepartment(vacancyEntity.getDepartment()) == false) {
					throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS, action);
				}
				if (vacancyEntity.getStatus().isLockedForHeadOfs() && recruitingOperatorDepartments.isHeadOf() == true) {
					throw new RecruitingException(RecruitingErrorCodes.LOCKED_FOR_HEADOFS, "");
				}
			}
		}

		if (action.equals(DcemConstants.ACTION_COPY)) {
			vacancyLogic.detachVacancy(vacancyEntity);
			vacancyEntity.setStartingFrom(LocalDate.now());
			vacancyEntity.setId(null);
			vacancyEntity.getLanguages().stream().forEach(l -> l.setId(null));
			vacancyEntity.setLanguages(null);
			vacancyEntity.setWatchers(null);
			vacancyEntity.setCandidates(null);
			vacancyEntity.setTotalCandidates(0);
			vacancyEntity.setArchived(false);
			vacancyEntity.setStatus(null);
		}

		watchers = vacancyEntity.getWatchers();
		for (DcemUser user : watchers) {
			user.getPhoto(); // avoid lazy
			user.getDepartmentName();
		}
		if (vacancyEntity.getLanguages() != null) {
			vacancyLanguages.addAll(vacancyEntity.getLanguages());
		}
		clerk = vacancyEntity.getClerk();
		decisionMaker = vacancyEntity.getDecisionMaker();
		currency = vacancyEntity.getCurrency() != null ? vacancyEntity.getCurrency() : module.getModulePreferences().getCurrency();
		skills = vacancyEntity.getSkills();
		skillsTreeTable.updateSkillTree(skills, this);

		classificationList = classificationLogic.getAllClassificationsByType(ClassificationType.Vacancy);
		initializeBranchLocations();
		initializeCareerLevels();
		initializeStatusList();

		selectedPriority = vacancyEntity.getPriority().name();
		selectedStatus = vacancyEntity.getStatus() != null ? vacancyEntity.getStatus().getStatus() : null;
		selectedDepartment = vacancyEntity.getDepartment() != null ? vacancyEntity.getDepartment().getName() : null;
		selectedClassification = vacancyEntity.getClassification() != null ? vacancyEntity.getClassification().getName() : null;
		selectedType = vacancyEntity.getType().name();
	}

	private void initializeBranchLocations() {
		branchLocations = branchLocationLogic.getAllBranchLocations();
		branchLocationsAsSelectItem = new ArrayList<SelectItem>();
		for (BranchLocation location : branchLocations) {
			branchLocationsAsSelectItem.add(new SelectItem(location.getId(), RecruitingUtils.formatAddress(operatorSessionBean.getLocale(), location)));
		}
		branchLocationsAsSelectItem.sort((o1, o2) -> o1.getLabel().compareTo(o2.getLabel()));
		selectedBranchLocationIds = vacancyEntity.getBranchLocations() != null
				? vacancyEntity.getBranchLocations().stream().map(branch -> branch.getId()).collect(Collectors.toList())
				: new ArrayList<Integer>();
	}

	private void initializeCareerLevels() {
		careerLevels = careerLevelLogic.getAllCareerLevels();
		careerLevelsAsSelectItem = new ArrayList<SelectItem>();
		for (CareerLevelEntity careerLevel : careerLevels) {
			careerLevelsAsSelectItem
					.add(new SelectItem(careerLevel.getId(), RecruitingUtils.getCareerLevelNameFromTextResource(careerLevel, operatorSessionBean.getLocale())));
		}
		selectedCareerLevelIds = vacancyEntity.getCareerLevels() != null
				? vacancyEntity.getCareerLevels().stream().map(careerLevel -> careerLevel.getId()).collect(Collectors.toList())
				: new ArrayList<Integer>();
	}

	private void initializeStatusList() {
		boolean onlyForHeadOf = hasActionPermission == false && recruitingOperatorDepartments.isHeadOf();
		statusList = vacancyStatusLogic.getAllNonDeprecatedVacancyStatus(onlyForHeadOf);
		statusListAsSelectItem = new ArrayList<SelectItem>();
		for (VacancyStatusEntity status : statusList) {
			String statusValue = status.getStatus();
			String statusLabel = RecruitingUtils.getVacancyStatusNameFromTextResource(status, operatorSessionBean.getLocale());
			statusListAsSelectItem.add(new SelectItem(statusValue, statusLabel));
		}
		if (vacancyEntity.getStatus() != null && vacancyEntity.getStatus().isDeprecated()) {
			String statusValue = vacancyEntity.getStatus().getStatus();
			String statusLocalization = RecruitingUtils.getVacancyStatusNameFromTextResource(vacancyEntity.getStatus(), operatorSessionBean.getLocale());
			String noLongerUsed = JsfUtils.getStringSafely(RecruitingModule.RESOURCE_NAME, "VacancyStatus.deprecated");
			String statusLabel = String.format("%s (%s)", statusLocalization, noLongerUsed);
			statusListAsSelectItem.add(new SelectItem(statusValue, statusLabel));
			statusList.add(vacancyEntity.getStatus());
		}
	}

	@Override
	public void leavingDialog() {
		vacancyEntity = null;
		selectedType = null;
		selectedLanguage = null;
		selectedStatus = null;
		selectedPriority = null;
		selectedClassification = null;
		classificationList = null;
		vacancyLanguages = null;
		skills = null;
		toDeleteLanguage = null;
		statusList = null;
		statusListAsSelectItem = null;
		langTitle = null;
		description = null;
		decisionMaker = null;
		clerk = null;
		newWatcher = null;
		watchers = null;
		branchLocations = null;
		branchLocationsAsSelectItem = null;
		selectedBranchLocationIds = null;
		careerLevels = null;
		careerLevelsAsSelectItem = null;
		selectedCareerLevelIds = null;
	}

	@Override
	public void actionConfirm() throws Exception {
		List<Object> vacanciesAsObjects = this.autoViewBean.getSelectedItems();
		List<VacancyEntity> vacancies = vacanciesAsObjects.stream().map(obj -> (VacancyEntity) obj).toList();
		vacancyLogic.deleteVacancies(vacancies);
	}

	public void actionPreAddWatcher() {
		newWatcher = null;
	}

	public void actionAddWatcher() {
		try {
			if (newWatcher == null) {
				JsfUtils.addErrorMessage(resourceBundle, "watchers.error.invalidWatcher");
				return;
			}
			if (watchers.contains(newWatcher)) {
				JsfUtils.addErrorMessage(resourceBundle, "watchers.error.duplicateWatcher");
				return;
			}
			watchers.add(newWatcher);
			newWatcher = null;
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "watchers.error.notFound");
			logger.warn("Unable to locate watcher " + newWatcher.getDisplayNameOrLoginId(), e);
		}
	}

	public void actionRemoveWatcher(DcemUser watcher) {
		try {
			watchers.remove(watcher);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "watchers.error.unableToRemoveWatcher");
			logger.warn("Unable to remove watcher " + watcher.getDisplayName() + " from list", e);
		}
	}

	public void actionPrepareDeleteLanguage(VacancyLanguageEntity vacancyLanguageEntity) {
		toDeleteLanguage = vacancyLanguageEntity;
	}

	public void actionDeleteLanguage() {
		if (vacancyLanguages.size() == 1) {
			JsfUtils.addErrorMessage(resourceBundle, "vacancyDialog.error.cannotDeleteDefault");
			return;
		}
		Iterator<VacancyLanguageEntity> itr = vacancyLanguages.iterator();
		while (itr.hasNext()) {
			VacancyLanguageEntity vacancyLanguage = itr.next();
			if (vacancyLanguage.getLanguage().equals(toDeleteLanguage.getLanguage())) {
				itr.remove();
				return;
			}
		}
	}

	public void actionAddLanguage() {
		vacancyLanguages.add(new VacancyLanguageEntity(vacancyEntity, SupportedLanguage.valueOf(selectedLanguage), false));
	}

	public void actionMakeDefault(VacancyLanguageEntity vacancyLanguageEntity) {
		vacancyLanguages.stream().forEach(l -> l.setDefaultLanguage(false));
		vacancyLanguageEntity.setDefaultLanguage(true);
	}

	@Override
	public void actionOnCellEdit(CellEditEvent<?> event) {
	}

	@Override
	public void listenShowAllSkills() {
		try {
			skills = skillsTreeTable.getSkillAvailableList(null, null);
			skillsTreeTable.updateSkillTree(skills, this);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "vacancyDialog.error.unableToUpdateSkillTree");
			logger.warn("Unable to update skill tree for vacancy " + vacancyEntity.getId(), e);
		}
	}

	public List<String> completeDepartment(String given) {
		List<String> departmentList = new ArrayList<>();
		try {
			if (hasActionPermission) {
				departmentList = departmentLogic.getCompleteDepartmentList(given, 50);
			} else {
				for (DepartmentEntity entity : recruitingOperatorDepartments.getHeadedDepartments()) {
					departmentList.add(entity.getName());
				}
			}
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "candidateDialog.error.unableToRetrieveDepts");
			logger.warn("Unable to retrieve departments ", e);
		}
		return departmentList;
	}

	public void watcherListener() {
		if (newWatcher != null) {
			newWatcher.getPhoto(); // avoid lazy exception
			newWatcher.getDepartmentName();
		}
	}

	public void decisionMakerListener() {
		if (decisionMaker != null) {
			decisionMaker.getPhoto(); // avoid lazy exception
			decisionMaker.getDepartmentName();
		}
	}

	public void clerkListener() {
		if (clerk != null) {
			clerk.getPhoto(); // avoid lazy exception
			clerk.getDepartmentName();
		}
	}

	public VacancyEntity getVacancyEntity() {
		return vacancyEntity;
	}

	public void setVacancyEntity(VacancyEntity vacancyEntity) {
		this.vacancyEntity = vacancyEntity;
	}

	public String getSelectedType() {
		return selectedType;
	}

	public void setSelectedType(String selectedType) {
		this.selectedType = selectedType;
	}

	public List<SelectItem> getTypes() {
		return Stream.of(VacancyType.values()).map(t -> new SelectItem(t, JsfUtils.getStringSafely(resourceBundle, "vacancyType." + t.name())))
				.collect(Collectors.toList());
	}

	public String getSelectedLanguage() {
		return selectedLanguage;
	}

	public void setSelectedLanguage(String selectedLanguage) {
		this.selectedLanguage = selectedLanguage;
	}

	public String getLangTitle() {
		return langTitle;
	}

	public void setLangTitle(String langTitle) {
		this.langTitle = langTitle;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<SelectItem> getLanguages() {
		if (vacancyLanguages == null) {
			vacancyLanguages = new ArrayList<VacancyLanguageEntity>();
		}
		EnumSet<SupportedLanguage> language = EnumSet.complementOf(EnumSet.of(SupportedLanguage.English_UK));
		List<SelectItem> list = language.stream().filter(lang -> vacancyLanguages.stream().allMatch(l -> l.getLanguage().equals(lang) == false))
				.map(l -> new SelectItem(l.name(), getLocalizedLanguage(l.name()))).collect(Collectors.toList());
		return list;
	}

	public String getLocalizedLanguage(String languageName) {
		return JsfUtils.getStringSafely(resourceBundle, "supportedLanguage." + languageName);
	}

	public String getSelectedStatus() {
		return selectedStatus;
	}

	public void setSelectedStatus(String selectedStatus) {
		this.selectedStatus = selectedStatus;
	}

	// public List<SelectItem> getStatus() {
	// EnumSet<VacancyStatus> status = vacancyView.isHeadOf() ? EnumSet.complementOf(EnumSet.of(VacancyStatus.Confirmed, VacancyStatus.Published))
	// : EnumSet.allOf(VacancyStatus.class);
	// List<SelectItem> list = status.stream().map(s -> new SelectItem(s, JsfUtils.getStringSafely(resourceBundle, "vacancyStatus." + s.name())))
	// .collect(Collectors.toList());
	// return list;
	// }

	public List<VacancyStatusEntity> getStatusList() {
		return statusList;
	}

	public List<SelectItem> getAvailableCurrencies() {
		return RecruitingUtils.getAvailableCurrencies(operatorSessionBean.getLocale());
	}

	public String getSelectedPriority() {
		return selectedPriority;
	}

	public void setSelectedPriority(String selectedPriority) {
		this.selectedPriority = selectedPriority;
	}

	public List<SelectItem> getPriorities() {
		return Stream.of(VacancyPriority.values()).map(p -> new SelectItem(p, JsfUtils.getStringSafely(resourceBundle, "vacancyPriority." + p.name())))
				.collect(Collectors.toList());
	}

	public String getSelectedClassification() {
		return selectedClassification;
	}

	public void setSelectedClassification(String selectedClassification) {
		this.selectedClassification = selectedClassification;
	}

	public List<ClassificationEntity> getClassificationsList() {
		return classificationList;
	}

	public int getProbationTime() {
		return probationTime;
	}

	public void setProbationTime(int probationTime) {
		this.probationTime = probationTime;
	}

	public int getHoursPerWeek() {
		return hoursPerWeek;
	}

	public void setHoursPerWeek(int hoursPerWeek) {
		this.hoursPerWeek = hoursPerWeek;
	}

	public List<VacancyLanguageEntity> getVacancyLanguages() {
		return vacancyLanguages;
	}

	public void setVacancyLanguages(List<VacancyLanguageEntity> vacancyLanguages) {
		this.vacancyLanguages = vacancyLanguages;
	}

	public List<DcemUser> getWatchers() {
		return watchers;
	}

	public void setWatchers(List<DcemUser> watchers) {
		this.watchers = watchers;
	}

	public String getSelectedDepartment() {
		return selectedDepartment;
	}

	public void setSelectedDepartment(String selectedDepartment) {
		this.selectedDepartment = selectedDepartment;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	@Override
	public String getHeight() {
		return "89vh";
	}

	public StreamedContent getUserPhoto(DcemUser dcemUser) {
		try {
			if (dcemUser == null) {
				return JsfUtils.getDefaultUserImage();
			}
			byte[] image = dcemUser.getPhoto();
			if (image != null) {
				InputStream in = new ByteArrayInputStream(image);
				return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
			}
		} catch (Exception e) {
			logger.error("Could not get user photo", e);
		}
		return JsfUtils.getDefaultUserImage();
	}

	@Override
	public String getWidth() {
		return "60vw";
	}

	public DcemUser getNewWatcher() {
		return newWatcher;
	}

	public void setNewWatcher(DcemUser newWatcher) {
		this.newWatcher = newWatcher;
	}

	public VacancyLanguageEntity getToDeleteLanguage() {
		return toDeleteLanguage;
	}

	public void setToDeleteLanguage(VacancyLanguageEntity toDeleteLanguage) {
		this.toDeleteLanguage = toDeleteLanguage;
	}

	public DcemUser getDecisionMaker() {
		return decisionMaker;
	}

	public void setDecisionMaker(DcemUser decisionMaker) {
		this.decisionMaker = decisionMaker;
	}

	public DcemUser getClerk() {
		return clerk;
	}

	public void setClerk(DcemUser clerk) {
		this.clerk = clerk;
	}

	public List<ClassificationEntity> getClassificationList() {
		return classificationList;
	}

	public void setClassificationList(List<ClassificationEntity> classificationList) {
		this.classificationList = classificationList;
	}

	public List<BranchLocation> getBranchLocations() {
		return branchLocations;
	}

	public void setBranchLocations(List<BranchLocation> branchLocations) {
		this.branchLocations = branchLocations;
	}

	public List<SelectItem> getBranchLocationsAsSelectItem() {
		return branchLocationsAsSelectItem;
	}

	public void setBranchLocationsAsSelectItem(List<SelectItem> branchLocationsAsSelectItem) {
		this.branchLocationsAsSelectItem = branchLocationsAsSelectItem;
	}

	public List<Integer> getSelectedBranchLocationIds() {
		return selectedBranchLocationIds;
	}

	public void setSelectedBranchLocationIds(List<Integer> selectedBranchLocationIds) {
		this.selectedBranchLocationIds = selectedBranchLocationIds;
	}

	public List<Integer> getSelectedCareerLevelIds() {
		return selectedCareerLevelIds;
	}

	public void setSelectedCareerLevelIds(List<Integer> selectedCareerLevelIds) {
		this.selectedCareerLevelIds = selectedCareerLevelIds;
	}

	public List<SelectItem> getCareerLevelsAsSelectItem() {
		return careerLevelsAsSelectItem;
	}

	public void setCareerLevelsAsSelectItem(List<SelectItem> careerLevelsAsSelectItem) {
		this.careerLevelsAsSelectItem = careerLevelsAsSelectItem;
	}

	public List<SelectItem> getStatusListAsSelectItem() {
		return statusListAsSelectItem;
	}

	public void setStatusListAsSelectItem(List<SelectItem> statusListAsSelectItem) {
		this.statusListAsSelectItem = statusListAsSelectItem;
	}

}