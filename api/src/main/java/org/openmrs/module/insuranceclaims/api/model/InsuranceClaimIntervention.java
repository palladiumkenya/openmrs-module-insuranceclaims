package org.openmrs.module.insuranceclaims.api.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.openmrs.Concept;

/**
 * Model class that represents an insurance claim intervention.
 * Used to point the reason for creating the claim.
 */
@Entity(name = "iclm.ClaimIntervention")
@Table(name = "iclm_claim_intervention")
@Inheritance(strategy = InheritanceType.JOINED)
public class InsuranceClaimIntervention extends AbstractBaseOpenmrsData {

	private static final long serialVersionUID = 1229077935109398654L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "iclm_claim_intervention_id")
	private Integer id;

	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "claim", nullable = false)
	private InsuranceClaim claim;

	@Basic
	@Column(name = "name")
	private String name;

	public InsuranceClaimIntervention() {
	}

	/**
	 * Creates the representation of an insurance claim diagnosis
	 *
	 * @param name        - the related concept object
	 * @param claim - the related insurance claim object
	 */
	public InsuranceClaimIntervention(String name, InsuranceClaim claim) {
		super();
		this.name= name;
		this.claim = claim;
	}

	@Override
	public Integer getId() {
		return this.id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public InsuranceClaim getClaim() {
		return claim;
	}

	public void setClaim(InsuranceClaim claim) {
		this.claim = claim;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
