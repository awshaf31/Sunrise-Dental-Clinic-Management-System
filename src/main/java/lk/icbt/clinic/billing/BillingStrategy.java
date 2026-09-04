package lk.icbt.clinic.billing;

import lk.icbt.clinic.model.TreatmentCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * How one family of treatments is priced.
 * <p>
 * The clinic prices treatments by different rules — some at a flat published
 * fee, some free of a treatment charge, some with a surcharge — and expects
 * those rules to change independently of one another. Expressing each rule as
 * its own implementation means a repricing touches a single class, and adding
 * a treatment adds a class rather than another branch in a method everything
 * else already depends on.
 */
public interface BillingStrategy {

    int CURRENCY_SCALE = 2;
    RoundingMode CURRENCY_ROUNDING = RoundingMode.HALF_UP;

    /** The treatments this strategy prices. Used by {@link BillingStrategyFactory}. */
    Set<TreatmentCode> appliesTo();

    /**
     * The treatment portion of the bill. The consultation fee is added
     * separately, because it is charged on every visit regardless of treatment.
     */
    BigDecimal treatmentFee(BigDecimal baseFee);

    default BigDecimal toCurrency(BigDecimal amount) {
        return amount.setScale(CURRENCY_SCALE, CURRENCY_ROUNDING);
    }
}
