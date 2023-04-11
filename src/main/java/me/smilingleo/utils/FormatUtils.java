package me.smilingleo.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class FormatUtils {

    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final Pattern RX_DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern RX_TIME = Pattern.compile("\\d{2}:\\d{2}:\\d{2}");
    private static final Pattern RX_TZ = Pattern.compile("(?:Z|(?:[+|-]\\d{1,2}:?\\d{2}))");

    private static final Map<String, String[]> DATE_TIME_PATTERNS_CACHE = new ConcurrentHashMap<>();

    static {
        DATE_TIME_PATTERNS_CACHE.put("ja_JP", new String[]{"yyyy/MM/dd", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_PE", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_CU", new String[]{"dd/MM/yyyy", "{1} {0}"}); // Spanish
        // Cuba,
        // java8 new
        // added.
        // http://demo.icu-project.org/icu4jweb/formatTest.jsp
        DATE_TIME_PATTERNS_CACHE.put("es_PA", new String[]{"MM/dd/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("sr_BA", new String[]{"yyyy-MM-dd", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_GT", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_AE", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("no_NO", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("sq_AL", new String[]{"yyyy-MM-dd", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_IQ", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_YE", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("pt_PT", new String[]{"dd-MM-yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("el_CY", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_QA", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("mk_MK", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("de_CH", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("en_US", new String[]{"MM/dd/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("fi_FI", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("en_MT", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("sl_SI", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("sk_SK", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("tr_TR", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_SA", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("en_GB", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("sr_CS", new String[]{"dd.MM.yyyy.", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("en_NZ", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_NI", new String[]{"MM-dd-yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ga_IE", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("fr_BE", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_ES", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_LB", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("fr_CA", new String[]{"yyyy-MM-dd", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("et_EE", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_KW", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("sr_RS", new String[]{"dd.MM.yyyy.", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_US", new String[]{"MM/dd/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_MX", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_SD", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("in_ID", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_UY", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("lv_LV", new String[]{"yyyy.dd.MM", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("pt_BR", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_SY", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_DO", new String[]{"MM/dd/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("fr_CH", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_VE", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_BH", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("en_PH", new String[]{"MM/dd/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_TN", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("de_AT", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("nl_NL", new String[]{"dd-MM-yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_EC", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("zh_TW", new String[]{"yyyy/MM/dd", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_JO", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("is_IS", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_CO", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_CR", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_CL", new String[]{"dd-MM-yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_EG", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("en_ZA", new String[]{"yyyy/MM/dd", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("th_TH", new String[]{"dd/MM/yyyy", "{1}, {0}"});
        DATE_TIME_PATTERNS_CACHE.put("el_GR", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("it_IT", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("hu_HU", new String[]{"yyyy.MM.dd.", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("en_IE", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("uk_UA", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("pl_PL", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("fr_LU", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("nl_BE", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("en_IN", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ca_ES", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_MA", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_BO", new String[]{"dd-MM-yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("en_AU", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("zh_SG", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_SV", new String[]{"MM-dd-yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ru_RU", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ko_KR", new String[]{"yyyy. MM. dd", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_DZ", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("vi_VN", new String[]{"dd/MM/yyyy", "{0} {1}"});
        DATE_TIME_PATTERNS_CACHE.put("sr_ME", new String[]{"dd.MM.yyyy.", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_LY", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("zh_CN", new String[]{"yyyy-MM-dd", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("be_BY", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("iw_IL", new String[]{"dd/MM/yyyy", "{0} {1}"});
        DATE_TIME_PATTERNS_CACHE.put("bg_BG", new String[]{"yyyy-MM-dd", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("mt_MT", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_PY", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("fr_FR", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("cs_CZ", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("it_CH", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ro_RO", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_PR", new String[]{"MM-dd-yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("en_CA", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("de_DE", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("de_GR", new String[]{"dd.MM.yyyy", "{1} {0}"}); // German
        // Greece,
        // java8
        // added.
        DATE_TIME_PATTERNS_CACHE.put("de_LU", new String[]{"dd.MM.yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_AR", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ms_MY", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("en_SG", new String[]{"MM/dd/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("ar_OM", new String[]{"dd/MM/yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("sv_SE", new String[]{"yyyy-MM-dd", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("da_DK", new String[]{"dd-MM-yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("es_HN", new String[]{"MM-dd-yyyy", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("lt_LT", new String[]{"yyyy-MM-dd", "{1} {0}"});
        DATE_TIME_PATTERNS_CACHE.put("hr_HR", new String[]{"dd.MM.yyyy.", "{1} {0}"});
    }

    /**
     * Localize the input according to the locale parameter. It works for numeric and temporal data fields.
     *
     * In case the input is not localizable, return it as is.
     * It's more user-friendly not to throw exception since in context of presentment.
     * @param input
     * @param localeStr
     * @return
     */
    public static String localiseDateTime(String input, String localeStr) {
        String dateStr = null;
        String timeStr = null;
        String timezoneStr = null;

        int length = input.length();
        if (length >= 10) {
            String part1 = input.substring(0, 10);
            dateStr = RX_DATE.matcher(part1).matches() ? part1 : null;
            if (dateStr == null) {
                return input;
            }

            String part2 = length >= 11 ? input.substring(10, 11) : null;
            if (part2 != null && !((part2.equals(" ")) || part2.equals("T"))) {
                return input;
            }
            String part3 = length >= 19 ? input.substring(11, 19) : null;
            timeStr = part3 != null && RX_TIME.matcher(part3).matches() ? part3 : null;
            if (part3 != null && timeStr == null) {
                return input;
            }
            // ignore the milliseconds. 2020-01-01T00:00:00.000Z
            String part4 = length >= 20 && input.charAt(19) == '.' ? input.substring(23) :
                    length >= 20 ? input.substring(19) : null;
            timezoneStr = part4 != null && RX_TZ.matcher(part4).matches() ? part4 : null;
            if (part4 != null && timezoneStr == null) {
                return input;
            }
            try {
                if (StringUtils.isNullOrBlank(timeStr) && StringUtils.isNullOrBlank(timezoneStr)) {
                    Locale locale = fromString(localeStr);
                    Date date = new SimpleDateFormat(DATE_PATTERN, locale).parse(dateStr);
                    String[] formatArray = DATE_TIME_PATTERNS_CACHE.get(localeStr);
                    String dateFormatPattern = formatArray[0];
                    return new SimpleDateFormat(dateFormatPattern, locale).format(date);
                } else if (StringUtils.isNullOrBlank(timezoneStr)) {
                    Locale locale = fromString(localeStr);
                    Date date = new SimpleDateFormat(DATE_PATTERN + " HH:mm:ss", locale)
                            .parse(dateStr + " " + timeStr);
                    String[] formatArray = DATE_TIME_PATTERNS_CACHE.get(localeStr);
                    String datetimeFormatPattern = formatArray[0] + " " + TIME_PATTERN;
                    return new SimpleDateFormat(datetimeFormatPattern, locale).format(date);
                } else {
                    Locale locale = fromString(localeStr);
                    Date date = new SimpleDateFormat(DATE_PATTERN + " HH:mm:ss", locale)
                            .parse(dateStr + " " + timeStr);
                    String[] formatArray = DATE_TIME_PATTERNS_CACHE.get(localeStr);
                    String datetimeFormatPattern = formatArray[0] + " " + TIME_PATTERN;
                    return new SimpleDateFormat(datetimeFormatPattern, locale).format(date) + timezoneStr;
                }
            } catch (ParseException e) {
                return input;
            }
        } else {
            return input;
        }
    }

    public static String localiseNumber(String numberInput, String localeStr) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(fromString(localeStr));
        // To make sure no rounding, DecimalFormat.DOUBLE_FRACTION_DIGITS = 340
        int periodPos = numberInput.lastIndexOf('.');
        int fractionDigits = 0;
        if (periodPos > 0) {
            fractionDigits = numberInput.length() - periodPos - 1;
        }
        if (fractionDigits > 0) {
            fractionDigits = fractionDigits > 340 ? 340 : fractionDigits;
            numberFormat.setMaximumFractionDigits(fractionDigits);
            numberFormat.setMinimumFractionDigits(fractionDigits);
        }
        try {
            return numberFormat.format(Double.parseDouble(numberInput));
        } catch (ArithmeticException e) {
            return numberInput;
        }
    }

    public static Locale fromString(String locale) {
        String[] parts = locale.split("_", -1);
        if (parts.length == 1) {
            return new Locale(parts[0]);
        } else if (parts.length == 2
                || (parts.length == 3 && parts[2].startsWith("#"))) {
            return new Locale(parts[0], parts[1]);
        } else {
            return new Locale(parts[0], parts[1], parts[2]);
        }
    }
}
