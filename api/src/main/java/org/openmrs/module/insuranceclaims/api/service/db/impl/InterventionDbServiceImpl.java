package org.openmrs.module.insuranceclaims.api.service.db.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimIntervention;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimItem;
import org.openmrs.module.insuranceclaims.api.service.db.InterventionDbService;
import org.openmrs.module.insuranceclaims.api.service.db.ItemDbService;

public class InterventionDbServiceImpl extends BaseOpenmrsService implements InterventionDbService {

    private DbSessionFactory dbSessionFactory;

    /**
     * Finds all InsuranceClaimIntervention that are related to InsuranceClaim
     *
     * @param insuranceClaimId - InsuranceClaim id
     */
    @Override
    public List<InsuranceClaimIntervention> findInsuranceClaimIntervention(int insuranceClaimId) {
        Criteria crit = getCurrentSession().createCriteria(InsuranceClaimIntervention.class, "intervention");
        crit.createAlias("intervention.claim", "claim");

        crit.add(Restrictions.eq("claim.id", insuranceClaimId));
        return crit.list();
    }

    public void setDbSessionFactory(DbSessionFactory dbSessionFactory) {
        this.dbSessionFactory = dbSessionFactory;
    }

    private DbSession getCurrentSession() {
        return dbSessionFactory.getCurrentSession();
    }
}
