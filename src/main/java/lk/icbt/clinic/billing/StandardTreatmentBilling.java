package lk.icbt.clinic.billing;

import lk.icbt.clinic.model.TreatmentCode;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

/** Treatments billed at their published fee with no adjustment. */
public class StandardTreatmentBilling implements BillingStrategy {

    @Override
    public Set<TreatmentCode> appliesTo() {
        return EnumSet.of(
                TreatmentCode.CLEANING, TreatmentCode.FILLING,
                TreatmentCode.EXTRACTION, TreatmentCode.WHITENING);
    }

    @Override
    public BigDecimal treatmentFee(BigDecimal baseFee) {
        return toCurrency(baseFee);
    }
}
