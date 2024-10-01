package com.doubleclue.dcem.recruiting.entities;

import java.util.Objects;
import java.util.SortedSet;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SortNatural;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.validators.NotNullOrEmptyString;
import com.doubleclue.dcem.recruiting.entities.enums.ClassificationType;

@NamedQueries({ 
	@NamedQuery(name = ClassificationEntity.GET_ALL_BY_TYPE, query = "SELECT c FROM ClassificationEntity c JOIN c.types t WHERE t = ?1"),
	@NamedQuery(name = ClassificationEntity.GET_BY_NAME, query = "SELECT c FROM ClassificationEntity c WHERE c.name = ?1"),
})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
@Table(name = "recruiting_classification", uniqueConstraints = @UniqueConstraint(name = "UK_CLASS_NAME", columnNames = { "dc_name" }))
public class ClassificationEntity extends EntityInterface {
	public static final String GET_ALL_BY_TYPE = "ClassificationEntity.getAllByType";
	public static final String GET_BY_NAME = "ClassificationEntity.getByName";
	
	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@DcemGui(sortOrder = SortOrder.ASCENDING)
	@Column(name = "dc_name", nullable = false, length = 255)
	@NotNullOrEmptyString
	private String name;
	
	@DcemGui
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "recruiting_ref_types_classification", joinColumns = @JoinColumn(name = "dc_id", referencedColumnName = "dc_id"))
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "dc_types", nullable = false)
	@SortNatural
	private SortedSet<ClassificationType> types;
	
	public ClassificationEntity() {
		
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public SortedSet<ClassificationType> getTypes(){
		return types;
	}
	
	public void setTypes(SortedSet<ClassificationType> types) {
		this.types = types;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (Hibernate.getClass(this) != Hibernate.getClass(obj))
			return false;
		ClassificationEntity other = (ClassificationEntity) obj;
		return Objects.equals(id, other.id);
	}
}
