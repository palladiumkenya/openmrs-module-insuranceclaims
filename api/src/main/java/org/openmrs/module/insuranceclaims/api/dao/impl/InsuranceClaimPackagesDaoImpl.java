package org.openmrs.module.insuranceclaims.api.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Session;
import org.openmrs.module.insuranceclaims.api.dao.InsuranceClaimPackagesDao;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimPackage;
import org.hibernate.Criteria;
import org.hibernate.Session;

public class InsuranceClaimPackagesDaoImpl implements InsuranceClaimPackagesDao {

    private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	private Session getSession() {
		return sessionFactory.getCurrentSession();
	}

    @Override
    public InsuranceClaimPackage saveOrUpdatePackage(InsuranceClaimPackage pkg) {
        getSession().saveOrUpdate(pkg);
		return pkg;
    }

    @Override
    public List<InsuranceClaimPackage> getAllPackages() {
        return (List<InsuranceClaimPackage>) getSession().createCriteria(InsuranceClaimPackage.class).list();
    }

	/**
	 * Gets a list of packages with gender filter
	 * 
	 * @param patient
	 * @return
	 */
	@Override
	public List<InsuranceClaimPackage> getPackages(String gender) {
		String lgender = "";
		Criteria criteria = getSession().createCriteria(InsuranceClaimPackage.class);

		if(gender != null && !gender.trim().isEmpty()) {
			lgender = gender.trim().toLowerCase();
			if(lgender.equalsIgnoreCase("MALE") || lgender.equalsIgnoreCase("M")) {
				criteria.add(Restrictions.eq("gender", "MALE"));
			} else if(lgender.equalsIgnoreCase("FEMALE") || lgender.equalsIgnoreCase("F")) {
				criteria.add(Restrictions.eq("gender", "FEMALE"));
			}
		} else {
			System.err.println("Insurance Claims Module: Getting Packages: No gender filter provided. We return the full list");
		}

		return(criteria.list());
	}
    
}
