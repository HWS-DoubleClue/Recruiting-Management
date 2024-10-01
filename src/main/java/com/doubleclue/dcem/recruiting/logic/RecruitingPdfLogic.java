package com.doubleclue.dcem.recruiting.logic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.util.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.CloudSafeUploadFile;
import com.doubleclue.dcem.core.as.DcemUploadFile;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import freemarker.template.Template;

@ApplicationScoped
@Named("recruitingPdfLogic")
public class RecruitingPdfLogic {

	private static Logger logger = LogManager.getLogger(RecruitingPdfLogic.class);

	@Inject
	private CloudSafeLogic cloudSafeLogic;

	@Inject
	private TemplateLogic templateLogic;

	@Inject
	private DcemApplicationBean dcemApplicationBean;

	public String getTemplateAsHtml(Object dataModel, String templateName, SupportedLanguage language) throws Exception {
		DcemTemplate dcemTemplate = templateLogic.getTemplateByNameLanguage(templateName, language);
		if (dcemTemplate == null) {
			throw new DcemException(DcemErrorCodes.NO_TEMPLATE_FOUND, templateName);
		}
		StringWriter stringWriter = new StringWriter();
		Template template = dcemApplicationBean.getTemplateFromConfig(dcemTemplate);
		template.process(dataModel, stringWriter);
		return stringWriter.toString();
	}

	public File exportPdf(String htmlData, String fileName) throws Exception {
		File tempFile = null;
		try (FileOutputStream os = new FileOutputStream(tempFile = File.createTempFile("dcem-", "-pdfFile"))) {
			Document doc = Jsoup.parse(htmlData, fileName);
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();
			doc = Jsoup.parse(htmlData);
			org.w3c.dom.Document dom = new W3CDom().fromJsoup(doc);
			builder.withW3cDocument(dom, null);
			builder.toStream(os);
			builder.run();
			return tempFile;
		}
	}

	public void zipCloudSafeEntities(ZipOutputStream zipOutputStream, List<CloudSafeUploadFile> cloudSafeUploadFiles) throws Exception {
		for (CloudSafeUploadFile cloudSafeUploadFile : cloudSafeUploadFiles) {
			zipFoldersOrFiles(zipOutputStream, cloudSafeUploadFile);
		}
	}

	private InputStream chooseInputStream(CloudSafeUploadFile cloudSafeUploadFile) throws Exception {
		if (cloudSafeUploadFile.getCloudSafeEntity() == null || cloudSafeUploadFile.getCloudSafeEntity().getId() == null) {
			return new FileInputStream(cloudSafeUploadFile.getFile());
		} else {
			return cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeUploadFile.getCloudSafeEntity(), null, null);
		}
	}

	public void zipFoldersOrFiles(ZipOutputStream zipOutputStream, CloudSafeUploadFile cloudSafeUploadFile) throws Exception {
		zipOutputStream.setLevel(Deflater.BEST_SPEED);
		int length = -1;
		byte[] buffer = new byte[1024 * 16];
		try (InputStream inputStream = chooseInputStream(cloudSafeUploadFile); BufferedInputStream bis = new BufferedInputStream(inputStream);) {
			String fileName;
			if (cloudSafeUploadFile.getCloudSafeEntity() == null || cloudSafeUploadFile.getCloudSafeEntity().getId() == null) {
				fileName = cloudSafeUploadFile.getFileName();
			} else {
				fileName = cloudSafeUploadFile.getCloudSafeEntity().getName();
			}
			ZipEntry zipEntry = new ZipEntry(fileName);
			zipOutputStream.putNextEntry(zipEntry);
			while ((length = bis.read(buffer)) != -1) {
				zipOutputStream.write(buffer, 0, length);
			}
		}
	}

	public File exportHtmlWithFilesToPdf(String htmlData, String fileName, List<CloudSafeEntity> cloudSafeEntities) throws Exception {
		List<File> tempFiles = new ArrayList<>();
		try {
			File candidateMainPdfTemp = exportPdf(htmlData, fileName);
			tempFiles.add(candidateMainPdfTemp);
			for (CloudSafeEntity cloudSafeEntity : cloudSafeEntities) {
				tempFiles.add(exportCloudSafeAsPdf(cloudSafeEntity));
			}
			File mergedFile = mergePdfFilesToSinglePdf(tempFiles);
			return mergedFile;
		} finally {
			deleteTempFiles(tempFiles);
		}
	}

	private File mergePdfFilesToSinglePdf(List<File> files) throws Exception {
		PDFMergerUtility merger = new PDFMergerUtility();
		for (File file : files) {
			merger.addSource(file);
		}
		File mergedFile = File.createTempFile("dcem-", ".pdf");
		merger.setDestinationFileName(mergedFile.getAbsolutePath());
		merger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
		return mergedFile;
	}

	public File exportCloudSafeAsPdf(CloudSafeEntity cloudSafeEntity) throws Exception {
		String extension = cloudSafeEntity.getName().substring(cloudSafeEntity.getName().lastIndexOf('.') + 1);
		if (extension.equalsIgnoreCase("pdf") == true) {
			return exportPdfFileAsPdf(cloudSafeEntity);
		} else if (isImage(extension)) {
			return exportImageFileAsPdf(cloudSafeEntity);
		} else {
			throw new Exception("Unsupported file format when converting files to pdf: " + extension);
		}
	}

	private boolean isImage(String extension) {
		return extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("png");
	}

	public File exportPdfFileAsPdf(CloudSafeEntity cloudSafeEntity) throws Exception {
		try (InputStream input = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, null, null)) {
			File attachmentTempFile = File.createTempFile(cloudSafeEntity.getName() + "-", ".pdf");
			FileUtils.copyInputStreamToFile(input, attachmentTempFile);
			return attachmentTempFile;
		}
	}

	public File exportImageFileAsPdf(CloudSafeEntity cloudSafeEntity) throws Exception {
		return exportImageFileAsPdf(cloudSafeEntity, 36f); // use 36 as standard margin

	}

	public File exportImageFileAsPdf(CloudSafeEntity cloudSafeEntity, float margin) throws Exception {
		try (InputStream input = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, null, null); PDDocument document = new PDDocument()) {
			PDPage page = new PDPage(PDRectangle.A4);
			document.addPage(page);
			PDImageXObject img = PDImageXObject.createFromByteArray(document, IOUtils.toByteArray(input), cloudSafeEntity.getName());
			try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
				float pageContentWidth = page.getMediaBox().getWidth() - 2 * margin;
				float pageContentHeight = page.getMediaBox().getHeight() - 2 * margin;
				float scale = Math.min(pageContentWidth / img.getWidth(), pageContentHeight / img.getHeight());
				scale = Math.min(1, scale);
				contentStream.drawImage(img, margin, page.getMediaBox().getHeight() - margin - img.getHeight() * scale, img.getWidth() * scale,
						img.getHeight() * scale);
			}
			File attachmentTempFile = File.createTempFile(cloudSafeEntity.getName() + "-", ".pdf");
			document.save(attachmentTempFile);
			return attachmentTempFile;
		}
	}

	public void deleteTempFiles(List<File> tempFiles) {
		for (File tempFile : tempFiles) {
			deleteTempFile(tempFile);
		}
	}

	public void deleteTempFile(File tempFile) {
		if (tempFile != null && tempFile.exists()) {
			try {
				if (tempFile.delete() == false) {
					logger.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
				}
			} catch (Exception e) {
				logger.warn("Unable to delete temporary file", e);
			}
		}
	}

	public void deleteDcemUploadFile(List<DcemUploadFile> dcemUploadFiles) {
		if (dcemUploadFiles != null && dcemUploadFiles.isEmpty() == false) {
			for (DcemUploadFile dcemUploadFile : dcemUploadFiles) {
				try {
					dcemUploadFile.file.delete();
				} catch (Exception e) {
					logger.warn("Unable to delete file " + dcemUploadFile.file.getName() + " from file cache ", e.getMessage());
				}
			}
		}

	}

}