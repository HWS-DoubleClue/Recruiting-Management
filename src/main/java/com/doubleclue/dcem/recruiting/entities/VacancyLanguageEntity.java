package com.doubleclue.dcem.recruiting.entities;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.Hibernate;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.gui.validators.NotNullOrEmptyString;
import com.doubleclue.dcem.core.utils.compare.DcemCompare;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "recruiting_vacancy_language")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VacancyLanguageEntity extends EntityInterface implements Serializable, Comparable<VacancyLanguageEntity> {

	private static final long serialVersionUID = 1274313064224363945L;

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private Integer id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "vacancy_id", foreignKey = @ForeignKey(name = "FK_VACANCY_LANGUAGE"), nullable = false)
	@NotNull
	@JsonIgnore
	private VacancyEntity vacancyEntity;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "locale", nullable = false)
	@NotNull
	private SupportedLanguage language;

	@Column(name = "title", length = 511, nullable = false)
	@NotNullOrEmptyString
	private String title;

	@DcemCompare(withoutResult = true)
	@Column(length = 1024 * 1024, name = "description", nullable = false)
	@NotNull
	private String description;
	
	@Transient
	@JsonIgnore
	private boolean defaultLanguage;

	public VacancyLanguageEntity() {
	}

	public VacancyLanguageEntity(VacancyEntity vacancyEntity) {
		super();
		this.vacancyEntity = vacancyEntity;
		this.language = vacancyEntity.getDefaultLanguage();
		this.title = vacancyEntity.getDefaultTitle();
		this.description = vacancyEntity.getDefaultDescription();
		this.defaultLanguage = true;
	}


	public VacancyLanguageEntity(VacancyEntity vacancyEntity, SupportedLanguage language, boolean defaultLanguage) {
		super();
		this.vacancyEntity = vacancyEntity;
		this.language = language;
		this.defaultLanguage = defaultLanguage;
	}


	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		if (id != null) {
			this.id = id.intValue();
		} else {
			this.id = null;
		}
	}

	public SupportedLanguage getLanguage() {
		return language;
	}

	public void setLanguage(SupportedLanguage language) {
		this.language = language;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public VacancyEntity getVacancyEntity() {
		return vacancyEntity;
	}

	public void setVacancyEntity(VacancyEntity vacancyEntity) {
		this.vacancyEntity = vacancyEntity;
	}
	
	public boolean isDefaultLanguage() {
		return defaultLanguage;
	}
	
	public void setDefaultLanguage(boolean defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	@Override
	public String toString() {
		return title + " (" + language + ")";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, title, description, language);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (Hibernate.getClass(this) != Hibernate.getClass(obj))
			return false;
		VacancyLanguageEntity other = (VacancyLanguageEntity) obj;
		return Objects.equals(this.getId(), other.getId());
	}

	@Override
	public int compareTo(VacancyLanguageEntity other) {
		int compareLanguageResult = Objects.compare(this.getLanguage(), other.getLanguage(), Comparator.nullsFirst(Comparator.naturalOrder()));
		if (compareLanguageResult != 0) {
			return compareLanguageResult;
		}
		return Objects.compare(this.getId(), other.getId(), Comparator.nullsFirst(Comparator.naturalOrder()));
	}
}
