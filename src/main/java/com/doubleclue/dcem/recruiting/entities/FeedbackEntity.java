package com.doubleclue.dcem.recruiting.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
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
import javax.persistence.UniqueConstraint;

import org.hibernate.Hibernate;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.validators.NotNullOrEmptyString;
import com.doubleclue.dcem.core.utils.compare.DcemCompare;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "recruiting_feedback", uniqueConstraints = { @UniqueConstraint(name = "UK_RECRUITING_FEEDBACK", columnNames = { "dc_user", "candidate_id" }) })
@NamedQueries({ @NamedQuery(name = FeedbackEntity.GET_FEEDBACK_OF_CANDIDATE, query = "SELECT tt FROM FeedbackEntity tt WHERE tt.candidate = ?1"),
		@NamedQuery(name = FeedbackEntity.GET_FEEDBACKS_BY_DCEMUSER, query = "SELECT fb FROM FeedbackEntity fb WHERE fb.user = ?1"), })
public class FeedbackEntity extends EntityInterface implements Serializable, Comparable<FeedbackEntity> {

	private static final long serialVersionUID = -1272588608877660213L;
	public static final String GET_FEEDBACK_OF_CANDIDATE = "FeedbackEntity.getFeedbackOfCandidate";
	public static final String GET_FEEDBACKS_BY_DCEMUSER = "FeedbackEntity.getAllFeedbacksByDcemuser";

	@Id
	@Column(name = "feedback_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemCompare(ignore = true)
	@Column(name = "posted_on", nullable = false)
	@JsonIgnore
	private LocalDateTime postedOn;

	@ManyToOne
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_USER_FEEDBACK"), name = "dc_user", nullable = false)
	@JsonIgnore
	private DcemUser user;

	@Column(name = "dc_rating", nullable = false)
	private int rating = 0;

	// @Column(name = "candidate_status", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_FEEDBACK_CANDIDATE_STATUS"), name = "candidate_status", nullable = false)
	private CandidateStatusEntity candidateStatus;

	@DcemCompare(withoutResult = true)
	@Column(name = "dc_text", length = 1024 * 4, nullable = false)
	@NotNullOrEmptyString
	private String text;

	@DcemCompare(ignore = true)
	@ManyToOne
	@JoinColumn(referencedColumnName = "candidate_id", foreignKey = @ForeignKey(name = "FK_CANDIDATE_FEEDBACK"), name = "candidate_id", nullable = false)
	@JsonIgnore
	private CandidateEntity candidate;

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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public DcemUser getUser() {
		return user;
	}

	public void setUser(DcemUser user) {
		this.user = user;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public CandidateEntity getCandidate() {
		return candidate;
	}

	public void setCandidate(CandidateEntity candidate) {
		this.candidate = candidate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, postedOn);
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
		FeedbackEntity other = (FeedbackEntity) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public int compareTo(FeedbackEntity other) {
		int comparePostedOnResult = Objects.compare(this.getPostedOn(), other.getPostedOn(), Comparator.nullsFirst(Comparator.naturalOrder()));
		if (comparePostedOnResult != 0) {
			return comparePostedOnResult;
		}
		return Objects.compare(this.getId(), other.getId(), Comparator.nullsFirst(Comparator.naturalOrder()));
	}

	@Override
	public String toString() {
		return "user=" + user + ", rating=" + rating + ", candidateStatus=" + candidateStatus + ", candidate=" + candidate + "]";
	}

	public LocalDateTime getPostedOn() {
		return postedOn;
	}

	public void setPostedOn(LocalDateTime postedOn) {
		this.postedOn = postedOn;
	}

	public CandidateStatusEntity getCandidateStatus() {
		return candidateStatus;
	}

	public void setCandidateStatus(CandidateStatusEntity candidateStatus) {
		this.candidateStatus = candidateStatus;
	}
}
