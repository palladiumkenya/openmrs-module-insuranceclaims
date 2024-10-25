package org.openmrs.module.insuranceclaims.api.dao.impl;

import org.openmrs.module.insuranceclaims.api.dao.BaseOpenmrsDataDao;
import org.openmrs.module.insuranceclaims.api.dao.InsuranceClaimDiagnosisDao;
import org.openmrs.module.insuranceclaims.api.dao.InsuranceClaimInterventionDao;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimDiagnosis;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimIntervention;

public class InsuranceClaimInterventionDaoImpl extends BaseOpenmrsDataDao<InsuranceClaimIntervention>
		implements InsuranceClaimInterventionDao {

	public InsuranceClaimInterventionDaoImpl() {
		super(InsuranceClaimIntervention.class);
	}
}
