package lk.icbt.clinic.billing;

import lk.icbt.clinic.model.TreatmentCode;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

/** A visit where the patient was examined but no treatment was carried out. */
public class ConsultationOnlyBilling implements BillingStrategy {

    @Override
    public Set<TreatmentCode> appliesTo() {
        return EnumSet.of(TreatmentCode.CONSULT);
    }

    @Override
    public BigDecimal treatmentFee(BigDecimal baseFee) {
        return toCurrency(BigDecimal.ZERO);
    }
}
