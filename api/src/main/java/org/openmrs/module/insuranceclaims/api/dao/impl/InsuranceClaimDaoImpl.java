package org.openmrs.module.insuranceclaims.api.dao.impl;

import org.openmrs.module.insuranceclaims.api.dao.BaseOpenmrsDataDao;
import org.openmrs.module.insuranceclaims.api.dao.InsuranceClaimDao;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimStatus;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class InsuranceClaimDaoImpl extends BaseOpenmrsDataDao<InsuranceClaim> implements InsuranceClaimDao {
	public InsuranceClaimDaoImpl() {
		super(InsuranceClaim.class);
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
	public List<InsuranceClaim> getAllInsuranceClaimsByCashierBill(String billUuid) {
		Criteria crit = createCriteria();
		crit.createCriteria("bill_number")
				.add(Restrictions.eq("bill_number", billUuid));
		return findAllByCriteria(crit, false);
	}
}
