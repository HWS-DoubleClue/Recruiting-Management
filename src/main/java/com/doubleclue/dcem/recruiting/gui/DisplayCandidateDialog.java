package com.doubleclue.dcem.recruiting.gui;

import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.logic.CloudSafeUploadFile;
import com.doubleclue.dcem.core.entities.DepartmentEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.recruiting.entities.CandidateEntity;
import com.doubleclue.dcem.recruiting.logic.CandidateLogic;
import com.doubleclue.dcem.recruiting.logic.RecruitingConstants;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;
import com.doubleclue.dcem.recruiting.logic.RecruitingOperatorDepartments;
import com.doubleclue.dcem.recruiting.logic.RecruitingPdfLogic;
import com.google.common.net.MediaType;

@SessionScoped
@Named("displayCandidateDialog")
public class DisplayCandidateDialog extends DcemDialog implements Serializable {

	private static final long serialVersionUID = 5686978295758558811L;
	private static Logger logger = LogManager.getLogger(DisplayCandidateDialog.class);
	private static final String CANDIDATES_DATA_ZIP = "CandidateFiles.zip";

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private CandidateLogic candidateLogic;

	@Inject
	private RecruitingPdfLogic recruitingPdfLogic;

	@Inject
	private RecruitingOperatorDepartments recruitingOperatorDepartments;

	private ResourceBundle resourceBundle;
	private CandidateEntity candidateEntity;
	private String htmlData;

	@PostConstruct
	public void init() {
		resourceBundle = JsfUtils.getBundle(RecruitingModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		String action = this.getAutoViewAction().getDcemAction().getAction();
		candidateEntity = candidateLogic.getFullCandidate((CandidateEntity) this.getActionObject());

		boolean hasActionPermission = operatorSessionBean.isPermission(autoViewAction.getDcemAction());
		List<DepartmentEntity> departmentsAppliedFor = candidateLogic.getDepartmentsAppliedFor(candidateEntity);
		boolean isHeadOfAnyDepartmentAppliedFor = recruitingOperatorDepartments.isHeadOfAnyDepartmentIn(departmentsAppliedFor);
		boolean isWatcher = candidateEntity.getWatchers().contains(operatorSessionBean.getDcemUser());

		if (hasActionPermission) {
			candidateEntity.setRestrictedViewRights(false);
		} else if (isHeadOfAnyDepartmentAppliedFor || isWatcher) {
			candidateEntity.setRestrictedViewRights(true);
		} else {
			throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS, action);
		}

		try {
			htmlData = recruitingPdfLogic.getTemplateAsHtml(candidateEntity, RecruitingConstants.CANDIDATE_DISPLAY_TEMPLATE,
					operatorSessionBean.getDcemUser().getLanguage());
		} catch (Exception e) {
			logger.error("Failed to write candidate {} to template {}", candidateEntity.toString(), RecruitingConstants.VACANCY_DISPLAY_TEMPLATE, e);
			throw new DcemException(DcemErrorCodes.TEMPLATE_BAD_FORMAT, "Cannot parse template to html", e);
		}
	}

	@Override
	public void leavingDialog() {
		candidateEntity = null;
		htmlData = null;
	}

	private CloudSafeUploadFile createCandidateMainPdf() throws Exception {
		String fileName = "Candidate_" + candidateEntity.getId() + "_" + operatorSessionBean.getDcemUser().getLanguage() + ".pdf";
		File tempFile = recruitingPdfLogic.exportPdf(htmlData, fileName);
		return new CloudSafeUploadFile(fileName, tempFile, null);
	}

	public void actionExportPdf() {
		File tempFile = null;
		try {
			CloudSafeUploadFile candidatePdf = createCandidateMainPdf();
			tempFile = candidatePdf.getFile();
			JsfUtils.downloadFile(MediaType.PDF.toString(), candidatePdf.getFileName(), tempFile);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "error.downloadPdf");
			logger.error("Unable to download PDF", e);
		} finally {
			recruitingPdfLogic.deleteTempFile(tempFile);
		}
	}

	public void actionExportZip() {
		File tempFile = null;
		try (OutputStream output = JsfUtils.getDownloadFileOutputStream("application/zip", CANDIDATES_DATA_ZIP);
				ZipOutputStream zipOutputStream = new ZipOutputStream(output);) {
			List<CloudSafeUploadFile> cloudSafeUploadFiles = new ArrayList<CloudSafeUploadFile>();
			CloudSafeUploadFile candidatePdf = createCandidateMainPdf();
			tempFile = candidatePdf.getFile();
			cloudSafeUploadFiles.add(candidatePdf);
			for (CloudSafeEntity cloudSafe : candidateEntity.getFiles()) {
				cloudSafeUploadFiles.add(new CloudSafeUploadFile(cloudSafe.getName(), null, cloudSafe));
			}
			recruitingPdfLogic.zipCloudSafeEntities(zipOutputStream, cloudSafeUploadFiles);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "candidateDialog.error.unableToDownloadZip");
			logger.error("Unable to download zipfile of candidate " + candidateEntity.getFullName(), e);
		} finally {
			recruitingPdfLogic.deleteTempFile(tempFile);
			FacesContext.getCurrentInstance().responseComplete();
		}
	}

	public void actionExportPdfWithDocs() {
		File mergedFile = null;
		try {
			String documentName = "Candidate_" + candidateEntity.getId() + "_" + operatorSessionBean.getDcemUser().getLanguage() + ".pdf";
			mergedFile = recruitingPdfLogic.exportHtmlWithFilesToPdf(htmlData, documentName, candidateEntity.getFiles());
			String fileName = candidateEntity.getFullName().replace(" ", "") + ".pdf";
			JsfUtils.downloadFile(MediaType.PDF.toString(), fileName, mergedFile);
		} catch (Exception e) {
			logger.error("Error exporting PDF for candidate " + candidateEntity.getFullName(), e);
			JsfUtils.addErrorMessage(resourceBundle, "displayCandidateDialog.error.unableToDownloadAllDocuments");
		} finally {
			recruitingPdfLogic.deleteTempFile(mergedFile);
		}
	}

	@Override
	public String getHeight() {
		return "80vh";
	}

	@Override
	public String getWidth() {
		return "65vw";
	}

	public String getHtmlData() {
		return htmlData;
	}

	public void setHtmlData(String htmlData) {
		this.htmlData = htmlData;
	}
}
