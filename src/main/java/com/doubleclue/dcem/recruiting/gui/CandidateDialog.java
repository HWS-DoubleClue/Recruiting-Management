package com.doubleclue.dcem.recruiting.gui;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.doubleclue.comm.thrift.CloudSafeOptions;
import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.DeviceEntity;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.CloudSafeUploadFile;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.DcemUploadFile;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.GenderEnum;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.utils.typedetector.DcemMediaType;
import com.doubleclue.dcem.core.utils.typedetector.FileUploadDetector;
import com.doubleclue.dcem.recruiting.entities.CandidateEntity;
import com.doubleclue.dcem.recruiting.entities.CandidateStatusEntity;
import com.doubleclue.dcem.recruiting.entities.ClassificationEntity;
import com.doubleclue.dcem.recruiting.entities.RecruiterEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity;
import com.doubleclue.dcem.recruiting.entities.enums.ClassificationType;
import com.doubleclue.dcem.recruiting.entities.enums.EntityType;
import com.doubleclue.dcem.recruiting.logic.CandidateLogic;
import com.doubleclue.dcem.recruiting.logic.CandidateStatusLogic;
import com.doubleclue.dcem.recruiting.logic.ClassificationLogic;
import com.doubleclue.dcem.recruiting.logic.RecruiterLogic;
import com.doubleclue.dcem.recruiting.logic.RecruitingConstants;
import com.doubleclue.dcem.recruiting.logic.RecruitingEmailLogic;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;
import com.doubleclue.dcem.recruiting.logic.RecruitingPdfLogic;
import com.doubleclue.dcem.recruiting.logic.RecruitingUtils;
import com.doubleclue.dcem.recruiting.logic.VacancyLogic;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.doubleclue.dcem.skills.gui.OnCellEditInterfaces;
import com.doubleclue.dcem.skills.gui.SkillsTreeTable;

@SessionScoped
@Named("candidateDialog")
public class CandidateDialog extends DcemDialog implements Serializable, OnCellEditInterfaces {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(CandidateDialog.class);

	private static final String CANDIDATES_DATA_ZIP = "CandidateFiles.zip";

	@Inject
	private RecruitingModule module;

	@Inject
	private DcemApplicationBean applicationBean;

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private SkillsTreeTable skillsTreeTable;

	@Inject
	private CloudSafeLogic cloudSafeLogic;

	@Inject
	private VacancyLogic vacancyLogic;

	@Inject
	private RecruiterLogic recruiterLogic;

	@Inject
	private ClassificationLogic classificationLogic;

	@Inject
	private CandidateLogic candidateLogic;

	@Inject
	private RecruitingEmailLogic recruitingEmailLogic;

	@Inject
	private RecruitingPdfLogic recruitingPdfLogic;

	@Inject
	private CandidateStatusLogic candidateStatusLogic;

	private CandidateEntity candidateEntity;
	private ResourceBundle resourceBundle;

	private List<VacancyEntity> availableVacancies;
	private SortedSet<VacancyEntity> relevantVacancies;
	private List<VacancyEntity> selectedVacancies;

	private String recruiterName;
	private List<RecruiterEntity> recruitersList;

	private DcemUser newWatcher;
	private List<DcemUser> watchers;

	private String selectedClassification;
	private List<ClassificationEntity> classificationList;

	private SortedSet<SkillsLevelEntity> candidateSkills;

	private boolean multipleFiles;

	private CloudSafeEntity cloudSafeRoot;
	private List<CloudSafeEntity> selectedFiles;
	private List<DcemUploadFile> uploadedFiles;

	private GenderEnum gender;
	private String currency;
	private DcemUser recommendedBy;

	private List<CloudSafeEntity> displayFiles;
	private List<CloudSafeEntity> deletedFiles;

	private List<CandidateStatusEntity> statuses;
	private String selectedStatus;

	private List<CandidateStatusEntity> statusList;
	private List<SelectItem> statusListAsSelectItem;

	private String fileInfo;
	private CloudSafeEntity filePreview;
	private boolean pdfFile;

	private StreamedContent downloadFile;

	@PostConstruct
	public void init() {
		resourceBundle = JsfUtils.getBundle(RecruitingModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		cloudSafeRoot = cloudSafeLogic.getCloudSafeRoot();
	}

	@Override
	public boolean actionOk() throws Exception {
		CandidateStatusEntity candidateStatus = statusList.stream().filter(s -> s.getStatus().equals(selectedStatus)).findFirst().orElse(null);
		candidateEntity.setStatus(candidateStatus);
		candidateEntity.setWatchers(watchers);
		candidateEntity.setVacancies(new TreeSet<>(relevantVacancies));
		candidateEntity.setClassification(Objects.isNull(selectedClassification) == false && selectedClassification.isEmpty() == false
				? classificationList.stream().filter(c -> c.getName().equals(selectedClassification)).findFirst().get()
				: null);
		candidateEntity.setRecruiterEntity(Objects.isNull(recruiterName) == false && recruiterName.isEmpty() == false
				? recruitersList.stream().filter(r -> r.getCompanyName().equals(recruiterName)).findFirst().get()
				: null);
		candidateEntity.setRecommendedBy(recommendedBy);
		candidateEntity.setGender(gender);
		candidateEntity.setCurrency(currency);
		candidateEntity.setSkills(skillsTreeTable.getSkillAvailableList(null, null));

		try {
			candidateLogic.addOrUpdateCandidateWithFiles(candidateEntity, getAutoViewAction().getDcemAction(), uploadedFiles, deletedFiles);
			recruitingEmailLogic.notifyWatcher(watchers, candidateEntity);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "candidateDialog.error.unableToAddOrUpdate");
			logger.error("Unable to add or update candidate " + candidateEntity.getFullName() + " to database", e);
			return false;
		}
		return true;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		String action = this.getAutoViewAction().getDcemAction().getAction();
		if (action.equals(DcemConstants.ACTION_DELETE)) {
			return;
		}
		deletedFiles = new ArrayList<CloudSafeEntity>();
		classificationList = classificationLogic.getAllClassificationsByType(ClassificationType.Candidate);
		recruitersList = recruiterLogic.getAllRecruiters();
		candidateEntity = (CandidateEntity) this.getActionObject();
		if (action.equals(DcemConstants.ACTION_EDIT) || action.equals(DcemConstants.ACTION_COPY)) {
			try {
				candidateEntity = candidateLogic.getFullCandidate(candidateEntity);
				selectedStatus = candidateEntity.getStatus().getStatus();
			} catch (Exception e) {
				JsfUtils.addErrorMessage(resourceBundle, "candidateDialog.error.unableToRetrieveCandidate");
				logger.error("Unable to retrieve details of candidate " + candidateEntity.getFullName(), e);
			}
		}

		if (action.equals(DcemConstants.ACTION_ADD)) {
			candidateEntity.setCitizenship(module.getModulePreferences().getNationality());
			candidateEntity.setCountry(module.getModulePreferences().getCountry());
		}

		if (action.equals(DcemConstants.ACTION_COPY)) {
			candidateLogic.detacheCandidate(candidateEntity);
			candidateEntity.setId(null);
			candidateEntity.setFeedback(null);
			candidateEntity.setFiles(null);
			candidateEntity.setStatus(null);
			candidateEntity.setRanking(0);
		}
		watchers = candidateEntity.getWatchers();
		for (DcemUser user : watchers) {
			user.getPhoto(); // avoid lazy
		}
		recommendedBy = candidateEntity.getRecommendedBy();
		if (recommendedBy != null) {
			recommendedBy.getPhoto(); // avoid lazy
		}
		initializeStatusList();
		availableVacancies = vacancyLogic.getTitleAndIdOfAllActiveVacancies();
		gender = candidateEntity.getGender();
		recruiterName = candidateEntity.getRecruiterEntity() != null ? candidateEntity.getRecruiterEntity().toString() : null;
		candidateSkills = candidateEntity.getSkills() != null ? candidateEntity.getSkills() : new TreeSet<SkillsLevelEntity>();
		skillsTreeTable.updateSkillTree(candidateSkills, this);
		currency = candidateEntity.getCurrency() != null ? candidateEntity.getCurrency() : module.getModulePreferences().getCurrency();
		relevantVacancies = candidateEntity.getVacancies() != null ? candidateEntity.getVacancies() : new TreeSet<>();
		uploadedFiles = new ArrayList<DcemUploadFile>();
		selectedFiles = new ArrayList<CloudSafeEntity>();
		displayFiles = candidateEntity.getFiles() != null ? candidateEntity.getFiles().stream().collect(Collectors.toList()) : new ArrayList<CloudSafeEntity>();
		selectedClassification = candidateEntity.getClassification() != null ? candidateEntity.getClassification().getName() : null;
		selectedVacancies = new ArrayList<VacancyEntity>(relevantVacancies);
		filePreview = null;
		pdfFile = false;
	}

	@Override
	public void actionConfirm() throws Exception {
		List<Object> candidatesAsObjects = this.autoViewBean.getSelectedItems();
		List<CandidateEntity> candidates = candidatesAsObjects.stream().map(obj -> (CandidateEntity) obj).toList();
		candidateLogic.deleteCandidates(candidates);
	}

	public void initializeStatusList() {
		statusList = candidateStatusLogic.getAllCandidateStatuses();
		statusListAsSelectItem = new ArrayList<SelectItem>();

		for (CandidateStatusEntity status : statusList) {
			String statusValue = status.getStatus();
			String statusLabel = RecruitingUtils.getCandidateStatusNameFromTextResource(status, operatorSessionBean.getLocale());
			statusListAsSelectItem.add(new SelectItem(statusValue, statusLabel));
		}
		if (candidateEntity.getStatus() != null && candidateEntity.getStatus().isDeprecated()) {
			String statusValue = candidateEntity.getStatus().getStatus();
			String statusLocalization = RecruitingUtils.getCandidateStatusNameFromTextResource(candidateEntity.getStatus(), operatorSessionBean.getLocale());
			String noLongerUsed = JsfUtils.getStringSafely(RecruitingModule.RESOURCE_NAME, "candidateStatus.deprecated");
			String statusLabel = String.format("%s (%s)", statusLocalization, noLongerUsed);
			statusListAsSelectItem.add(new SelectItem(statusValue, statusLabel));
			statusList.add(candidateEntity.getStatus());
		}
	}

	@Override
	public void leavingDialog() {
		statusList = null;
		statusListAsSelectItem = null;
		candidateEntity = null;
		availableVacancies = null;
		relevantVacancies = null;
		selectedVacancies = null;
		recruiterName = null;
		recruitersList = null;
		newWatcher = null;
		watchers = null;
		selectedClassification = null;
		classificationList = null;
		candidateSkills = null;
		selectedFiles = null;
		currency = null;
		uploadedFiles = null;
		recommendedBy = null;
		displayFiles = null;
		deletedFiles = null;
		recruitingPdfLogic.deleteDcemUploadFile(uploadedFiles);
		uploadedFiles = null;
		filePreview = null;
	}

	@Override
	public void actionOnCellEdit(CellEditEvent<?> event) {

	}

	public void actionPreAddVacancy() {
		selectedVacancies = relevantVacancies.stream().collect(Collectors.toList());
	}

	public void actionUpdateVacancies() {
		relevantVacancies = new TreeSet<>(selectedVacancies);
	}

	public void removeVacancy(VacancyEntity vacancyEntity) {
		try {
			relevantVacancies.remove(vacancyEntity);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "candidateDialog.error.unableToRemoveVacancy");
			logger.error("Unable to remove vacancy " + vacancyEntity.getId() + " from list", e);
		}
	}

	public void actionPreAddWatcher() {
		newWatcher = null;
	}

	public void listenWatcher() {
		newWatcher.getPhoto();
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
			PrimeFaces.current().executeScript("PF('watcherDialog').hide();");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "watchers.error.notFound");
			logger.warn("Unable to locate watcher " + newWatcher.getDisplayNameOrLoginId(), e);
		}
	}

	public void removeWatcher(DcemUser watcher) {
		try {
			watchers.remove(watcher);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "watchers.error.unableToRemoveWatcher");
			logger.warn("Unable to remove watcher " + watcher.getDisplayName() + " from list", e);
		}
	}

	@Override
	public void listenShowAllSkills() {
		try {
			candidateEntity.setSkills(skillsTreeTable.getSkillAvailableList(null, null));
			skillsTreeTable.updateSkillTree(candidateEntity.getSkills(), this);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "vacancyDialog.error.unableToUpdateSkillTree");
			logger.error("Unable to update skill tree for candidate " + candidateEntity.getFullName(), e);
		}
	}

	public String toUploadFile(CloudSafeEntity cloudSafeEntity) {
		if (cloudSafeEntity.getId() == null) {
			return JsfUtils.getStringSafely(resourceBundle, "candidateDialog.toUpload");
		}
		return JsfUtils.getStringSafely(resourceBundle, "candidateDialog.uploaded");
	}

	public void removeFile() {
		if (selectedFiles.isEmpty()) {
			JsfUtils.addErrorMessage(resourceBundle, "candidateDialog.selectFile");
			return;
		}
		for (CloudSafeEntity cloudSafeEntity : selectedFiles) {
			Iterator<CloudSafeEntity> itr = displayFiles.iterator();
			while (itr.hasNext()) {
				CloudSafeEntity cloudSafeFile = itr.next();
				if (cloudSafeEntity.getName().equals(cloudSafeFile.getName())) {
					itr.remove();
					break;
				}
			}
			for (DcemUploadFile dcemUploadFile : uploadedFiles) {
				if (dcemUploadFile.fileName.equals(cloudSafeEntity.getName())) {
					uploadedFiles.remove(dcemUploadFile);
					break;
				}
			}

			if (Objects.isNull(cloudSafeEntity.getId()) == false) {
				candidateEntity.getFiles().remove(cloudSafeEntity);
				deletedFiles.add(cloudSafeEntity);
			}
		}
		selectedFiles.clear();
	}

	public void actionDownloadSingleFile() {
		downloadFile = null;
		if (selectedFiles.isEmpty()) {
			JsfUtils.addErrorMessage(resourceBundle, "candidateDialog.selectFile");
			return;
		}
		CloudSafeEntity selectedFile = selectedFiles.get(0);
		try {
			InputStream inputStream = chooseInputStream(selectedFile);
			downloadFile = DefaultStreamedContent.builder().name(selectedFile.getName()).contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.stream(() -> inputStream).build();
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "candidateDialog.error.unableToDownloadFile");
			logger.error(" Unable to download file " + selectedFile.getName(), e);
		}
	}

	private InputStream chooseInputStream(CloudSafeEntity cloudSafeEntity) throws Exception {
		if (cloudSafeEntity.getId() != null) {
			return cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, null, null);
		} else {
			String fileName = cloudSafeEntity.getName();
			DcemUploadFile file = uploadedFiles.stream().filter(uploadedFile -> uploadedFile.fileName.equals(fileName)).findFirst().orElse(null);
			return new FileInputStream(file.file);
		}
	}

	public void actionDownloadMultipleFilesOrFolders() {
		if (selectedFiles == null || selectedFiles.isEmpty()) {
			JsfUtils.addErrorMessage(resourceBundle, "candidateDialog.selectFile");
			return;
		}
		try (OutputStream output = JsfUtils.getDownloadFileOutputStream("application/zip", CANDIDATES_DATA_ZIP);
				ZipOutputStream zipOutputStream = new ZipOutputStream(output);) {
			List<CloudSafeUploadFile> cloudSafeUploadFiles = new ArrayList<CloudSafeUploadFile>();
			for (CloudSafeEntity cloudSafeEntity : selectedFiles) {
				if (cloudSafeEntity.getId() != null) {
					cloudSafeUploadFiles.add(new CloudSafeUploadFile(cloudSafeEntity.getName(), null, cloudSafeEntity));
				} else {
					String fileName = cloudSafeEntity.getName();
					DcemUploadFile file = uploadedFiles.stream().filter(uploadedFile -> uploadedFile.fileName.equals(fileName)).findFirst().orElse(null);
					cloudSafeUploadFiles.add(new CloudSafeUploadFile(fileName, file.file, cloudSafeEntity));
				}
			}
			recruitingPdfLogic.zipCloudSafeEntities(zipOutputStream, cloudSafeUploadFiles);
			FacesContext.getCurrentInstance().responseComplete();
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "candidateDialog.error.unableToDownloadZip");
			logger.warn("Unable to download archive of selected files " + CANDIDATES_DATA_ZIP, e);
			FacesContext.getCurrentInstance().responseComplete();
		}
	}

	public void handleFileUpload(FileUploadEvent event) {
		if (displayFiles.size() > RecruitingConstants.MAXIMUM_FILE_NUMBER) {
			JsfUtils.addErrorMessage(resourceBundle, "error.fileNumberExceeded", String.valueOf(RecruitingConstants.MAXIMUM_FILE_NUMBER));
			return;
		}
		String fileName = event.getFile().getFileName();
		if (fileName.contains(",")) {
			JsfUtils.addErrorMessage(resourceBundle, "error.fileNameCannotContainComma", fileName);
			return;
		}
		for (CloudSafeEntity cloudSafeEntity : displayFiles) {
			if (cloudSafeEntity.getName().equals(fileName)) {
				JsfUtils.addErrorMessage(resourceBundle, "error.fileNameAlreadyExists", fileName);
				return;
			}
			if (candidateEntity.getId() != null) {
				String fileNameForDB = RecruitingUtils.createFileNameForDB(fileName, candidateEntity, EntityType.Candidate);
				if (cloudSafeEntity.getName().equals(fileNameForDB)) {
					JsfUtils.addErrorMessage(resourceBundle, "error.fileNameAlreadyExists", fileName);
					return;
				}
			}
		}

		try {
			uploadedFiles.add(FileUploadDetector.getUploadedFile(fileName, event.getFile().getInputStream(), RecruitingConstants.ALLOWED_FILE_TYPES));
		} catch (DcemException exp) {
			JsfUtils.addErrorMessage(exp.getLocalizedMessage());
			return;
		} catch (Exception e) {
			JsfUtils.addErrorMessage("Unexpected Error! " + e.toString());
			return;
		}

		CloudSafeEntity cloudSafeEntity = new CloudSafeEntity(CloudSafeOwner.GLOBAL, (DcemUser) null, (DeviceEntity) null, fileName, (LocalDateTime) null,
				CloudSafeOptions.ENC.name(), false, cloudSafeRoot, operatorSessionBean.getDcemUser());
		cloudSafeEntity.setLength(event.getFile().getSize());
		displayFiles.add(cloudSafeEntity);
	}

	public void setMultipleFiles() {
		if (selectedFiles == null || selectedFiles.size() <= 1) {
			multipleFiles = false;
		}
		if (selectedFiles.size() > 1) {
			multipleFiles = true;
		}
	}

	public void actionPrepareFileInfoEdit() {
		if (selectedFiles.isEmpty()) {
			JsfUtils.addErrorMessage(resourceBundle, "candidateDialog.selectFile");
			return;
		}
		if (selectedFiles.size() > 1) {
			JsfUtils.addErrorMessage(resourceBundle, "candidateDialog.selectOneFile"); // TODO
			return;
		}
		fileInfo = selectedFiles.get(0).getInfo();
		PrimeFaces.current().executeScript("PF('addInfoDialogVar').show();");
	}

	public void actionPrepareFilePreview() {
		if (selectedFiles.isEmpty()) {
			JsfUtils.addErrorMessage(resourceBundle, "candidateDialog.selectFile");
			return;
		}
		if (selectedFiles.size() > 1) {
			return;
		}
		filePreview = selectedFiles.get(0);
		try {
			String mediaType = FileUploadDetector.detectMediaType(chooseInputStream(filePreview));
			if (Objects.equals(mediaType, DcemMediaType.PDF.getMediaType())) {
				pdfFile = true;
			} else if (Objects.equals(mediaType, DcemMediaType.JPEG.getMediaType()) || Objects.equals(mediaType, DcemMediaType.PNG.getMediaType())) {
				pdfFile = false;
			} else {
				JsfUtils.addErrorMessage(resourceBundle, "error.filePreviewFormat");
				filePreview = null;
				return;
			}
			PrimeFaces.current().executeScript("PF('previewFileDialogVar').show();");
		} catch (Exception e) {
			logger.error("", e);
			filePreview = null;
			JsfUtils.addErrorMessage(resourceBundle, "error.filePreview");
		}
	}

	public void actionUpdateInfo() {
		CloudSafeEntity selectedDisplayFile = selectedFiles.get(0);
		selectedDisplayFile.setInfo(fileInfo);
		if (selectedDisplayFile.getId() == null) {
			DcemUploadFile uploadFile = uploadedFiles.stream().filter(dcemUploadFile -> dcemUploadFile.fileName.equals(selectedDisplayFile.getName()))
					.findFirst().orElse(null);
			if (uploadFile != null) {
				uploadFile.info = fileInfo;
			} else {
				JsfUtils.addErrorMessage(resourceBundle, "error.global");
				return;
			}
		}
		PrimeFaces.current().executeScript("PF('addInfoDialogVar').hide();");
	}

	public String getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(String fileInfo) {
		this.fileInfo = fileInfo;
	}

	public StreamedContent getCandidatePhoto() {
		if (candidateEntity == null) {
			return JsfUtils.getDefaultUserImage();
		}
		return getPhoto(candidateEntity.getPhoto());
	}

	public StreamedContent getUserPhoto(DcemUser dcemUser) {
		if (dcemUser == null) {
			return JsfUtils.getDefaultUserImage();
		}
		return getPhoto(dcemUser.getPhoto());
	}

	private StreamedContent getPhoto(byte[] image) {
		if (image != null) {
			InputStream in = new ByteArrayInputStream(image);
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
		} else {
			return JsfUtils.getDefaultUserImage();
		}
	}

	public StreamedContent getFilePreview() {
		if (filePreview == null) {
			return null;
		}
		try {
			InputStream inputStream = chooseInputStream(filePreview);
			if (pdfFile) {
				return DefaultStreamedContent.builder().name(filePreview.getName()).contentType(DcemMediaType.PDF.getMediaType()).stream(() -> inputStream)
						.build();
			} else {
				return DefaultStreamedContent.builder().name(filePreview.getName()).contentType(RecruitingConstants.GENERIC_IMAGE_MEDIATYPE)
						.stream(() -> inputStream).build();
			}
		} catch (Exception e) {
			logger.error("Could not create preview filestream", e);
			JsfUtils.addErrorMessage(resourceBundle, "error.global");
			return null;
		}
	}

	public void handlePhotoUpload(FileUploadEvent event) {
		if (event == null) {
			return;
		}
		try {
			candidateEntity.setPhoto(DcemUtils.resizeImage(event.getFile().getContent(), DcemConstants.PHOTO_MAX));
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "candidateDialog.error.unableToUploadPhoto");
			logger.error("Unable to upload photo " + event.getFile().getFileName(), e);
		}
	}

	public CandidateEntity getCandidateEntity() {
		return candidateEntity;
	}

	public void setCandidateEntity(CandidateEntity candidateEntity) {
		this.candidateEntity = candidateEntity;
	}

	public List<VacancyEntity> getAvailableVacancies() {
		return availableVacancies;
	}

	public void setAvailableVacancies(List<VacancyEntity> availableVacancies) {
		this.availableVacancies = availableVacancies;
	}

	public String getRecruiterName() {
		return recruiterName;
	}

	public void setRecruiterName(String recruiterName) {
		this.recruiterName = recruiterName;
	}

	public List<RecruiterEntity> getRecruitersList() {
		return recruitersList;
	}

	public List<DcemUser> getWatchers() {
		return watchers;
	}

	public void setWatchers(List<DcemUser> watchers) {
		this.watchers = watchers;
	}

	public String getSelectedClassification() {
		return selectedClassification;
	}

	public void setSelectedClassification(String ClassificationEntity) {
		this.selectedClassification = ClassificationEntity;
	}

	public List<ClassificationEntity> getClassificationsList() {
		return classificationList;
	}

	public SortedSet<SkillsLevelEntity> getCandidateSkills() {
		return candidateSkills;
	}

	public void setCandidateSkills(SortedSet<SkillsLevelEntity> candidateSkills) {
		this.candidateSkills = candidateSkills;
	}

	public List<SelectItem> getGenders() {
		List<SelectItem> list = new LinkedList<>();
		ResourceBundle dcemResourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, operatorSessionBean.getLocale());
		list = Arrays.stream(GenderEnum.values()).map(g -> new SelectItem(g, dcemResourceBundle.getString(g.name()))).collect(Collectors.toList());
		return list;
	}

	public List<SelectItem> getAvailableCurrencies() {
		return RecruitingUtils.getAvailableCurrencies(operatorSessionBean.getLocale());
	}

	public List<SelectItem> getAvailableCountries() {
		return applicationBean.getAvailableCountries(operatorSessionBean.getLocale());
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public GenderEnum getGender() {
		return gender;
	}

	public void setGender(GenderEnum gender) {
		this.gender = gender;
	}

	public List<DcemUploadFile> getUploadedFiles() {
		return uploadedFiles;
	}

	public void setUploadedFiles(List<DcemUploadFile> uploadedFiles) {
		this.uploadedFiles = uploadedFiles;
	}

	public List<CloudSafeEntity> getFiles() {
		return displayFiles;
	}

	public List<CloudSafeEntity> getDeletedFiles() {
		return deletedFiles;
	}

	public void setDeletedFiles(List<CloudSafeEntity> deletedFiles) {
		this.deletedFiles = deletedFiles;
	}

	public List<CloudSafeEntity> getSelectedFiles() {
		return selectedFiles;
	}

	public void setSelectedFiles(List<CloudSafeEntity> selectedFiles) {
		this.selectedFiles = selectedFiles;
	}

	public boolean isMultipleFiles() {
		return multipleFiles;
	}

	public void setMultipleFiles(boolean multipleFiles) {
		this.multipleFiles = multipleFiles;
	}

	public StreamedContent getDownloadFile() {
		return downloadFile;
	}

	public void setDownloadFile(StreamedContent downloadFile) {
		this.downloadFile = downloadFile;
	}

	@Override
	public String getHeight() {
		return "80vh";
	}

	@Override
	public String getWidth() {
		return "80vw";
	}

	public DcemUser getNewWatcher() {
		return newWatcher;
	}

	public void setNewWatcher(DcemUser newWatcher) {
		this.newWatcher = newWatcher;
		newWatcher.getPhoto(); // avoid lazy exception
	}

	public DcemUser getRecommendedBy() {
		return recommendedBy;
	}

	public void setRecommendedBy(DcemUser recommendedBy) {
		this.recommendedBy = recommendedBy;
	}

	public SortedSet<VacancyEntity> getRelevantVacancies() {
		return relevantVacancies;
	}

	public void setRelevantVacancies(SortedSet<VacancyEntity> relevantVacancies) {
		this.relevantVacancies = relevantVacancies;
	}

	public List<VacancyEntity> getSelectedVacancies() {
		return selectedVacancies;
	}

	public void setSelectedVacancies(List<VacancyEntity> selectedVacancies) {
		this.selectedVacancies = selectedVacancies;
	}

	public String getSelectedStatus() {
		return selectedStatus;
	}

	public void setSelectedStatus(String selectedStatus) {
		this.selectedStatus = selectedStatus;
	}

	public List<CandidateStatusEntity> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<CandidateStatusEntity> statuses) {
		this.statuses = statuses;
	}

	public List<CandidateStatusEntity> getStatusList() {
		return statusList;
	}

	public void setStatusList(List<CandidateStatusEntity> statusList) {
		this.statusList = statusList;
	}

	public List<SelectItem> getStatusListAsSelectItem() {
		return statusListAsSelectItem;
	}

	public void setStatusListAsSelectItem(List<SelectItem> statusListAsSelectItem) {
		this.statusListAsSelectItem = statusListAsSelectItem;
	}
	
	public void setPdfFile(boolean pdfFile) {
		this.pdfFile = pdfFile;
	}

	public boolean isPdfFile() {
		return pdfFile;
	}
}
