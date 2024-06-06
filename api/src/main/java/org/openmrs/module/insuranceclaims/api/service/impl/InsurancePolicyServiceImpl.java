package org.openmrs.module.insuranceclaims.api.service.impl;

import ca.uhn.fhir.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.NullPrecedence;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse;
import org.hl7.fhir.r4.model.Money;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.exceptions.FHIRException;

import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.api.PersonService;
import org.openmrs.api.ValidationException;
import org.openmrs.module.insuranceclaims.api.mapper.InsurancePolicyMapper;
import org.openmrs.module.insuranceclaims.api.model.InsurancePolicy;
import org.openmrs.module.insuranceclaims.api.model.InsurancePolicyStatus;
import org.openmrs.module.insuranceclaims.api.model.dto.InsurancePolicyDTO;
import org.openmrs.module.insuranceclaims.api.service.InsurancePolicyService;
import org.openmrs.module.insuranceclaims.util.ConstantValues;

import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.CONTRACT_DATE_PATTERN;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.CONTRACT_EXPIRE_DATE_ORDINAL;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.CONTRACT_POLICY_ID_ORDINAL;

public class InsurancePolicyServiceImpl extends BaseOpenmrsDataService<InsurancePolicy> implements InsurancePolicyService {

    private PersonService personService;

    private InsurancePolicyMapper insurancePolicyMapper;

    @Override
    public InsurancePolicy generateInsurancePolicy(CoverageEligibilityResponse response) throws FHIRException {
        Reference contract = response.getInsuranceFirstRep().getCoverage();
        InsurancePolicy policy = new InsurancePolicy();
        Date expireDate = getExpireDateFromContractReference(contract);

        policy.setExpiryDate(expireDate);
        policy.setStatus(getPolicyStatus(policy));
        policy.setPolicyNumber(getPolicyIdFromContractReference(contract));

        policy.setUsedMoney(getUsedMoney(response));
        policy.setAllowedMoney(getAllowedMoney(response));

        return policy;
    }

    @Override
    public String getPolicyIdFromContractReference(Reference contract) {
        return getElementFromContract(contract.getReference(), CONTRACT_POLICY_ID_ORDINAL);
    }

    @Override
    public Date getExpireDateFromContractReference(Reference contract) {
        String dateString = getElementFromContract(contract.getReference(), CONTRACT_EXPIRE_DATE_ORDINAL);
        String[] patterns = CONTRACT_DATE_PATTERN.toArray(new String[0]);
        return parseDate(dateString, patterns);
    }

    /**
     * Parses a date value.  The formats used for parsing the date value are retrieved from
     * the default http params.
     *
     * @param dateValue the date value to parse
     *
     * @return the parsed date or null if input could not be parsed
     */
    public static Date parseDate(final String dateValue) {
        return parseDate(dateValue, null, null);
    }

    /**
     * Parses the date value using the given date formats.
     *
     * @param dateValue the date value to parse
     * @param dateFormats the date formats to use
     *
     * @return the parsed date or null if input could not be parsed
     */
    public static Date parseDate(final String dateValue, final String[] dateFormats) {
        return parseDate(dateValue, dateFormats, null);
    }

    /**
     * Parses the date value using the given date formats.
     *
     * @param dateValue the date value to parse
     * @param dateFormats the date formats to use
     * @param startDate During parsing, two digit years will be placed in the range
     * {@code startDate} to {@code startDate + 100 years}. This value may
     * be {@code null}. When {@code null} is given as a parameter, year
     * {@code 2000} will be used.
     *
     * @return the parsed date or null if input could not be parsed
     */
    public static Date parseDate(
            final String dateValue,
            final String[] dateFormats,
            final Date startDate) {

        final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
        final String PATTERN_RFC1036 = "EEE, dd-MMM-yy HH:mm:ss zzz";
        final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";

        final String[] DEFAULT_PATTERNS = new String[] {
            PATTERN_RFC1123,
            PATTERN_RFC1036,
            PATTERN_ASCTIME
        };

        final TimeZone GMT = TimeZone.getTimeZone("GMT");
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(GMT);
        calendar.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        final Date DEFAULT_TWO_DIGIT_YEAR_START = calendar.getTime();

        notNull(dateValue, "Date value");
        final String[] localDateFormats = dateFormats != null ? dateFormats : DEFAULT_PATTERNS;
        final Date localStartDate = startDate != null ? startDate : DEFAULT_TWO_DIGIT_YEAR_START;
        String v = dateValue;
        // trim single quotes around date if present
        // see issue #5279
        if (v.length() > 1 && v.startsWith("'") && v.endsWith("'")) {
            v = v.substring (1, v.length() - 1);
        }

        for (final String dateFormat : localDateFormats) {
            final SimpleDateFormat dateParser = DateFormatHolder.formatFor(dateFormat);
            dateParser.set2DigitYearStart(localStartDate);
            final ParsePosition pos = new ParsePosition(0);
            final Date result = dateParser.parse(v, pos);
            if (pos.getIndex() != 0) {
                return result;
            }
        }
        return null;
    }

    public static <T> T notNull(final T argument, final String name) {
        if (argument == null) {
            throw new IllegalArgumentException(name + " may not be null");
        }
        return argument;
    }

    @Override
    public List<InsurancePolicyDTO> getForPerson(String personUuid) {
        if (StringUtils.isBlank(personUuid)) {
            throw new ValidationException(String.format("Wrong value of personUuid: %s", personUuid));
        }
        Criteria criteria = createOrderedQuery(personUuid);
        return insurancePolicyMapper.toDtos(getAllByCriteria(criteria, false));
    }

    @Override
    public List<InsurancePolicyDTO> addOrUpdatePolicy(String personUuid, InsurancePolicy policy) {
        if (StringUtils.isBlank(personUuid) || policy == null) {
            throw new ValidationException(String.format("Wrong value of personUuid: %s or InsurancePolicy %s",
                    personUuid, policy));
        }
        InsurancePolicy actualPolicy = getByPatientAndPolicyNumber(personUuid, policy.getPolicyNumber());
        Person person = personService.getPersonByUuid(personUuid);
        updatePersonAttributes(person, policy);
        assignPersonToPolicyIfMissing(policy, person);
        saveOrUpdate(updatePolicyAttributes(actualPolicy, policy));
        return getForPerson(personUuid);
    }

    private void assignPersonToPolicyIfMissing(InsurancePolicy policy, Person person) {
        if (policy.getPatient() == null && person instanceof Patient) {
            policy.setPatient((Patient) person);
        }
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setInsurancePolicyMapper(
            InsurancePolicyMapper insurancePolicyMapper) {
        this.insurancePolicyMapper = insurancePolicyMapper;
    }

    /**
     * Updates only the fields which should be changed but if actual policy does not exit then newPolicy will be returned
     * @param oldPolicy - the actual value of policy
     * @param newPolicy - the new value of policy
     */
    private InsurancePolicy updatePolicyAttributes(InsurancePolicy oldPolicy, InsurancePolicy newPolicy) {
        if (oldPolicy != null) {
            oldPolicy.setStartDate(newPolicy.getStartDate());
            oldPolicy.setExpiryDate(newPolicy.getExpiryDate());
            oldPolicy.setAllowedMoney(newPolicy.getAllowedMoney());
            oldPolicy.setUsedMoney(newPolicy.getUsedMoney());
            oldPolicy.setStatus(newPolicy.getStatus());
            return oldPolicy;
        } else {
            return newPolicy;
        }
    }

    private PersonAttribute createInsuranceNumberAttribute(String policyNumber) {
        return new PersonAttribute(
                personService.getPersonAttributeTypeByName(ConstantValues.POLICY_NUMBER_ATTRIBUTE_TYPE_NAME),
                policyNumber);
    }

    private Criteria createOrderedQuery(String personUuid) {
        Criteria criteria = createCriteria();
        criteria.createAlias("patient", "p")
                .add(Restrictions.eq("p.uuid", personUuid))
                .addOrder(Order.desc("dateCreated"))
                .addOrder(Order.desc("dateChanged").nulls(NullPrecedence.LAST));
        return criteria;
    }

    private InsurancePolicy getByPatientAndPolicyNumber(String personUuid, String policyNumber) {
        Criteria criteria = createCriteria();
        criteria.createAlias("patient", "p")
                .add(Restrictions.eq("p.uuid", personUuid))
                .add(Restrictions.eq("policyNumber", policyNumber));
        return getByCriteria(criteria, false);
    }

    private void updatePersonAttributes(Person person, InsurancePolicy policy) {
        person.addAttribute(createInsuranceNumberAttribute(policy.getPolicyNumber()));
    }

    private String getElementFromContract(String contract, int ordinal) {
        String[] contractParts = splitContract(contract);
        return contractParts[ordinal];
    }

    private String[] splitContract(String contract) {
        //Contract should have format: "Contract/policyId/expireDate
        return contract.split("/");
    }

    private InsurancePolicyStatus getPolicyStatus(InsurancePolicy policy) {
        Date policyExpireDate = policy.getExpiryDate();
        return isPolicyActive(policyExpireDate) ? InsurancePolicyStatus.ACTIVE : InsurancePolicyStatus.EXPIRED;
    }

    private boolean isPolicyActive(Date policyExpireDate) {
        Date today = Calendar.getInstance().getTime();
        return policyExpireDate.after(today);
    }

    private BigDecimal getAllowedMoney(CoverageEligibilityResponse response) throws FHIRException {
        // CoverageEligibilityResponse.BenefitsComponent benefit = response.getInsuranceFirstRep().getBenefitBalanceFirstRep();
        // Money allowedMoney = benefit.getFinancialFirstRep().getAllowedMoney();
        // return allowedMoney.getValue();
        BigDecimal ret = new BigDecimal(0);

        // Iterate through the insurance items to find financial details
        for (CoverageEligibilityResponse.InsuranceComponent insurance : response.getInsurance()) {
            for (CoverageEligibilityResponse.ItemsComponent item : insurance.getItem()) {
                for (CoverageEligibilityResponse.BenefitComponent benefit : item.getBenefit()) {
                    Money allowedMoney = benefit.getAllowedMoney();
                    if (allowedMoney != null) {
                        ret = ret.add(allowedMoney.getValue());
                    }
                }
            }
        }

        return(ret);
    }

    private BigDecimal getUsedMoney(CoverageEligibilityResponse response) throws FHIRException {
        // CoverageEligibilityResponse.BenefitComponent benefit = response.getInsuranceFirstRep().getBenefitPeriod();
        // Money usedMoney = benefit.getFinancialFirstRep().getUsedMoney();
        // return usedMoney.getValue();
        BigDecimal ret = new BigDecimal(0);

        // Iterate through the insurance items to find financial details
        for (CoverageEligibilityResponse.InsuranceComponent insurance : response.getInsurance()) {
            for (CoverageEligibilityResponse.ItemsComponent item : insurance.getItem()) {
                for (CoverageEligibilityResponse.BenefitComponent benefit : item.getBenefit()) {
                    Money usedMoney = benefit.getUsedMoney();
                    if (usedMoney != null) {
                        ret = ret.add(usedMoney.getValue());
                    }
                }
            }
        }

        return(ret);
    }

    /**
     * A factory for {@link SimpleDateFormat}s. The instances are stored in a
     * threadlocal way because SimpleDateFormat is not threadsafe as noted in
     * {@link SimpleDateFormat its javadoc}.
     *
     */
    final static class DateFormatHolder {

        private static final ThreadLocal<SoftReference<Map<String, SimpleDateFormat>>>
            THREADLOCAL_FORMATS = new ThreadLocal<SoftReference<Map<String, SimpleDateFormat>>>() {

            @Override
            protected SoftReference<Map<String, SimpleDateFormat>> initialValue() {
                return new SoftReference<Map<String, SimpleDateFormat>>(
                        new HashMap<String, SimpleDateFormat>());
            }

        };

        /**
         * creates a {@link SimpleDateFormat} for the requested format string.
         *
         * @param pattern
         *            a non-{@code null} format String according to
         *            {@link SimpleDateFormat}. The format is not checked against
         *            {@code null} since all paths go through
         *            {@link DateUtils}.
         * @return the requested format. This simple dateformat should not be used
         *         to {@link SimpleDateFormat#applyPattern(String) apply} to a
         *         different pattern.
         */
        public static SimpleDateFormat formatFor(final String pattern) {
            final SoftReference<Map<String, SimpleDateFormat>> ref = THREADLOCAL_FORMATS.get();
            Map<String, SimpleDateFormat> formats = ref.get();
            if (formats == null) {
                formats = new HashMap<String, SimpleDateFormat>();
                THREADLOCAL_FORMATS.set(
                        new SoftReference<Map<String, SimpleDateFormat>>(formats));
            }

            SimpleDateFormat format = formats.get(pattern);
            if (format == null) {
                format = new SimpleDateFormat(pattern, Locale.US);
                format.setTimeZone(TimeZone.getTimeZone("GMT"));
                formats.put(pattern, format);
            }

            return format;
        }

        public static void clearThreadLocal() {
            THREADLOCAL_FORMATS.remove();
        }

    }
}
