package com.doubleclue.dcem.recruiting.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyLanguageEntity;
import com.doubleclue.dcem.recruiting.logic.RecruitingConstants;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;
import com.doubleclue.dcem.recruiting.logic.RecruitingPdfLogic;
import com.doubleclue.dcem.recruiting.logic.VacancyLogic;
import com.google.common.net.MediaType;

@SessionScoped
@Named("showVacancyDialog")
public class ShowVacancyDialog extends DcemDialog {

	private static final long serialVersionUID = 5461479046928899555L;

	// logger
	private static Logger logger = LogManager.getLogger(ShowVacancyDialog.class);

	// class injections

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	private VacancyLogic vacancyLogic;

	@Inject
	private RecruitingPdfLogic recruitingPdfLogic;

	// variables
	private ResourceBundle resourceBundle;
	private VacancyEntity vacancyEntity;
	private String selectedLanguage;
	private String htmlData;

	@PostConstruct
	public void init() {
		resourceBundle = JsfUtils.getBundle(RecruitingModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		try {
			vacancyEntity = vacancyLogic.getFullVacancy((VacancyEntity) this.getActionObject());
			selectedLanguage = SupportedLanguage.fromLocale(operatorSessionBean.getLocale()).name();
			htmlData = recruitingPdfLogic.getTemplateAsHtml(vacancyEntity, RecruitingConstants.VACANCY_DISPLAY_TEMPLATE,
					SupportedLanguage.valueOf(selectedLanguage));
		} catch (Exception e) {
			logger.error("Failed to write vacancy {} to template {}", vacancyEntity.toString(), RecruitingConstants.VACANCY_DISPLAY_TEMPLATE, e);
			throw new DcemException(DcemErrorCodes.TEMPLATE_BAD_FORMAT, "Cannot parse template to html", e);
		}
	}

	public void actionUpdateHtmlData() {
		try {
			htmlData = recruitingPdfLogic.getTemplateAsHtml(vacancyEntity, RecruitingConstants.VACANCY_DISPLAY_TEMPLATE,
					SupportedLanguage.valueOf(selectedLanguage));
		} catch (DcemException dcemExc) {
			if (dcemExc.getErrorCode().equals(DcemErrorCodes.NO_TEMPLATE_FOUND)) {
				JsfUtils.addErrorMessage(
						JsfUtils.getStringSafely(resourceBundle, "showVacancyDialog.error.notFound") + RecruitingConstants.VACANCY_DISPLAY_TEMPLATE);
				logger.error("Template could not be found: " + RecruitingConstants.VACANCY_DISPLAY_TEMPLATE, dcemExc);
			} else {
				JsfUtils.addErrorMessage(dcemExc.getLocalizedMessage());
				logger.error("Error occured when processing template to html", dcemExc);
			}
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "showVacancyDialog.error.writeError");
			logger.error("Failed to write vacancy {} to template {}", vacancyEntity.toString(), RecruitingConstants.VACANCY_DISPLAY_TEMPLATE, e);
		}
	}

	public void actionExportPdf() {
		File tempFile = null;
		try {
			String fileName = "Vacancy_" + vacancyEntity.getId() + "_" + SupportedLanguage.valueOf(selectedLanguage).getLocale() + ".pdf";
			tempFile = recruitingPdfLogic.exportPdf(htmlData, fileName);
			JsfUtils.downloadFile(MediaType.PDF.toString(), fileName, tempFile);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(resourceBundle, "error.downloadPdf");
			logger.error("Unable to download PDF", e);
		} finally {
			recruitingPdfLogic.deleteTempFile(tempFile);
		}
	}

	@Override
	public void leavingDialog() {
		vacancyEntity = null;
		htmlData = null;
	}

	public VacancyEntity getVacancyEntity() {
		return vacancyEntity;
	}

	public void setVacancyEntity(VacancyEntity vacancyEntity) {
		this.vacancyEntity = vacancyEntity;
	}

	public String getSelectedLanguage() {
		return selectedLanguage;
	}

	public void setSelectedLanguage(String selectedLanguage) {
		this.selectedLanguage = selectedLanguage;
	}

	public List<SelectItem> getLanguageList() {
		List<SelectItem> languageList = new ArrayList<SelectItem>();
		languageList.add(new SelectItem(vacancyEntity.getDefaultLanguage().name(), 
				getLocalizedLanguage(vacancyEntity.getDefaultLanguage().name()) + " (" + JsfUtils.getStringSafely(resourceBundle, "default") + ")"));
		if (vacancyEntity.getLanguages() != null) {
			for (VacancyLanguageEntity vacancyLanguage : vacancyEntity.getLanguages()) {
				languageList.add(new SelectItem(vacancyLanguage.getLanguage().name(), getLocalizedLanguage(vacancyLanguage.getLanguage().name())));
			}
		}
		return languageList;
	}

	public String getLocalizedLanguage(String languageName) {
		return JsfUtils.getStringSafely(resourceBundle, "supportedLanguage." + languageName);
	}

	@Override
	public String getHeight() {
		return "80vh";
	}

	@Override
	public String getWidth() {
		return "80vw";
	}

	public String getHtmlData() {
		return htmlData;
	}

	public void setHtmlData(String htmlData) {
		this.htmlData = htmlData;
	}
}
