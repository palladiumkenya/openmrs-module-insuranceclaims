package org.openmrs.module.insuranceclaims.forms;

import ca.uhn.fhir.util.DateUtils;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaim;

import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.EXPECTED_DATE_PATTERN;

public class ValuatedClaimForm extends NewClaimForm {
    private List<ValuatedClaimItem> claimItems;
    private List<ValuatedClaimDiagnosis> claimDiagnoses;
    private InsuranceClaim claim;

    public ValuatedClaimForm(InsuranceClaim claim) {
        this.claim = claim;
        setClaimCode(claim.getClaimCode());
        setClaimExplanation(claim.getExplanation());
        setClaimJustification(claim.getAdjustment());
        setStartDate(formatDate(claim.getDateFrom(), EXPECTED_DATE_PATTERN));
        setEndDate(formatDate(claim.getDateTo(), EXPECTED_DATE_PATTERN));
        setLocation(claim.getLocation().getId().toString());

        setPaidInFacility(false);
        setPatient(claim.getPatient().getId().toString());
        setVisitType(claim.getVisitType().getName());
        setGuaranteeId(claim.getGuaranteeId());
        setProvider(claim.getProvider().getUuid());
        setInsurer(claim.getInsurer());
    }
    public void setClaimItems(List<ValuatedClaimItem> claimItems) {
        this.claimItems = claimItems;
    }

    public List<ValuatedClaimItem> getClaimItems() {
        return claimItems;
    }

    public void setClaimDiagnoses(List<ValuatedClaimDiagnosis> claimDiagnoses) {
        this.claimDiagnoses = claimDiagnoses;
    }

    public List<ValuatedClaimDiagnosis> getClaimDiagnoses() {
        return claimDiagnoses;
    }

    public InsuranceClaim getClaim() {
        return claim;
    }

    public void setClaim(InsuranceClaim claim) {
        this.claim = claim;
    }

    /**
     * Formats the given date according to the specified pattern.  The pattern
     * must conform to that used by the {@link SimpleDateFormat simple date
     * format} class.
     *
     * @param date The date to format.
     * @param pattern The pattern to use for formatting the date.
     * @return A formatted date string.
     *
     * @throws IllegalArgumentException If the given date pattern is invalid.
     *
     * @see SimpleDateFormat
     */
    public static String formatDate(final Date date, final String pattern) {
        notNull(date, "Date");
        notNull(pattern, "Pattern");
        final SimpleDateFormat formatter = DateFormatHolder.formatFor(pattern);
        return formatter.format(date);
    }

    public static <T> T notNull(final T argument, final String name) {
        if (argument == null) {
            throw new IllegalArgumentException(name + " may not be null");
        }
        return argument;
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
