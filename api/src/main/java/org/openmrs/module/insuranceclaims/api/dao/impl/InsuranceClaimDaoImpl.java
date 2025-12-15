package org.openmrs.module.insuranceclaims.api.dao.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.insuranceclaims.api.dao.BaseOpenmrsDataDao;
import org.openmrs.module.insuranceclaims.api.dao.InsuranceClaimDao;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimStatus;

public class InsuranceClaimDaoImpl extends BaseOpenmrsDataDao<InsuranceClaim> implements InsuranceClaimDao {
	public InsuranceClaimDaoImpl() {
		super(InsuranceClaim.class);
	}

	@Override
	public List<InsuranceClaim> getAllInsuranceClaims() {
		Criteria criteria = createCriteria();
		// Add ordering by iclm_claim_id in descending order
		criteria.addOrder(org.hibernate.criterion.Order.desc("id"));
		return findAllByCriteria(criteria, false);
	}

	@Override
	public List<InsuranceClaim> getAllInsuranceClaimsByPatient(Integer patientId) {
		Criteria crit = createCriteria();
		crit.createCriteria("patient")
				.add(Restrictions.eq("patient", patientId));
		return findAllByCriteria(crit, false);
	}

	@Override
	public List<InsuranceClaim> getAllInsuranceClaimsByPatient(String patientId) {
		Criteria crit = createCriteria();
		crit.createCriteria("patient")
				.add(Restrictions.eq("patient", patientId));
		return findAllByCriteria(crit, false);
	}

	@Override
	public List<InsuranceClaim> getUnProcessedInsuranceClaims() {
		Criteria crit = createCriteria();
		crit.createCriteria("status")
				.add(Restrictions.eq("status", InsuranceClaimStatus.ENTERED));
		return findAllByCriteria(crit, false);
	}

	@Override
	public List<InsuranceClaim> getInsuranceClaims(String uuid, String status, String usetype, String claimCode,
			Date createdOnOrAfterDate, Date createdOnOrBeforeDate) {
		System.err.println("Insurance Claims: Searching for claims using: " + uuid + " : " + status + " : " + usetype
				+ " : " + claimCode + " : " + createdOnOrAfterDate + " : " + createdOnOrBeforeDate + " : ");

		Session session = this.sessionFactory.getCurrentSession();
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<InsuranceClaim> criteriaQuery = criteriaBuilder.createQuery(InsuranceClaim.class);
		Root<InsuranceClaim> root = criteriaQuery.from(InsuranceClaim.class);

		// Create predicates for the restrictions
		Predicate predicate = criteriaBuilder.conjunction();

		// uuid
		if (uuid != null && !uuid.isEmpty()) {
			predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("uuid"), uuid));

		}

		// status
		if (status != null && !status.isEmpty()) {
			predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
		}

		// type
		if (usetype != null && !usetype.isEmpty()) {
			predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("usetype"), usetype));
		}

		// type
		if (claimCode != null && !claimCode.isEmpty()) {
			predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("claimCode"), claimCode));
		}

		// createdOnOrAfterDate
		if (createdOnOrAfterDate != null) {
			Path<Date> datePath = root.get("dateCreated");

			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.greaterThanOrEqualTo(datePath, createdOnOrAfterDate));
		}

		// createdOnOrBeforeDate
		if (createdOnOrBeforeDate != null) {
			Path<Date> datePath = root.get("dateCreated");

			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.lessThanOrEqualTo(datePath, createdOnOrBeforeDate));
		}

		// criteriaQuery.where(predicate);
		criteriaQuery.where(predicate).distinct(true);

		// Order by 'iclm_claim_id' in descending order
		criteriaQuery.orderBy(criteriaBuilder.desc(root.get("id")));

		// Print the generated SQL query
		Query query = session.createQuery(criteriaQuery);
		String sqlQuery = query.unwrap(org.hibernate.query.Query.class).getQueryString();
		System.out.println("Insurance Claims: Generated SQL Query: " + sqlQuery);

		List<InsuranceClaim> results = session.createQuery(criteriaQuery).getResultList();

		return (results);
	}

	@Override
	public List<InsuranceClaim> getAllInsuranceClaimsByCashierBill(String billUuid) {
		Criteria crit = createCriteria();
		crit.createCriteria("bill_number")
				.add(Restrictions.eq("bill_number", billUuid));
		return findAllByCriteria(crit, false);
	}

	// @Override
	// public InsuranceClaim getInsuranceClaimByExternalId(String externalId) {
	// Criteria criteria = createCriteria();
	// criteria.createCriteria("external_id")
	// .add(Restrictions.eq("external_id", externalId));
	// return findByCriteria(criteria, false);
	// }

	@Override
	public InsuranceClaim getInsuranceClaimByExternalId(String externalId) {
		Criteria criteria = createCriteria();
		criteria.add(Restrictions.eq("externalId", externalId));
		return findByCriteria(criteria, false);
	}

	@Override
	public InsuranceClaim getLatestInsuranceClaimByPatient(Integer patientId) {
		Criteria criteria = createCriteria();
		criteria.createCriteria("patient")
				.add(Restrictions.eq("patient", patientId));
		// Add ordering by iclm_claim_id in descending order
		criteria.addOrder(org.hibernate.criterion.Order.desc("id"));
		criteria.setMaxResults(1);

		return (InsuranceClaim) criteria.uniqueResult();
	}
}
