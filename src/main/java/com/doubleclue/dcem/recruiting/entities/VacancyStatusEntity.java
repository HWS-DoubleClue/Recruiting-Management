package com.doubleclue.dcem.recruiting.entities;

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
import com.doubleclue.dcem.core.jpa.FilterOperator;

@NamedQueries({
		@NamedQuery(name = VacancyStatusEntity.GET_ALL_VACANCY_STATUS, query = "SELECT vs FROM VacancyStatusEntity vs WHERE vs.deprecated=false ORDER BY vs.rank ASC"),
		@NamedQuery(name = VacancyStatusEntity.GET_COUNT_OF_ALL_VACANCY_STATUS, query = "SELECT COUNT(vs) FROM VacancyStatusEntity vs"),
		@NamedQuery(name = VacancyStatusEntity.GET_STATUS_FOR_HEAD_OFS, query = "SELECT vs FROM VacancyStatusEntity vs WHERE vs.lockedForHeadOfs=false ORDER BY vs.rank ASC") })

@Entity
@Table(name = "recruiting_vacancy_status", uniqueConstraints = @UniqueConstraint(name = "UK_VACANCY_STATUS", columnNames = { "dc_status" }))
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class VacancyStatusEntity extends EntityInterface {

	public static final String GET_ALL_VACANCY_STATUS = "recruiting.GET_ALL_VACANCY_STATUS";
	public static final String GET_COUNT_OF_ALL_VACANCY_STATUS = "recruiting.GET_COUNT_OF_ALL_VACANCY_STATUS";
	public static final String GET_STATUS_FOR_HEAD_OFS = "recruiting.GET_STATUS_FOR_HEAD_OFS";

	@DcemGui(visible = false)
	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui()
	@Column(name = "dc_status", nullable = false, length = 128)
	@NotNullOrEmptyString
	private String status;

	@DcemGui(sortOrder = SortOrder.ASCENDING)
	@Column(name = "dc_rank", nullable = false)
	int rank;

	@DcemGui
	@Column(name = "locked_for_head_ofs", nullable = false)
	boolean lockedForHeadOfs;

	@DcemGui(name = "published")
	boolean published;

	@DcemGui(filterValue = "false", filterOperator = FilterOperator.IS_FALSE)
	boolean deprecated;

	public VacancyStatusEntity() {
	}

	public VacancyStatusEntity(String status, int rank) {
		this(status, rank, false, false);
	}

	public VacancyStatusEntity(String status, int rank, boolean lockedForHeadOfs, boolean deprecated) {
		super();
		this.status = status;
		this.rank = rank;
		this.lockedForHeadOfs = lockedForHeadOfs;
		this.deprecated = deprecated;
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
		return Objects.hash(id, status);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (Hibernate.getClass(this) != Hibernate.getClass(obj)) {
			return false;
		}
		VacancyStatusEntity other = (VacancyStatusEntity) obj;
		return Objects.equals(this.getId(), other.getId()) && Objects.equals(this.getStatus(), other.getStatus());
	}

	@Override
	public String toString() {
		return status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public boolean isDeprecated() {
		return deprecated;
	}

	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}

	public boolean isLockedForHeadOfs() {
		return lockedForHeadOfs;
	}

	public void setLockedForHeadOfs(boolean lockedForHeadOfs) {
		this.lockedForHeadOfs = lockedForHeadOfs;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}
}
