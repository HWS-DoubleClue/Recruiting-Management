package com.doubleclue.dcem.recruiting.logic;

import java.util.Currency;
import java.util.Locale;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;

@SuppressWarnings("serial")
@XmlType
@XmlRootElement(name = "recruitingPreferences")
public class RecruitingPreferences extends ModulePreferences {
	
	@DcemGui(separator = "Default settings", style = "width: 200px")
	String country = Locale.GERMANY.getCountry();

	@DcemGui(style = "width: 200px")
	String nationality = Locale.GERMANY.getCountry();

	@DcemGui(style = "width: 200px")
	String currency = Currency.getInstance("EUR").getCurrencyCode();
	
	@DcemGui
	private boolean disableEmailNotifications;

	@DcemGui(style = "width: 30em", separator = "DCEM settings", help = "Manual URL - must start with 'http'. If the manual comes in multiple languages, end URL with a '_' as the language identifier and .pdf extention will be added automatically.")
	private String manualsLink = "https://doubleclue.com/files/DC_Recruiting_Manual_";

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getManualsLink() {
		return manualsLink;
	}

	public void setManualsLink(String manualsLink) {
		this.manualsLink = manualsLink;
	}

	public boolean isDisableEmailNotifications() {
		return disableEmailNotifications;
	}

	public void setDisableEmailNotifications(boolean disableEmailNotifications) {
		this.disableEmailNotifications = disableEmailNotifications;
	}
}
