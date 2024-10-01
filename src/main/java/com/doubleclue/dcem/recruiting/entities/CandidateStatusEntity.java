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
		@NamedQuery(name = CandidateStatusEntity.GET_ALL, query = "SELECT cs FROM CandidateStatusEntity cs WHERE cs.deprecated = false ORDER BY cs.rank ASC"),
		@NamedQuery(name = CandidateStatusEntity.COUNT_ALL, query = "SELECT COUNT(cs) FROM CandidateStatusEntity cs"),

})

@Entity
@Table(name = "recruiting_candidate_status", uniqueConstraints = @UniqueConstraint(name = "UK_CANDIDATE_STATUS", columnNames = { "dc_status" }))
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CandidateStatusEntity extends EntityInterface {

	public static final String GET_ALL = "recruiting.GET_ALL_CANDIDATE_STATUS";
	public static final String COUNT_ALL = "recruiting.COUNT_ALL_CANDIDATE_STATUS";

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

	@DcemGui(name = "dc_deprecated", filterValue = "false", filterOperator = FilterOperator.IS_FALSE)
	boolean deprecated;

	public CandidateStatusEntity() {
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
		CandidateStatusEntity other = (CandidateStatusEntity) obj;
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
}
