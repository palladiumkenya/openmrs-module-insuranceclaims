package org.openmrs.module.insuranceclaims.forms;

import ca.uhn.fhir.util.DateUtils;
import org.openmrs.module.insuranceclaims.api.model.InsuranceClaimItem;

import static org.openmrs.module.insuranceclaims.ClaimUtils.buildItemName;
import static org.openmrs.module.insuranceclaims.api.service.fhir.util.InsuranceClaimConstants.EXPECTED_DATE_PATTERN;

import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ValuatedClaimItem {
    private String itemName;
    private String itemUuid;
    private String dateServed;
    private String explanation;
    private InsuranceClaimItem item;

    public ValuatedClaimItem() {}

    public ValuatedClaimItem(InsuranceClaimItem item) {
        this.item = item;
        this.itemName = buildItemName(item.getItem());
        this.itemUuid = item.getUuid();
        this.explanation = item.getExplanation();
        this.dateServed = formatDate(item.getItem().getDateOfServed(), EXPECTED_DATE_PATTERN);
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemUuid() {
        return itemUuid;
    }

    public void setItemUuid(String itemUuid) {
        this.itemUuid = itemUuid;
    }

    public void setDateServed(String dateServed) {
        this.dateServed = dateServed;
    }

    public String getDateServed() {
        return dateServed;
    }

    public InsuranceClaimItem getItem() {
        return this.item;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getExplanation() {
        return this.explanation;
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
