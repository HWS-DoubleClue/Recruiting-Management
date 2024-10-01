package com.doubleclue.dcem.recruiting.entities;

import java.time.LocalDateTime;

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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.recruiting.entities.enums.EntityType;
import com.doubleclue.dcem.recruiting.entities.enums.Operation;

@Entity
@Table(name = "recruiting_history")
@NamedQueries({ @NamedQuery(name = HistoryEntity.GET_SINGLE_BY_TYPE, query = "SELECT h FROM HistoryEntity h WHERE type = ?1 AND reference = ?2"),
		@NamedQuery(name = HistoryEntity.GET_ALL_ENTRIES_BY_DCEMUSER, query = "SELECT h FROM HistoryEntity h WHERE h.modifiedBy = ?1") })
public class HistoryEntity extends EntityInterface {
	public static final String GET_SINGLE_BY_TYPE = "HistoryEntity.getSingleByType";
	public static final String GET_ALL_ENTRIES_BY_DCEMUSER = "HistoryEntity.getAllEntriesByDcemuser";

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@DcemGui(sortOrder = SortOrder.DESCENDING)
	private Integer id;

	@DcemGui(subClass = "displayName")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_MODIFIED_HISTORY"), name = "modified_by", nullable = false)
	private DcemUser modifiedBy;

	@DcemGui
	@Column(nullable = false, name = "dc_timestamp")
	private LocalDateTime timestamp;

	@DcemGui
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "operation", nullable = false)
	private Operation operation;

	@DcemGui
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "dc_type", nullable = false)
	private EntityType type;

	@DcemGui
	@Column(name = "object_reference", nullable = false)
	private int reference;

	@DcemGui
	@Column(length = 1024 * 4, name = "dc_information", nullable = false)
	private String information;

	public HistoryEntity() {
	}

	public HistoryEntity(DcemUser modifiedBy, LocalDateTime timestamp, EntityType type) {
		super();
		this.modifiedBy = modifiedBy;
		this.timestamp = timestamp;
		this.type = type;
	}

	@Override
	public Number getId() {
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

	public DcemUser getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(DcemUser modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public EntityType getType() {
		return type;
	}

	public void setType(EntityType type) {
		this.type = type;
	}

	public int getReference() {
		return reference;
	}

	public void setReference(int reference) {
		this.reference = reference;
	}

	public String getInformation() {
		return information;
	}

	public void setInformation(String information) {
		this.information = information;
	}
}
