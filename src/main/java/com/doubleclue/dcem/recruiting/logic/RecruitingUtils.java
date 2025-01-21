package com.doubleclue.dcem.recruiting.logic;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.jsoup.Jsoup;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.BranchLocation;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.DbResourceBundle;
import com.doubleclue.dcem.core.logic.GenderEnum;
import com.doubleclue.dcem.recruiting.entities.CandidateStatusEntity;
import com.doubleclue.dcem.recruiting.entities.CareerLevelEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyStatusEntity;
import com.doubleclue.dcem.recruiting.entities.enums.EntityType;
import com.doubleclue.dcem.recruiting.entities.enums.Nationality;
import com.doubleclue.dcem.recruiting.entities.enums.VacancyPriority;

public class RecruitingUtils {

	public static String createFileNameForDB(String fileName, EntityInterface entity, EntityType entityType) {
		int index = fileName.lastIndexOf('.');
		String ext = fileName.substring(index, fileName.length());
		return fileName.substring(0, index) + "-" + entityType.name() + "ID-" + entity.getId() + ext;
	}

	public static boolean invalidTextEditorContent(String text) throws Exception {
		if (text == null) {
			return true;
		}
		String zeroWidthNoBreakSpace = new String("\ufeff".getBytes("UTF-16"), "UTF-16"); // Primefaces gives a weird empty string...
		String htmlReducedText = Jsoup.parse(text).text().trim();
		htmlReducedText = htmlReducedText.replace(zeroWidthNoBreakSpace, "");
		return htmlReducedText.isBlank();
	}

	public static String compareFiles(List<CloudSafeEntity> oldFiles, List<CloudSafeEntity> newFiles) {
		oldFiles = oldFiles != null ? oldFiles : new ArrayList<CloudSafeEntity>();
		newFiles = newFiles != null ? newFiles : new ArrayList<CloudSafeEntity>();
		StringBuilder sb = new StringBuilder();
		for (CloudSafeEntity oldFile : oldFiles) {
			boolean wasRemoved = true;
			for (CloudSafeEntity newFile : newFiles) {
				if (newFile.equals(oldFile)) {
					wasRemoved = false;
					if (newFile.getLastModified().equals(oldFile.getLastModified()) == false) {
						sb.append(newFile.getName() + " updated; ");
					}
					break;
				}
			}
			if (wasRemoved) {
				sb.append(oldFile.getName() + " removed; ");
			}
		}
		for (CloudSafeEntity newFile : newFiles) {
			boolean wasAdded = true;
			for (CloudSafeEntity oldFile : oldFiles) {
				if (newFile.equals(oldFile)) {
					wasAdded = false;
					break;
				}
			}
			if (wasAdded) {
				sb.append(newFile.getName() + " added; ");
			}
		}
		if (sb.isEmpty() == false) {
			sb.insert(0, "Files: ");
		}
		return sb.toString();
	}
	
	public static String getCareerLevelNameFromTextResource(CareerLevelEntity careerLevel, String locale) {
		return getCareerLevelNameFromTextResource(careerLevel, new Locale(locale));
	}

	public static String getCareerLevelNameFromTextResource(CareerLevelEntity careerLevel, Locale locale) {
		DbResourceBundle dbResourceBundle = DbResourceBundle.getDbResourceBundle(locale);
		String textResource = RecruitingConstants.CAREERLEVEL_TEXTRESOURCE + careerLevel.getCareerLevel();
		String dbString = dbResourceBundle.getString(textResource);
		String result;
		if (textResource.equals(dbString)) {
			result = careerLevel.getCareerLevel();
		} else {
			result = dbString;
		}
		return result;
	}
	
	public static String getCandidateStatusNameFromTextResource(CandidateStatusEntity candidateStatus, String locale) {
		return getCandidateStatusNameFromTextResource(candidateStatus, new Locale(locale));
	}

	public static String getCandidateStatusNameFromTextResource(CandidateStatusEntity candidateStatus, Locale locale) {
		DbResourceBundle dbResourceBundle = DbResourceBundle.getDbResourceBundle(locale);
		String textResource = RecruitingConstants.CANDIDATE_STATUS_TEXTRESOURCE + candidateStatus.getStatus();
		String dbString = dbResourceBundle.getString(textResource);
		String result;
		if (textResource.equals(dbString)) {
			result = candidateStatus.getStatus();
		} else {
			result = dbString;
		}
		return result;
	}
	
	public static String getVacancyStatusNameFromTextResource(VacancyStatusEntity vacancyStatus, String locale) {
		return getVacancyStatusNameFromTextResource(vacancyStatus, new Locale(locale));
	}

	public static String getVacancyStatusNameFromTextResource(VacancyStatusEntity vacancyStatus, Locale locale) {
		DbResourceBundle dbResourceBundle = DbResourceBundle.getDbResourceBundle(locale);
		String textResource = RecruitingConstants.VACANCY_STATUS_TEXTRESOURCE + vacancyStatus.getStatus();
		String dbString = dbResourceBundle.getString(textResource);
		String result;
		if (textResource.equals(dbString)) {
			result = vacancyStatus.getStatus();
		} else {
			result = dbString;
		}
		return result;
	}

	
	public static String getLocalizedGender(String locale, GenderEnum gender) {
		ResourceBundle dcemResourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, new Locale(locale));
		return JsfUtils.getStringSafely(dcemResourceBundle, gender.name());
	}

	public static String getLocalizedCitizenship(String language, String citizenship) {
		if (citizenship == null) {
			return "";
		}
		for (Nationality nationality : Nationality.values()) {
			if (nationality.getCountryCode().equals(language)) {
				return nationality.toString();
			}
		}
		return "";
	}

	public static String getLocalizedCountry(String language, String country) {
		if (country == null) {
			return "";
		}
		return new Locale("", country).getDisplayCountry(new Locale(language));
	}

	public static List<SelectItem> getAvailableCurrencies(Locale locale) {
		List<SelectItem> items = new ArrayList<SelectItem>();
		Set<Currency> currencies = Currency.getAvailableCurrencies();
		for (Currency currency : currencies) {
			String localizedDisplayName = currency.getDisplayName(locale);
			String currencyCode = currency.getCurrencyCode();
			String label = String.format("%s (%s)", currencyCode, localizedDisplayName);
			items.add(new SelectItem(currency.getCurrencyCode(), label));
		}
		Comparator<SelectItem> comparator = new Comparator<SelectItem>() {
			@Override
			public int compare(SelectItem s1, SelectItem s2) {
				return (s1.getLabel()).compareTo(s2.getLabel());
			}
		};
		Collections.sort(items, comparator);
		return items;
	}
	
	public static String[] getAvailableCurrenciesAsStringArray(Locale locale) {
		List<String> currencyNames = new ArrayList<String>();
		Set<Currency> currencies = Currency.getAvailableCurrencies();
		for (Currency currency : currencies) {
			currencyNames.add(currency.getCurrencyCode());
		}
		Comparator<String> comparator = new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return (s1.compareTo(s2));
			}
		};
		Collections.sort(currencyNames, comparator);
		return currencyNames.toArray(new String[currencyNames.size()]);
	}

	public static String formatCurrency(String language, String currencyCode, Number number, int fractionDigits) {
		return formatCurrency(new Locale(language), currencyCode, number, fractionDigits);
	}

	public static String formatCurrency(Locale locale, String currencyCode, Number number, int fractionDigits) {
		NumberFormat numberFormatter = NumberFormat.getNumberInstance(locale);
		numberFormatter.setMaximumFractionDigits(fractionDigits);
		return numberFormatter.format(number) + " " + currencyCode;
	}

	public static String getLocalizedPriority(String language, VacancyPriority priority) {
		if (priority == null) {
			return "";
		}
		ResourceBundle recruitingResourceBundle = JsfUtils.getBundle(RecruitingModule.RESOURCE_NAME, new Locale(language));
		return JsfUtils.getStringSafely(recruitingResourceBundle, "vacancyPriority." + priority.name());
	}
	
	public static String formatAddress(Locale locale, BranchLocation branchLocation) {
		Locale countryLocale = new Locale("", branchLocation.getCountry());
		StringBuilder sb = new StringBuilder();
		sb.append(countryLocale.getDisplayCountry(locale) + " - " + branchLocation.getCity());
		if (branchLocation.getZipCode() != null) {
			sb.append(" (" + branchLocation.getZipCode() + ")");
		}
		if (branchLocation.getStreet() != null) {
			sb.append(", " + branchLocation.getStreet());
			if (branchLocation.getStreetNumber() != null) {
				sb.append(" " + branchLocation.getStreetNumber());
			}
		}
		return sb.toString();
	}
}
