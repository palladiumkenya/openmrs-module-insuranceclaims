package org.openmrs.module.insuranceclaims.api.model;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.VisitType;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Model class that represents an insurance claim.
 */
@Entity(name = "iclm.Claim")
@Table(name = "iclm_claim")
@Inheritance(strategy = InheritanceType.JOINED)
public class InsuranceClaim extends AbstractBaseOpenmrsData {

	private static final long serialVersionUID = 1649522411236291450L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "iclm_claim_id")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "provider")
	@JsonIgnore
	private Provider provider;

	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "patient", nullable = false)
	@JsonIgnore
	private Patient patient;

	@ManyToOne
	@JoinColumn(name = "location")
	private Location location;

	@Basic
	@Column(name = "claim_code", length = 255, nullable = false)
	private String claimCode;

	@Basic
	@Column(name = "response_UUID", length = 255, nullable = true)
	private String responseUUID;

	@Basic
	@Column(name = "usetype", length = 64, nullable = false)
	private String use;

	@Basic
	@Column(name = "bill_number", length = 255, nullable = true)
	private String billNumber;

	@Basic
	@Column(name = "date_from", nullable = false)
	private Date dateFrom;

	@Basic
	@Column(name = "date_to", nullable = false)
	private Date dateTo;

	@Basic
	@Column(name = "adjustment", columnDefinition = "TEXT")
	private String adjustment;

	@Basic
	@Column(name = "claimed_total")
	private BigDecimal claimedTotal;

	@Basic
	@Column(name = "approved_total")
	private BigDecimal approvedTotal;

	@Basic
	@Column(name = "date_processed")
	private Date dateProcessed;

	@Basic
	@Column(name = "explanation", columnDefinition = "TEXT")
	private String explanation;

	@Basic
	@Column(name = "rejection_reason", length = 255)
	private String rejectionReason;

	@Basic
	@Column(name = "guarantee_id", length = 255)
	private String guaranteeId;

	@Basic
	@Column(name = "insurer", length = 255)
	private String insurer;

	@Basic
	@Column(name = "product", length = 255)
	private String product;

	@ManyToOne
	@JoinColumn(name = "visit")
	private Visit visit;

	@ManyToOne
	@JoinColumn(name = "encounter")
	private Encounter encounter;

	@Basic
	@Column(name = "external_id", length = 255)
	private String externalId;

	@ManyToOne
	@JoinColumn(name = "visit_type")
	private VisitType visitType;

	@Basic
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private InsuranceClaimStatus status;

	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "bill", nullable = true)
	@JsonIgnore
	private Bill bill;

	@Override
	public Integer getId() {
		return this.id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getClaimCode() {
		return claimCode;
	}

	public void setClaimCode(String claimCode) {
		this.claimCode = claimCode;
	}

	public Date getDateFrom() {
		return dateFrom == null ? null : (Date) dateFrom.clone();
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom == null ? null : (Date) dateFrom.clone();
	}

	public Date getDateTo() {
		return dateTo == null ? null : (Date) dateTo.clone();
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo == null ? null : (Date) dateTo.clone();
	}

	public String getAdjustment() {
		return adjustment;
	}

	public void setAdjustment(String adjustment) {
		this.adjustment = adjustment;
	}

	public BigDecimal getClaimedTotal() {
		return claimedTotal;
	}

	public void setClaimedTotal(BigDecimal claimedTotal) {
		this.claimedTotal = claimedTotal;
	}

	public BigDecimal getApprovedTotal() {
		return approvedTotal;
	}

	public void setApprovedTotal(BigDecimal approvedTotal) {
		this.approvedTotal = approvedTotal;
	}

	public Date getDateProcessed() {
		return dateProcessed == null ? null : (Date) dateProcessed.clone();
	}

	public void setDateProcessed(Date dateProcessed) {
		this.dateProcessed = dateProcessed == null ? null : (Date) dateProcessed.clone();
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public String getGuaranteeId() {
		return guaranteeId;
	}


	public void setGuaranteeId(String guaranteeId) {
		this.guaranteeId = guaranteeId;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public VisitType getVisitType() {
		return visitType;
	}

	public void setVisitType(VisitType visitType) {
		this.visitType = visitType;
	}

	public InsuranceClaimStatus getStatus() {
		return status;
	}

	public void setStatus(InsuranceClaimStatus status) {
		this.status = status;
	}

	public Bill getBill() {
		return bill;
	}

	public void setBill(Bill bill) {
		this.bill = bill;
	}

	public String getBillNumber() {
		return billNumber;
	}

	public void setBillNumber(String billNumber) {
		this.billNumber = billNumber;
	}

	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
	}

	public Visit getVisit() {
		return visit;
	}

	public void setVisit(Visit visit) {
		this.visit = visit;
	}

	public String getResponseUUID() {
		return responseUUID;
	}

	public void setResponseUUID(String responseUUID) {
		this.responseUUID = responseUUID;
	}

	public Encounter getEncounter() {
		return encounter;
	}

	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}

	public String getInsurer() {
		return insurer;
	}

	public void setInsurer(String insurer) {
		this.insurer = insurer;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}
}
