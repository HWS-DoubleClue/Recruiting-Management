package com.doubleclue.dcem.recruiting.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;

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
import javax.validation.constraints.NotNull;

import org.hibernate.Hibernate;
import org.hibernate.annotations.SortNatural;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.entities.BranchLocation;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DepartmentEntity;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.gui.validators.NotNullOrEmptyString;
import com.doubleclue.dcem.core.jersey.SerializerEntities;
import com.doubleclue.dcem.core.jersey.SerializerEntity;
import com.doubleclue.dcem.core.jersey.SerializerToString;
import com.doubleclue.dcem.core.jpa.FilterOperator;
import com.doubleclue.dcem.core.utils.compare.DcemCompare;
import com.doubleclue.dcem.recruiting.entities.enums.VacancyPriority;
import com.doubleclue.dcem.recruiting.entities.enums.VacancyType;
import com.doubleclue.dcem.recruiting.logic.RecruitingConstants;
import com.doubleclue.dcem.recruiting.logic.RecruitingUtils;
import com.doubleclue.dcem.skills.entities.SkillsLevelEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@NamedEntityGraphs({
		@NamedEntityGraph(name = VacancyEntity.GRAPH_SKILLS, attributeNodes = { @NamedAttributeNode(value = RecruitingConstants.GRAPH_ATTRIBUTE_SKILLS) }),
		@NamedEntityGraph(name = VacancyEntity.GRAPH_WATCHERS, attributeNodes = { @NamedAttributeNode(value = RecruitingConstants.GRAPH_ATTRIBUTE_WATCHERS) }),
		@NamedEntityGraph(name = VacancyEntity.GRAPH_CANDIDATES, attributeNodes = {
				@NamedAttributeNode(value = RecruitingConstants.GRAPH_ATTRIBUTE_CANDIDATES) }),
		@NamedEntityGraph(name = VacancyEntity.GRAPH_LANGUAGES, attributeNodes = {
				@NamedAttributeNode(value = RecruitingConstants.GRAPH_ATTRIBUTE_LANGUAGES) }),
		@NamedEntityGraph(name = VacancyEntity.GRAPH_CAREER_LEVEL, attributeNodes = {
				@NamedAttributeNode(value = RecruitingConstants.GRAPH_ATTRIBUTE_CAREER_LEVELS) }),
		@NamedEntityGraph(name = VacancyEntity.GRAPH_BRANCH_LOCATIONS, attributeNodes = {
				@NamedAttributeNode(value = RecruitingConstants.GRAPH_ATTRIBUTE_BRANCH_LOCATIONS) }), })

@Entity
@Table(name = "recruiting_vacancy")
@NamedQueries({
		@NamedQuery(name = VacancyEntity.GET_TOTAL_CANDIDATES, query = "SELECT CAST(COUNT(v) AS int) FROM VacancyEntity v JOIN v.candidates c WHERE v.id = ?1"),
		@NamedQuery(name = VacancyEntity.GET_VACANCIES_WITH_DCEMUSER_REFERENCES, query = "SELECT v FROM VacancyEntity v WHERE decisionMaker = ?1 OR clerk = ?1 OR modifiedBy = ?1 OR ?1 MEMBER OF watchers"), })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class VacancyEntity extends EntityInterface implements Comparable<VacancyEntity> {
	public static final String GRAPH_SKILLS = "VacancyEntity.skills";
	public static final String GRAPH_WATCHERS = "VacancyEntity.watchers";
	public static final String GRAPH_LANGUAGES = "VacancyEntity.languages";
	public static final String GRAPH_CANDIDATES = "VacancyEntity.candidates";
	public static final String GRAPH_CAREER_LEVEL = "VacancyEntity.careerLevels";
	public static final String GRAPH_BRANCH_LOCATIONS = "VacancyEntity.branchLocations";

	public static final String GET_TOTAL_CANDIDATES = "VacancyEntity.getTotalCandidates";
	public static final String GET_VACANCIES_WITH_DCEMUSER_REFERENCES = "VacancyEntity.getVavanciesWithDcemuserReferences";

	@DcemGui(style = "width: 3em")
	@Id
	@Column(name = "vac_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui
	@Column(name = "title", length = 511, nullable = false)
	@NotNullOrEmptyString
	@JsonProperty("title")
	private String defaultTitle;

	@DcemCompare(withoutResult = true)
	@Column(length = 1024 * 1024, name = "description", nullable = false)
	@NotNull
	@JsonProperty("description")
	private String defaultDescription;

	@DcemGui(visible = false)
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "locale", nullable = false)
	@NotNull
	private SupportedLanguage defaultLanguage;

	@DcemGui(filterValue = "false", filterOperator = FilterOperator.IS_FALSE, visible = false)
	@Column(name = "dc_archived", nullable = false)
	@NotNull
	@JsonIgnore
	private boolean archived = false;

	@DcemGui(subClass = "status")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_VACANCY_STATUS"), name = "vacancy_status", nullable = false)
	@JsonSerialize(using = SerializerEntity.class)
	private VacancyStatusEntity status;

	@DcemGui(visible = false, subClass = "careerLevel")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "recruiting_ref_careerlevel_vacancy", joinColumns = @JoinColumn(name = "vac_id"), foreignKey = @ForeignKey(name = "FK_CAREERLEVEL_VACANCY"), inverseJoinColumns = @JoinColumn(name = "dc_id"), inverseForeignKey = @ForeignKey(name = "FK_VACANCY_CAREERLEVEL"))
	@SortNatural
	@JsonSerialize(using = SerializerEntities.class)
	private SortedSet<CareerLevelEntity> careerLevels;

	@DcemGui(sortOrder = SortOrder.ASCENDING)
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "vac_priority", nullable = false)
	@NotNull
	private VacancyPriority priority = VacancyPriority.Normal;

	@DcemGui
	@Column(name = "starting_from", nullable = false)
	@NotNull
	private LocalDate startingFrom;

	@DcemGui
	@Column(name = "application_deadline")
	private LocalDate applicationDeadline;

	@DcemGui(visible = false)
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "vac_type", nullable = false)
	@NotNull
	private VacancyType type = VacancyType.Employee;

	@DcemGui(visible = false)
	@Column(name = "replacement", nullable = false)
	@NotNull
	private boolean replacement;

	@DcemGui(visible = false)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "recruiting_ref_branchlocation_vacancy", joinColumns = @JoinColumn(name = "vac_id"), foreignKey = @ForeignKey(name = "FK_BRANCHLOCATION_VACANCY"), inverseJoinColumns = @JoinColumn(name = "dc_id"), inverseForeignKey = @ForeignKey(name = "FK_VACANCY_BRANCHLOCATION"))
	@SortNatural
	@JsonSerialize(using = SerializerEntities.class)
	private SortedSet<BranchLocation> branchLocations;

	@DcemGui(subClass = "name")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_CLASS_VACANCY"), name = "classification")
	@JsonSerialize(using = SerializerEntity.class)
	private ClassificationEntity classification;

	@DcemGui(subClass = "name")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_DEPT_VACANCY"), name = "department_id")
	@JsonSerialize(using = SerializerToString.class)
	private DepartmentEntity department;

	@DcemCompare(deepCompare = false)
	@DcemGui(visible = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_DECISIONMAKER_VACANCY"), name = "decision_maker")
	@JsonIgnore
	private DcemUser decisionMaker;

	@DcemCompare(deepCompare = false)
	@DcemGui(visible = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_CLERK_VACANCY"), name = "clerk")
	@JsonIgnore
	private DcemUser clerk;

	@OneToMany(mappedBy = "vacancyEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@SortNatural
	@JsonProperty("additionalLangauges")
	private SortedSet<VacancyLanguageEntity> languages;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "recruiting_ref_watchers_vacancy", joinColumns = @JoinColumn(name = "vac_id"), foreignKey = @ForeignKey(name = "FK_WATCHERS_VACANCY"), inverseJoinColumns = @JoinColumn(name = "user_id"), inverseForeignKey = @ForeignKey(name = "FK_VACANCY_WATCHERS"))
	@JsonIgnore
	private List<DcemUser> watchers;

	@DcemGui(visible = false)
	@Column(name = "hours_per_week")
	private Integer hoursPerWeek = 40;

	@DcemGui(visible = false)
	@Column(length = 15, name = "currency")
	private String currency;

	@DcemGui(visible = false)
	@Column(name = "salary_target")
	private Integer salaryTarget;

	@DcemGui(visible = false)
	@Column(name = "salary_max")
	private Integer salaryMax;

	@DcemGui(visible = false)
	@Column(name = "remote_percent")
	private Integer remotePercent;

	@DcemGui(visible = false)
	@Column(name = "onboarding_remote_percent")
	private Integer onboardingRemotePercent;

	@DcemGui(visible = false)
	@Column(name = "probation_time")
	private Integer probationTime = 6;

	@DcemCompare(withoutResult = true)
	@Column(length = 1024 * 1024, name = "remarks")
	private String remarks;

	@DcemCompare(ignore = true)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "recruiting_ref_vacancy_candidate", joinColumns = @JoinColumn(name = "vac_id"), foreignKey = @ForeignKey(name = "FK_CANDIDATE_VACANCY"), inverseJoinColumns = @JoinColumn(name = "candidate_id"), inverseForeignKey = @ForeignKey(name = "FK_VACANCY_CANDIDATE"))
	@SortNatural
	@JsonIgnore
	private SortedSet<CandidateEntity> candidates;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "recruiting_ref_skills_vacancy", joinColumns = @JoinColumn(name = "vac_id"), foreignKey = @ForeignKey(name = "FK_SKILLS_VACANCY"), inverseJoinColumns = @JoinColumn(name = "skills_level_id"), inverseForeignKey = @ForeignKey(name = "FK_VACANCY_SKILLS"))
	@SortNatural
	@JsonIgnore
	private SortedSet<SkillsLevelEntity> skills;

	@DcemGui
	@Column(name = "total_candidates")
	@JsonIgnore
	private int totalCandidates;

	@DcemCompare(ignore = true)
	@DcemGui(visible = true)
	@Column(name = "last_modified_on", nullable = false)
	@NotNull
	private LocalDateTime lastModifiedOn;

	@DcemCompare(ignore = true)
	@DcemGui(visible = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_MODIFIED_VACANCY"), name = "modified_by")
	@JsonIgnore
	private DcemUser modifiedBy;

	public VacancyEntity() {
	}

	public VacancyEntity(Integer id, String defaultTitle) {
		this.id = id;
		this.defaultTitle = defaultTitle;
	}

	@Override
	@JsonIgnore
	public void setId(Number id) {
		if (id != null) {
			this.id = id.intValue();
		} else {
			this.id = null;
		}
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public Integer getId() {
		return id;
	}

	public VacancyStatusEntity getStatus() {
		return status;
	}

	public void setStatus(VacancyStatusEntity status) {
		this.status = status;
	}

	public LocalDate getStartingFrom() {
		return startingFrom;
	}

	public void setStartingFrom(LocalDate startingFrom) {
		this.startingFrom = startingFrom;
	}

	public VacancyType getType() {
		return type;
	}

	public void setType(VacancyType type) {
		this.type = type;
	}

	public boolean isReplacement() {
		return replacement;
	}

	public void setReplacement(boolean replacement) {
		this.replacement = replacement;
	}

	public VacancyPriority getPriority() {
		return priority;
	}

	public void setPriority(VacancyPriority priority) {
		this.priority = priority;
	}

	public ClassificationEntity getClassification() {
		return classification;
	}

	public void setClassification(ClassificationEntity classification) {
		this.classification = classification;
	}

	public DepartmentEntity getDepartment() {
		return department;
	}

	public void setDepartment(DepartmentEntity department) {
		this.department = department;
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

	public SortedSet<VacancyLanguageEntity> getLanguages() {
		return languages;
	}

	public VacancyLanguageEntity getLanguage(SupportedLanguage language) {
		for (VacancyLanguageEntity languageEntity : getLanguages()) {
			if (languageEntity.getLanguage() == language) {
				return languageEntity;
			}
		}
		return null;
	}

	public void setLanguages(SortedSet<VacancyLanguageEntity> languages) {
		this.languages = languages;
	}

	public List<DcemUser> getWatchers() {
		return watchers != null ? watchers : new ArrayList<>();
	}

	public void setWatchers(List<DcemUser> watchers) {
		this.watchers = watchers;
	}

	public Integer getHoursPerWeek() {
		return hoursPerWeek;
	}

	public void setHoursPerWeek(Integer hoursPerWeek) {
		this.hoursPerWeek = hoursPerWeek;
	}

	public Integer getSalaryTarget() {
		return salaryTarget;
	}

	public void setSalaryTarget(Integer salaryTarget) {
		this.salaryTarget = salaryTarget;
	}

	public Integer getSalaryMax() {
		return salaryMax;
	}

	public void setSalaryMax(Integer salaryMax) {
		this.salaryMax = salaryMax;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Integer getRemotePercent() {
		return remotePercent;
	}

	public void setRemotePercent(Integer remotePercent) {
		this.remotePercent = remotePercent;
	}

	public Integer getOnboardingRemotePercent() {
		return onboardingRemotePercent;
	}

	public void setOnboardingRemotePercent(Integer onboardingRemotePercent) {
		this.onboardingRemotePercent = onboardingRemotePercent;
	}

	public Integer getProbationTime() {
		return probationTime;
	}

	public void setProbationTime(Integer probationTime) {
		this.probationTime = probationTime;
	}

	public SortedSet<CandidateEntity> getCandidates() {
		return candidates;
	}

	public void setCandidates(SortedSet<CandidateEntity> candidates) {
		this.candidates = candidates;
	}

	public int getTotalCandidates() {
		return totalCandidates;
	}

	public void setTotalCandidates(int totalCandidates) {
		this.totalCandidates = totalCandidates;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
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

	public String getDefaultTitle() {
		return defaultTitle;
	}

	public void setDefaultTitle(String defaultTitle) {
		this.defaultTitle = defaultTitle;
	}

	public String getDefaultDescription() {
		return defaultDescription;
	}

	public void setDefaultDescription(String defaultDescription) {
		this.defaultDescription = defaultDescription;
	}

	public SupportedLanguage getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(SupportedLanguage defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	public LocalDate getApplicationDeadline() {
		return applicationDeadline;
	}

	public void setApplicationDeadline(LocalDate applicationDeadline) {
		this.applicationDeadline = applicationDeadline;
	}

	public SortedSet<SkillsLevelEntity> getSkills() {
		return skills;
	}

	public void setSkills(SortedSet<SkillsLevelEntity> skills) {
		this.skills = skills;
	}

	@Override
	@JsonIgnore
	public String getRowStyle() {
		return priority.getColorClass();
	}

	@Override
	public String toString() {
		return id + "-" + defaultTitle;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, lastModifiedOn);
	}

	@Override
	public int compareTo(VacancyEntity other) {
		int compareLastModifiedOnResult = Objects.compare(this.getLastModifiedOn(), other.getLastModifiedOn(),
				Comparator.nullsFirst(Comparator.naturalOrder()));
		if (compareLastModifiedOnResult != 0) {
			return compareLastModifiedOnResult;
		}
		int compareDefaultTitleResult = Objects.compare(this.getDefaultTitle(), other.getDefaultTitle(), Comparator.nullsFirst(Comparator.naturalOrder()));
		if (compareDefaultTitleResult != 0) {
			return compareDefaultTitleResult;
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
		return Objects.equals(id, ((VacancyEntity) obj).getId());
	}

	public String toFilename() {
		return toString().replace(' ', '-').replaceAll("[()]", "");
	}

	public SortedSet<BranchLocation> getBranchLocations() {
		return branchLocations;
	}

	public void setBranchLocations(SortedSet<BranchLocation> branchLocations) {
		this.branchLocations = branchLocations;
	}

	public SortedSet<CareerLevelEntity> getCareerLevels() {
		return careerLevels;
	}

	public void setCareerLevels(SortedSet<CareerLevelEntity> careerLevels) {
		this.careerLevels = careerLevels;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	// Following methods are used for template export

	public String getFormattedTitle(String language) {
		Locale locale = new Locale(language);
		Optional<VacancyLanguageEntity> vacancyLanguageOptional = languages.stream().filter(l -> l.getLanguage().getLocale().equals(locale)).findFirst();
		if (vacancyLanguageOptional.isEmpty()) {
			return defaultTitle;
		}
		return vacancyLanguageOptional.get().getTitle();
	}

	public String getFormattedDescription(String language) {
		Locale locale = new Locale(language);
		Optional<VacancyLanguageEntity> vacancyLanguageOptional = languages.stream().filter(l -> l.getLanguage().getLocale().equals(locale)).findFirst();
		if (vacancyLanguageOptional.isEmpty()) {
			return defaultDescription;
		}
		return vacancyLanguageOptional.get().getDescription();
	}

	@JsonIgnore
	public String getFormattedRemarks() {
		if (remarks == null || remarks.equals("<p></p>")) {
			return "";
		}
		return remarks;
	}

	@JsonIgnore
	public String getFormattedRemote() {
		if (remotePercent == null && onboardingRemotePercent == null) {
			return "";
		}
		String remote = remotePercent != null ? String.valueOf(remotePercent) : "-";
		String remoteOnboarding = onboardingRemotePercent != null ? String.valueOf(onboardingRemotePercent) : "-";
		return String.format("%s%% (%s%%)", remote, remoteOnboarding);
	}

	@JsonIgnore
	public String getFormattedSalary(String language) {
		if (salaryTarget == null && salaryMax == null) {
			return "";
		}
		String salaryTargetString = salaryTarget != null ? RecruitingUtils.formatCurrency(language, currency, salaryTarget, 0) : "-";
		String salaryMayString = salaryMax != null ? RecruitingUtils.formatCurrency(language, currency, salaryMax, 0) : "-";
		return String.format("%s / %s", salaryTargetString, salaryMayString);
	}

	@JsonIgnore
	public String getFormattedApplicationDeadline(String language) {
		return RecruitingUtils.formatDate(language, applicationDeadline);
	}

	@JsonIgnore
	public String getFormattedStartingFrom(String language) {
		return RecruitingUtils.formatDate(language, startingFrom);
	}

	@JsonIgnore
	public String getFormattedStatus(String language) {
		return RecruitingUtils.getVacancyStatusNameFromTextResource(status, language);
	}

	@JsonIgnore
	public String getFormattedPriority(String language) {
		return RecruitingUtils.getLocalizedPriority(language, priority);
	}

	@JsonIgnore
	public String getLastModifiedOnFormatted(String language) {
		return RecruitingUtils.formatDateTime(language, lastModifiedOn);
	}

	public List<SkillsLevelEntity> getSkillsRequired() throws Exception {
		if (skills != null) {
			return skills.stream().filter(skill -> skill.getPriority() > 5).collect(Collectors.toList());
		}
		return null;
	}

	public List<SkillsLevelEntity> getSkillsNiceToHave() throws Exception {
		if (skills != null) {
			return skills.stream().filter(skill -> skill.getPriority() <= 5).collect(Collectors.toList());
		}
		return null;
	}

}
