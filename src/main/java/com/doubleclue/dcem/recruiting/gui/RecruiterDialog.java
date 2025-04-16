package com.doubleclue.dcem.recruiting.gui;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
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
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.utils.typedetector.DcemMediaType;
import com.doubleclue.dcem.core.utils.typedetector.FileUploadDetector;
import com.doubleclue.dcem.recruiting.entities.RecruiterEntity;
import com.doubleclue.dcem.recruiting.entities.enums.EntityType;
import com.doubleclue.dcem.recruiting.logic.RecruiterLogic;
import com.doubleclue.dcem.recruiting.logic.RecruitingConstants;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;
import com.doubleclue.dcem.recruiting.logic.RecruitingPdfLogic;
import com.doubleclue.dcem.recruiting.logic.RecruitingUtils;

@SessionScoped
@Named("recruiterDialog")
public class RecruiterDialog extends DcemDialog implements Serializable {

	private static final long serialVersionUID = 1353597066238277448L;
	private static Logger logger = LogManager.getLogger(RecruiterDialog.class);
	private static final String RECRUITER_DATA_ZIP = "RecruiterFiles.zip";

	@Inject
	private DcemApplicationBean applicationBean;

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private RecruitingPdfLogic recruitingPdfLogic;

	@Inject
	private CloudSafeLogic cloudSafeLogic;

	@Inject
	private RecruiterLogic recruiterLogic;

	@Inject
	private RecruitingModule recruitingModule;

	private RecruiterEntity recruiterEntity;
	private ResourceBundle resourceBundle;

	private String contractInfo;
	private boolean multipleFiles;

	private CloudSafeEntity cloudSafeRoot;
	private List<CloudSafeEntity> selectedFiles;
	private List<DcemUploadFile> uploadedFiles;

	private List<CloudSafeEntity> displayFiles;
	private List<CloudSafeEntity> deletedFiles;

	private String fileInfo;
	private CloudSafeEntity filePreview;
	private boolean pdfFile;

	private StreamedContent downloadFile;

	@PostConstruct
	public void init() {
		resourceBundle = JsfUtils.getBundle(RecruitingModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	@Override
	public boolean actionOk() throws Exception {
		recruiterEntity.setContractInformation(contractInfo);
		try {
			recruiterLogic.addOrUpdateRecruiterWithFiles(recruiterEntity, getAutoViewAction().getDcemAction(), uploadedFiles, deletedFiles);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "recruiterDialog.error.unableToAddOrUpdate");
			logger.error("Unable to add or update recruiter from " + recruiterEntity.getCompanyName(), e);
			return false;
		}
		return true;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) {
		String action = this.getAutoViewAction().getDcemAction().getAction();
		if (action.equals(DcemConstants.ACTION_DELETE)) {
			return;
		}
		deletedFiles = new ArrayList<CloudSafeEntity>();
		recruiterEntity = (RecruiterEntity) this.getActionObject();
		if (action.equals(DcemConstants.ACTION_EDIT) || action.equals(DcemConstants.ACTION_COPY)) {
			try {
				recruiterEntity = recruiterLogic.getFullRecruiter(recruiterEntity);
			} catch (Exception e) {
				JsfUtils.addErrorMessage(resourceBundle, "recruiterDialog.error.unableToRetrieveRecruiter");
				logger.error("Unable to retrieve details of recruiter from " + recruiterEntity.getCompanyName(), e);
			}
		}
		if (action.equals(DcemConstants.ACTION_ADD)) {
			recruiterEntity.setCountry(recruitingModule.getModulePreferences().getCountry());
		}
		if (action.equals(DcemConstants.ACTION_COPY)) {
			recruiterLogic.detachRecruiter(recruiterEntity);
			recruiterEntity.setId(null);
			recruiterEntity.setFiles(null);
		}
		contractInfo = recruiterEntity.getContractInformation();
		uploadedFiles = new ArrayList<DcemUploadFile>();
		displayFiles = recruiterEntity.getFiles() != null ? recruiterEntity.getFiles().stream().collect(Collectors.toList()) : new ArrayList<CloudSafeEntity>();
		selectedFiles = new ArrayList<CloudSafeEntity>();
		filePreview = null;
		pdfFile = false;
	}

	@Override
	public void actionConfirm() throws Exception {
		List<Object> recruitersAsObjects = this.autoViewBean.getSelectedItems();
		List<RecruiterEntity> recruiters = recruitersAsObjects.stream().map(obj -> (RecruiterEntity) obj).toList();
		recruiterLogic.deleteRecruiter(recruiters);
	}

	@Override
	public void leavingDialog() {
		recruiterEntity = null;
		contractInfo = null;
		displayFiles = null;
		selectedFiles = null;
		deletedFiles = null;
		recruitingPdfLogic.deleteDcemUploadFile(uploadedFiles);
		uploadedFiles = null;
		fileInfo = null;
		filePreview = null;
		pdfFile = false;
	}

	@Override
	public String getHeight() {
		return "75vh";
	}

	@Override
	public String getWidth() {
		return "75vw";
	}

	public String toUploadFile(CloudSafeEntity cloudSafeEntity) {
		if (cloudSafeEntity.getId() == null) {
			return JsfUtils.getStringSafely(resourceBundle, "candidateDialog.toUpload");
		} else {
			return JsfUtils.getStringSafely(resourceBundle, "candidateDialog.uploaded");
		}
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
				recruiterEntity.getFiles().remove(cloudSafeEntity);
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
		try (OutputStream output = JsfUtils.getDownloadFileOutputStream("application/zip", RECRUITER_DATA_ZIP);
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
			logger.warn("Unable to download archive of selected files " + RECRUITER_DATA_ZIP, e);
			FacesContext.getCurrentInstance().responseComplete();
		}
	}

	public void handleFileUpload(FileUploadEvent event) {
		if (displayFiles.size() > RecruitingConstants.MAXIMUM_FILE_NUMBER) {
			JsfUtils.addErrorMessage(resourceBundle, "error.fileNumberExceeded", String.valueOf(RecruitingConstants.MAXIMUM_FILE_NUMBER));
			return;
		}
		String fileName = event.getFile().getFileName();
		if(fileName.contains(",")) {
			JsfUtils.addErrorMessage(resourceBundle, "error.fileNameCannotContainComma", fileName);
			return;
		}
		for (CloudSafeEntity cloudSafeEntity : displayFiles) {
			if (cloudSafeEntity.getName().equals(fileName)) {
				JsfUtils.addErrorMessage(resourceBundle, "error.fileNameAlreadyExists", fileName);
				return;
			}
			if (recruiterEntity.getId() != null) {
				String fileNameForDB = RecruitingUtils.createFileNameForDB(fileName, recruiterEntity, EntityType.Recruiter);
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
			JsfUtils.addErrorMessage("Unexcpected Error! " + e.toString());
			return;
		}
		CloudSafeEntity cloudSafeEntity = new CloudSafeEntity(CloudSafeOwner.GLOBAL, (DcemUser) null, (DeviceEntity) null, fileName,
				(LocalDateTime) null, CloudSafeOptions.ENC.name(), false, cloudSafeRoot);
		cloudSafeEntity.setLength(event.getFile().getSize());
		displayFiles.add(cloudSafeEntity);
	}

	public void setMultipleFiles() {
		if (selectedFiles.size() <= 1) {
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
			JsfUtils.addErrorMessage(resourceBundle, "candidateDialog.selectOneFile");
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

	public String getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(String fileInfo) {
		this.fileInfo = fileInfo;
	}

	public RecruiterEntity getRecruiterEntity() {
		return recruiterEntity;
	}

	public void setRecruiterEntity(RecruiterEntity recruiterEntity) {
		this.recruiterEntity = recruiterEntity;
	}

	public List<SelectItem> getAvailableCountries() {
		return applicationBean.getAvailableCountries(operatorSessionBean.getLocale());
	}

	public String getContractInfo() {
		return contractInfo;
	}

	public void setContractInfo(String contractInfo) {
		this.contractInfo = contractInfo;
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
	
	public void setPdfFile(boolean pdfFile) {
		this.pdfFile = pdfFile;
	}

	public boolean isPdfFile() {
		return pdfFile;
	}

}
