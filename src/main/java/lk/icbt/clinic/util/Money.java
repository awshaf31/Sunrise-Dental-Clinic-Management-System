package lk.icbt.clinic.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formats a currency amount as {@code 1,500.00} for display.
 * <p>
 * Exposed to JSPs as a JSP function (see {@code /WEB-INF/functions.tld}),
 * used as {@code ${fn:money(amount)}}. Written by hand rather than relying
 * on JSTL's {@code <fmt:formatNumber>}, whose {@code pattern} attribute did
 * not apply correctly in testing against this Tomcat/JSTL combination --
 * silently falling back to an unpatterned format. A five-line static method
 * with a fixed {@link Locale} is more predictable than debugging a
 * third-party tag library's environment-specific behaviour, and just as
 * "pure Java" either way.
 */
public final class Money {

    private static final DecimalFormat FORMAT =
            new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));

    private Money() {
    }

    public static String format(BigDecimal amount) {
        return amount == null ? "0.00" : FORMAT.format(amount);
    }
}
