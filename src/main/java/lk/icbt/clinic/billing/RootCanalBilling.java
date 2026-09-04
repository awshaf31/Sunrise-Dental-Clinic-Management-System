package lk.icbt.clinic.billing;

import lk.icbt.clinic.model.TreatmentCode;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

/** Root canal therapy runs over multiple visits and carries a complexity surcharge. */
public class RootCanalBilling implements BillingStrategy {

    private static final BigDecimal COMPLEXITY_SURCHARGE = new BigDecimal("0.10");

    @Override
    public Set<TreatmentCode> appliesTo() {
        return EnumSet.of(TreatmentCode.ROOT_CANAL);
    }

    @Override
    public BigDecimal treatmentFee(BigDecimal baseFee) {
        BigDecimal surcharge = baseFee.multiply(COMPLEXITY_SURCHARGE);
        return toCurrency(baseFee.add(surcharge));
    }
}
