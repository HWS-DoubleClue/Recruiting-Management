package com.doubleclue.dcem.recruiting.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.Hibernate;
import org.hibernate.annotations.SortNatural;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.IPhoto;
import com.doubleclue.dcem.core.gui.validators.NotNullOrEmptyString;
import com.doubleclue.dcem.core.jersey.SerializerEntities;
import com.doubleclue.dcem.core.jersey.SerializerEntity;
import com.doubleclue.dcem.core.jpa.VariableType;
import com.doubleclue.dcem.core.logic.GenderEnum;
import com.doubleclue.dcem.core.utils.compare.DcemCompare;
import com.doubleclue.dcem.recruiting.logic.RecruitingUtils;
import com.doubleclue.dcem.recruiting.restapi.DeserializerClassification;
import com.doubleclue.dcem.recruiting.restapi.DeserializerVacancies;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@NamedEntityGraphs({ @NamedEntityGraph(name = CandidateEntity.GRAPH_FILES, attributeNodes = { @NamedAttributeNode(value = "files") }),
		@NamedEntityGraph(name = CandidateEntity.GRAPH_FEEDBACK, attributeNodes = { @NamedAttributeNode(value = "feedback") }),
		@NamedEntityGraph(name = CandidateEntity.GRAPH_VACANCIES, attributeNodes = { @NamedAttributeNode(value = "vacancies") }),
		@NamedEntityGraph(name = CandidateEntity.GRAPH_WATCHERS, attributeNodes = { @NamedAttributeNode(value = "watchers") }),
		@NamedEntityGraph(name = CandidateEntity.GRAPH_SKILLS, attributeNodes = { @NamedAttributeNode(value = "skills") }) })

@Entity
@Table(name = "recruiting_candidate")
@NamedQueries({
		@NamedQuery(name = CandidateEntity.GET_ALL_CANDIDATES_WITH_DCEMUSER_REFERENCES, query = "SELECT c FROM CandidateEntity c WHERE recommendedBy = ?1 OR modifiedBy = ?1 OR ?1 MEMBER OF watchers"), })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class CandidateEntity extends EntityInterface implements IPhoto, Serializable, Comparable<CandidateEntity> {
	private static final long serialVersionUID = -4905545261903660559L;

	public static final String GET_ALL_CANDIDATES_WITH_DCEMUSER_REFERENCES = "CadidateEntity.getAllCandidatesWithDcemuserReferences";

	public static final String GRAPH_FILES = "CandidateEntity.files";
	public static final String GRAPH_FEEDBACK = "CandidateEntity.feedback";
	public static final String GRAPH_VACANCIES = "CandidateEntity.vacancies";
	public static final String GRAPH_WATCHERS = "CandidateEntity.watchers";
	public static final String GRAPH_SKILLS = "CandidateEntity.skills";

	@Id
	@Column(name = "candidate_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@DcemGui
	private Integer id;

	@DcemGui(variableType = VariableType.IMAGE)
	@Column(length = 1024 * 16, name = "photo")
	private byte[] photo;

	@DcemGui(restricted = false)
	@Column(nullable = false, name = "first_name", length = 255)
	@NotNullOrEmptyString
	private String firstName;

	@Column(nullable = false, name = "surname", length = 255)
	@DcemGui(restricted = true)
	@NotNullOrEmptyString
	private String surname;

	@Column(length = 255, name = "title")
	private String title;

	@Transient
	private String fullName;

	@Column(nullable = false, length = 255)
	@DcemGui(restricted = true)
	@NotNullOrEmptyString
	private String email;

	@DcemGui
	@Column(name = "job_title", length = 255)
	@NotNullOrEmptyString
	private String jobTitle;

	@DcemGui(subClass = "status")
	@ManyToOne
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_CLASS_CANDIDATE_STATUS"), name = "candidate_status", nullable = false)
	@NotNull
	private CandidateStatusEntity status;

	@DcemGui(visible = false, subClass = "displayName")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_REC_CANDIDATE"), name = "recommended_by")
	@JsonIgnore
	private DcemUser recommendedBy;

	@DcemGui
	@ManyToOne
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_CLASS_CANDIDATE"), name = "classification")
	@JsonSerialize(using = SerializerEntity.class)
	@JsonDeserialize(using = DeserializerClassification.class)
	private ClassificationEntity classification;

	@DcemCompare(withoutResult = true)
	@Column(length = 1024 * 1024, name = "remarks")
	@JsonIgnore
	private String remarks;

	@Column(name = "date_of_birth")
	@DcemGui(restricted = true)
	private LocalDate dateOfBirth;

	@DcemGui(visible = false, restricted = true)
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "gender", nullable = false)
	@NotNull
	private GenderEnum gender = GenderEnum.NotSpecified;

	@DcemGui(restricted = true, visible = false)
	@Column(length = 255, name = "citizenship")
	private String citizenship;

	@DcemGui(visible = false, restricted = true)
	@Column(name = "phone", length = 255)
	private String phone;

	@DcemGui(visible = false, restricted = true)
	@Column(name = "dc_address", length = 255)
	private String address;

	@DcemGui(visible = false, restricted = true)
	@Column(name = "zip_code", length = 15)
	private String zipCode;

	@DcemGui(visible = false, restricted = true)
	@Column(name = "city", length = 255)
	private String city;

	@DcemGui(visible = false, restricted = true)
	@Column(name = "country_state", length = 255)
	private String countryState;

	@DcemGui(visible = false)
	@Column(length = 255, name = "dc_country")
	private String country;

	@DcemGui
	@Column(length = 255, name = "school_graduation")
	private String schoolGraduation;

	@DcemGui
	@Column(length = 255, name = "professional_qualification")
	private String professionalQualification;

	@DcemGui
	@Column(name = "experience", length = 255)
	private String experience;

	// @DcemGui(subClass = "defaultLanguage", visible = false)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "recruiting_ref_vacancy_candidate", joinColumns = @JoinColumn(name = "candidate_id"), foreignKey = @ForeignKey(name = "FK_VACANCY_CANDIDATE"), inverseJoinColumns = @JoinColumn(name = "vac_id"), inverseForeignKey = @ForeignKey(name = "FK_CANDIDATE_VACANCY"))
	@SortNatural
	@JsonSerialize(using = SerializerEntities.class)
	@JsonDeserialize(using = DeserializerVacancies.class)
	private SortedSet<VacancyEntity> vacancies;

	@DcemGui(visible = false)
	@Column(name = "starting_on")
	private LocalDate startingOn;

	@DcemGui
	@Column(name = "currency", length = 15)
	private String currency;

	@DcemGui
	@Column(name = "salary_request")
	private Integer salaryRequest;

	@DcemGui
	@Column(length = 255, name = "period_of_notice")
	private String periodOfNotice;

	@DcemGui(visible = false)
	@Column(name = "dc_restricted")
	private boolean restricted = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_RECRUITER_CANDIDATE"), name = "recruiter_id")
	@JsonIgnore
	private RecruiterEntity recruiterEntity;

	@Column(length = 255, name = "referral")
	private String referral;

	// @DcemGui(visible = false)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "recruiting_ref_skills_candidates", joinColumns = @JoinColumn(name = "candidate_id"), foreignKey = @ForeignKey(name = "FK_SKILLS_CANDIDATE"), inverseJoinColumns = @JoinColumn(name = "skills_level_id"), inverseForeignKey = @ForeignKey(name = "FK_CANDIDATE_SKILLS"))
	@SortNatural
	private SortedSet<SkillsLevelEntity> skills;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "recruiting_ref_watchers_candidate", joinColumns = @JoinColumn(name = "candidate_id"), foreignKey = @ForeignKey(name = "FK_WATCHERS_CANDIDATE"), inverseJoinColumns = @JoinColumn(name = "user_id"), inverseForeignKey = @ForeignKey(name = "FK_CANDIDATE_WATCHERS"))
	@JsonIgnore
	private List<DcemUser> watchers;

	@DcemCompare(ignore = true)
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "recruiting_ref_candidate_file", joinColumns = @JoinColumn(name = "candidate_id"), foreignKey = @ForeignKey(name = "FK_FILES_CANDIDATE"), inverseJoinColumns = @JoinColumn(name = "file_id"), inverseForeignKey = @ForeignKey(name = "FK_CANDIDATE_FILES"))
	private List<CloudSafeEntity> files;

	@DcemCompare(ignore = true)
	@DcemGui(visible = false, subClass = "rating")
	@OneToMany(mappedBy = "candidate", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@SortNatural
	@JsonIgnore
	private SortedSet<FeedbackEntity> feedback;

	@DcemGui(variableType = VariableType.RATING)
	@Column(name = "ranking", nullable = false)
	private float ranking = 0;

	@DcemGui(visible = false)
	@Column(nullable = false, name = "registered_on", updatable = false)
	private LocalDate registeredOn;

	@DcemCompare(ignore = true)
	@DcemGui(visible = false)
	@Column(nullable = false, name = "last_modified_on")
	private LocalDateTime lastModifiedOn;

	@DcemCompare(ignore = true)
	@DcemGui(visible = false, subClass = "displayName")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_MODIFIED_CANDIDATE"), name = "modified_by")
	@JsonIgnore
	private DcemUser modifiedBy;

	@Transient // Used for displaying purposes in the template
	private boolean restrictedViewRights;

	public CandidateEntity() {

	}

	@Override
	public void setId(Number id) {
		if (id != null) {
			this.id = id.intValue();
		} else {
			this.id = null;
		}
	}

	@Override
	public Integer getId() {
		return id;
	}

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

	public String getFullName() {
		if (title == null || title.isEmpty()) {
			return firstName + " " + surname;
		}
		return title + " " + firstName + " " + surname;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
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

	public SortedSet<VacancyEntity> getVacancies() {
		return vacancies;
	}

	public void setVacancies(SortedSet<VacancyEntity> vacancies) {
		this.vacancies = vacancies;
	}

	public RecruiterEntity getRecruiterEntity() {
		return recruiterEntity;
	}

	public void setRecruiterEntity(RecruiterEntity recruiterEntity) {
		this.recruiterEntity = recruiterEntity;
	}

	public List<DcemUser> getWatchers() {
		return watchers != null ? watchers : new ArrayList<>();
	}

	public void setWatchers(List<DcemUser> watchers) {
		this.watchers = watchers;
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

	public LocalDate getRegisteredOn() {
		return registeredOn;
	}

	public void setRegisteredOn(LocalDate registeredOn) {
		this.registeredOn = registeredOn;
	}

	public float getRanking() {
		return ranking;
	}

	public void setRanking(float ranking) {
		this.ranking = ranking;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public ClassificationEntity getClassification() {
		return classification;
	}

	public void setClassification(ClassificationEntity classification) {
		this.classification = classification;
	}

	public DcemUser getRecommendedBy() {
		return recommendedBy;
	}

	public void setRecommendedBy(DcemUser recommendedBy) {
		this.recommendedBy = recommendedBy;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public LocalDateTime getLastModifiedOn() {
		return lastModifiedOn;
	}

	public void setLastModifiedOn(LocalDateTime lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
	}

	public DcemUser getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(DcemUser modifiedBy) {
		this.modifiedBy = modifiedBy;
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

	public SortedSet<FeedbackEntity> getFeedback() {
		return feedback;
	}

	public void setFeedback(SortedSet<FeedbackEntity> feedback) {
		this.feedback = feedback;
	}

	public List<CloudSafeEntity> getFiles() {
		return files;
	}

	public void setFiles(List<CloudSafeEntity> files) {
		this.files = files;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Integer getSalaryRequest() {
		return salaryRequest;
	}

	public void setSalaryRequest(Integer salaryRequest) {
		this.salaryRequest = salaryRequest;
	}

	public String getExperience() {
		return experience;
	}

	public void setExperience(String experience) {
		this.experience = experience;
	}

	@Override
	public boolean isRestricted() {
		return restricted;
	}

	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	@Override
	public String toString() {
		return id + " - " + firstName + " " + surname;
	}

	public String getPeriodOfNotice() {
		return periodOfNotice;
	}

	public void setPeriodOfNotice(String periodOfNotice) {
		this.periodOfNotice = periodOfNotice;
	}

	public String getSchoolGraduation() {
		return schoolGraduation;
	}

	public void setSchoolGraduation(String schoolGraduation) {
		this.schoolGraduation = schoolGraduation;
	}

	public String getProfessionalQualification() {
		return professionalQualification;
	}

	public void setProfessionalQualification(String professionalQualification) {
		this.professionalQualification = professionalQualification;
	}

	public CandidateStatusEntity getStatus() {
		return status;
	}

	public void setStatus(CandidateStatusEntity status) {
		this.status = status;
	}

	public String getReferral() {
		return referral;
	}

	public void setReferral(String referral) {
		this.referral = referral;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, firstName, surname);
	}

	@Override
	public int compareTo(CandidateEntity other) {
		int compareSurnameResult = Objects.compare(this.getSurname(), other.getSurname(), Comparator.nullsFirst(Comparator.naturalOrder()));
		if (compareSurnameResult != 0) {
			return compareSurnameResult;
		}
		int compareFirstNameResult = Objects.compare(this.getFirstName(), other.getFirstName(), Comparator.nullsFirst(Comparator.naturalOrder()));
		if (compareFirstNameResult != 0) {
			return compareFirstNameResult;
		}
		return Objects.compare(this.getId(), other.getId(), Comparator.nullsFirst(Comparator.naturalOrder()));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (Hibernate.getClass(this) != Hibernate.getClass(obj)) {
			return false;
		}
		return Objects.equals(id, ((CandidateEntity) obj).getId());
	}

	// Following methods are used for template export

	@JsonIgnore
	public boolean isRestrictedViewRights() {
		return restrictedViewRights;
	}

	@JsonIgnore
	public void setRestrictedViewRights(boolean restrictedViewRights) {
		this.restrictedViewRights = restrictedViewRights;
	}

	@JsonIgnore
	public String getFormattedFullName() {
		String surname_ = this.getSurname();
		if (restrictedViewRights && restricted) {
			surname_ = DcemConstants.RESTRICTED_REPLACEMENT;
		}
		if (title == null || title.isEmpty()) {
			return firstName + " " + surname_;
		}
		return title + " " + firstName + " " + surname_;
	}

	@JsonIgnore
	public String getFormattedEmail() {
		if (restrictedViewRights && restricted) {
			return DcemConstants.RESTRICTED_REPLACEMENT;
		}
		return email;
	}

	@JsonIgnore
	public String getFormattedDateOfBirth(String language) {
		if (restrictedViewRights && restricted) {
			return DcemConstants.RESTRICTED_REPLACEMENT;
		}
		return RecruitingUtils.formatDate(language, dateOfBirth);
	}

	@JsonIgnore
	public String getFormattedGender(String language) {
		if (restrictedViewRights && restricted) {
			return DcemConstants.RESTRICTED_REPLACEMENT;
		}
		return RecruitingUtils.getLocalizedGender(language, gender);
	}

	@JsonIgnore
	public String getFormattedAdress() {
		if (restrictedViewRights && restricted) {
			return DcemConstants.RESTRICTED_REPLACEMENT;
		}
		return address;
	}

	@JsonIgnore
	public String getFormattedZipAndCity() {
		if (restrictedViewRights && restricted) {
			return DcemConstants.RESTRICTED_REPLACEMENT;
		}
		if (zipCode == null || zipCode.isBlank()) {
			return city;
		}
		return zipCode + ", " + city;
	}

	@JsonIgnore
	public String getFormattedCountryState() {
		if (restrictedViewRights && restricted) {
			return DcemConstants.RESTRICTED_REPLACEMENT;
		}
		return countryState;
	}

	@JsonIgnore
	public String getFormattedCitizenship(String language) {
		if (restrictedViewRights && restricted) {
			return DcemConstants.RESTRICTED_REPLACEMENT;
		}
		return RecruitingUtils.getLocalizedCitizenship(language, citizenship);
	}

	@JsonIgnore
	public String getFormattedPhone() {
		if (restrictedViewRights && restricted) {
			return DcemConstants.RESTRICTED_REPLACEMENT;
		}
		return phone;
	}

	@JsonIgnore
	public String getFormattedStartingOn(String language) {
		return RecruitingUtils.formatDate(language, startingOn);
	}

	@JsonIgnore
	public String getLastModifiedOnFormatted(String language) {
		return RecruitingUtils.formatDateTime(language, lastModifiedOn);
	}

	@JsonIgnore
	public String getFormattedCountry(String language) {
		return RecruitingUtils.getLocalizedCountry(language, country);
	}

	@JsonIgnore
	public String getFormattedStatus(String language) {
		return RecruitingUtils.getCandidateStatusNameFromTextResource(status, language);
	}

	@JsonIgnore
	public String getFormattedSalary(String language) {
		if (salaryRequest == null) {
			return "";
		}
		return RecruitingUtils.formatCurrency(language, currency, salaryRequest, 0);
	}

	@JsonIgnore
	public String getFormattedRemarks() {
		if (remarks == null || remarks.equals("<p></p>")) {
			return "";
		}
		return remarks;
	}

	@JsonIgnore
	public String getPhotoData() {
		return Objects.isNull(photo) == true ? "" : Base64.getEncoder().encodeToString(photo);
	}

}
