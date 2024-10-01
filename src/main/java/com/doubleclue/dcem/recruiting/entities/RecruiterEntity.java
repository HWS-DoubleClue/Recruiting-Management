package com.doubleclue.dcem.recruiting.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.Hibernate;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.validators.NotNullOrEmptyString;
import com.doubleclue.dcem.core.utils.compare.DcemCompare;
import com.doubleclue.dcem.recruiting.logic.RecruitingConstants;

@NamedEntityGraphs({
		@NamedEntityGraph(name = RecruiterEntity.GRAPH_FILES, attributeNodes = { @NamedAttributeNode(value = RecruitingConstants.GRAPH_ATTRIBUTE_FILES) }), })
@Entity
@Table(name = "recruiting_recruiter", uniqueConstraints = @UniqueConstraint(name = "UK_RECRUITER_NAME", columnNames = { "company_name" }))
@NamedQueries({ @NamedQuery(name = RecruiterEntity.GET_ALL, query = "SELECT rc FROM RecruiterEntity rc ORDER BY rc.companyName ASC"),
				@NamedQuery(name = RecruiterEntity.GET_RECRUITERS_BY_DCEMUSER, query = "SELECT rc FROM RecruiterEntity rc WHERE rc.modifiedBy = ?1"),
})
public class RecruiterEntity extends EntityInterface {
	public static final String GRAPH_FILES = "RecruiterEntity.files";

	public static final String GET_ALL = "RecruiterEntity.getAll";
	public static final String GET_RECRUITERS_BY_DCEMUSER = "RecruiterEntity.getRecruiterByDcemuser";

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@DcemGui(visible = false)
	private Integer id;

	@DcemGui(sortOrder = SortOrder.ASCENDING)
	@Column(nullable = false, name = "company_name", length = 255)
	@NotNullOrEmptyString
	private String companyName;

	@DcemGui(linkUrl = "https://#{value}")
	@Column(nullable = false, name = "company_url", length = 255)
	@NotNullOrEmptyString
	private String companyUrl;

	@DcemGui(visible = false)
	@Column(name = "dc_address", length = 255)
	private String address;

	@DcemGui(visible = false)
	@Column(name = "zip_code", length = 15)
	private String zipCode;

	@DcemGui(visible = true)
	@Column(name = "city", length = 127)
	private String city;

	@DcemGui(visible = true)
	@Column(name = "countryState", length = 127)
	private String countryState;

	@DcemGui
	@Column(length = 255, name = "dc_country", nullable = false)
	@NotNullOrEmptyString
	private String country;

	@DcemGui
	@Column(nullable = false, name = "contact_person", length = 255)
	@NotNullOrEmptyString
	private String contactPerson;

	@DcemGui
	@Column(nullable = false, name = "email", length = 255)
	@NotNullOrEmptyString
	private String email;

	@DcemGui
	@Column(nullable = false, name = "phone", length = 63)
	@NotNullOrEmptyString
	private String phone;

	@DcemCompare(withoutResult = true)
	@Column(length = 1024 * 1024, name = "contract_information")
	private String contractInformation;

	@DcemCompare(ignore = true)
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "recruiting_ref_recruiter_file", joinColumns = @JoinColumn(name = "recruiter_id"), foreignKey = @ForeignKey(name = "FK_FILE_RECRUITER"), inverseJoinColumns = @JoinColumn(name = "file_id"), inverseForeignKey = @ForeignKey(name = "FK_RECRUITER_FILE"))
	private List<CloudSafeEntity> files;

	@DcemCompare(ignore = true)
	@DcemGui(visible = false)
	@Column(nullable = false, name = "last_modified_on")
	@NotNull
	private LocalDateTime lastModifiedOn;

	@DcemCompare(ignore = true)
	@DcemGui(visible = false, subClass = "displayName")
	@ManyToOne
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_MODIFIED_RECRUITER"), name = "modified_by")
	private DcemUser modifiedBy;

	public RecruiterEntity() {

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

	public String getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public List<CloudSafeEntity> getFiles() {
		return files;
	}

	public void setFiles(List<CloudSafeEntity> files) {
		this.files = files;
	}

	public String getContractInformation() {
		return contractInformation;
	}

	public void setContractInformation(String contractInformation) {
		this.contractInformation = contractInformation;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
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

	@Override
	public String toString() {
		return companyName;
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
		RecruiterEntity other = (RecruiterEntity) obj;
		return Objects.equals(id, other.id);
	}

	public String getCompanyUrl() {
		return companyUrl;
	}

	public void setCompanyUrl(String companyUrl) {
		this.companyUrl = companyUrl;
	}
}
