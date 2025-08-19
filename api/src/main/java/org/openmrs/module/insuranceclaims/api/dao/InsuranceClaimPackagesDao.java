/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.insuranceclaims.api.dao;

import java.util.Date;
import java.util.List;
import java.util.Collection;

import org.openmrs.Patient;
// import org.openmrs.module.kenyaemrml.iit.PatientRiskScore;
// import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimPackage;

public interface InsuranceClaimPackagesDao {
	
	/**
	 * Saves or updates package
	 * 
	 * @param riskScore
	 * @return
	 */
	public InsuranceClaimPackage saveOrUpdatePackage(InsuranceClaimPackage pkg);
	
	/**
	 * Gets a list of risk score for a patient
	 * 
	 * @return
	 */
	public List<InsuranceClaimPackage> getAllPackages();


}
