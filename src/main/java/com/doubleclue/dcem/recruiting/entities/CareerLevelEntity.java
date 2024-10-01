package com.doubleclue.dcem.recruiting.entities;

import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.validators.NotNullOrEmptyString;
import com.doubleclue.dcem.recruiting.logic.RecruitingUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@NamedQueries({
	@NamedQuery(name = CareerLevelEntity.GET_FILTERED_CAREER_LEVEL_NAMES, query = "SELECT cl.careerLevel FROM CareerLevelEntity cl WHERE LOWER(cl.careerLevel) LIKE ?1  ORDER BY cl.careerLevel ASC"), 
	@NamedQuery(name = CareerLevelEntity.GET_CAREER_LEVEL_BY_NAMES, query = "SELECT cl FROM CareerLevelEntity cl WHERE cl.careerLevel IN ?1 "), 
	})

@Entity
@Table(name = "recruiting_career_level", uniqueConstraints = @UniqueConstraint(name = "UK_CAREER_LEVEL", columnNames = { "career_Level" }))
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CareerLevelEntity extends EntityInterface implements Comparable<CareerLevelEntity> {

	public static final String GET_FILTERED_CAREER_LEVEL_NAMES = "recruiting.GET_FILTERED_CAREER_LEVEL_NAMES";
	public static final String GET_CAREER_LEVEL_BY_NAMES = "recruiting.GET_CAREER_LEVEL_BY_NAMES";
	
	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui(sortOrder = SortOrder.ASCENDING)
	@Column(name = "career_Level", nullable = false, length = 255)
	@NotNullOrEmptyString
	private String careerLevel;

	
	public CareerLevelEntity() {
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

	@Override
	public int hashCode() {
		return Objects.hash(id, careerLevel);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (Hibernate.getClass(this) != Hibernate.getClass(obj))
			return false;
		CareerLevelEntity other = (CareerLevelEntity) obj;
		return Objects.equals(this.getId(), other.getId()) && Objects.equals(this.getCareerLevel(), other.getCareerLevel());
	}

	@Override
	public int compareTo(CareerLevelEntity other) {
		int compareSurnameResult = Objects.compare(this.getCareerLevel(), other.getCareerLevel(), Comparator.nullsFirst(Comparator.naturalOrder()));
		if (compareSurnameResult != 0) {
			return compareSurnameResult;
		}
		return Objects.compare(this.getId(), other.getId(), Comparator.nullsFirst(Comparator.naturalOrder()));
	}

	@Override
	public String toString() {
		return careerLevel;
	}

	public String getCareerLevel() {
		return careerLevel;
	}

	public void setCareerLevel(String careerLevel) {
		this.careerLevel = careerLevel;
	}
	
	public String getLocalizedCareerLevel(String language) {
		return RecruitingUtils.getCareerLevelNameFromTextResource(this, language);
	}

}
