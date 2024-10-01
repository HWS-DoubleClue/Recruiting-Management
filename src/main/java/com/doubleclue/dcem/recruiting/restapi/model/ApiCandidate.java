package com.doubleclue.dcem.recruiting.restapi.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.SortedSet;

import com.doubleclue.dcem.core.logic.GenderEnum;
import com.doubleclue.dcem.recruiting.entities.CandidateStatusEntity;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;

public class ApiCandidate implements Serializable {

	private static final long serialVersionUID = -4905545261903660559L;

	private Integer id;

	private byte[] avatar;

	private String title;

	private String firstName;

	private String surname;

	private String jobTitle;

	private GenderEnum gender = GenderEnum.NotSpecified;

	private LocalDate dateOfBirth;

	private String email;

	private List<Integer> vacancyIds;

	private SortedSet<SkillsLevelEntity> skills;

	private LocalDate startingOn;

	private CandidateStatusEntity status;

	private String classification;

	private String phone;

	private LocalDate registeredOn;

	private String address;

	private String zipCode;

	private String city;

	private String countryState;

	private String country;

	private String citizenship;

	private String currency;

	private int salaryRequest;

	private String qualification;

	private String experience;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public GenderEnum getGender() {
		return gender;
	}

	public void setGender(GenderEnum gender) {
		this.gender = gender;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public SortedSet<SkillsLevelEntity> getSkills() {
		return skills;
	}

	public void setSkills(SortedSet<SkillsLevelEntity> skills) {
		this.skills = skills;
	}

	public LocalDate getStartingOn() {
		return startingOn;
	}

	public void setStartingOn(LocalDate startingOn) {
		this.startingOn = startingOn;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public LocalDate getRegisteredOn() {
		return registeredOn;
	}

	public void setRegisteredOn(LocalDate registeredOn) {
		this.registeredOn = registeredOn;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountryState() {
		return countryState;
	}

	public void setCountryState(String countryState) {
		this.countryState = countryState;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCitizenship() {
		return citizenship;
	}

	public void setCitizenship(String citizenship) {
		this.citizenship = citizenship;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public int getSalaryRequest() {
		return salaryRequest;
	}

	public void setSalaryRequest(int salaryRequest) {
		this.salaryRequest = salaryRequest;
	}

	public String getQualification() {
		return qualification;
	}

	public void setQualification(String qualification) {
		this.qualification = qualification;
	}

	public String getExperience() {
		return experience;
	}

	public void setExperience(String experience) {
		this.experience = experience;
	}

	@Override
	public String toString() {
		return id + " - " + firstName + " " + surname;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Integer> getVacancyIds() {
		return vacancyIds;
	}

	public void setVacancyIds(List<Integer> vacancyIds) {
		this.vacancyIds = vacancyIds;
	}

	public CandidateStatusEntity getStatus() {
		return status;
	}

	public void setStatus(CandidateStatusEntity status) {
		this.status = status;
	}

}
