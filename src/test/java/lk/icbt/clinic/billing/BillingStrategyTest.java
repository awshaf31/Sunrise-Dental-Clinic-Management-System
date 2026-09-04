package lk.icbt.clinic.billing;

import lk.icbt.clinic.model.TreatmentCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The clinic does not price every treatment the same way, and the rules are
 * expected to keep changing. Each policy is tested in isolation, which is
 * what justifies expressing them as separate strategies.
 */
class BillingStrategyTest {

    @Test
    @DisplayName("a consultation-only visit is charged no treatment fee")
    void consultationOnlyChargesNoTreatmentFee() {
        assertThat(new ConsultationOnlyBilling().treatmentFee(new BigDecimal("3500.00")))
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("a standard treatment is charged its published base fee")
    void standardTreatmentChargesBaseFee() {
        assertThat(new StandardTreatmentBilling().treatmentFee(new BigDecimal("5000.00")))
                .isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("root canal therapy adds a 10% complexity surcharge")
    void rootCanalAddsComplexitySurcharge() {
        assertThat(new RootCanalBilling().treatmentFee(new BigDecimal("25000.00")))
                .isEqualByComparingTo("27500.00");
    }

    @Test
    @DisplayName("fees are rounded to two decimal places for currency")
    void feesAreRoundedToTwoDecimalPlaces() {
        BigDecimal fee = new RootCanalBilling().treatmentFee(new BigDecimal("333.33"));
        assertThat(fee).isEqualByComparingTo("366.66");
        assertThat(fee.scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("a zero base fee stays zero rather than becoming a surcharge")
    void zeroBaseFeeProducesZero() {
        assertThat(new RootCanalBilling().treatmentFee(BigDecimal.ZERO)).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("each strategy declares which treatments it prices")
    void strategiesDeclareTheTreatmentsTheyPrice() {
        assertThat(new ConsultationOnlyBilling().appliesTo()).containsExactly(TreatmentCode.CONSULT);
        assertThat(new RootCanalBilling().appliesTo()).containsExactly(TreatmentCode.ROOT_CANAL);
        assertThat(new StandardTreatmentBilling().appliesTo()).containsExactlyInAnyOrder(
                TreatmentCode.CLEANING, TreatmentCode.FILLING, TreatmentCode.EXTRACTION, TreatmentCode.WHITENING);
    }

    @Test
    @DisplayName("the three strategies together cover every treatment the clinic offers")
    void togetherCoverEveryTreatment() {
        List<BillingStrategy> all = List.of(
                new ConsultationOnlyBilling(), new StandardTreatmentBilling(), new RootCanalBilling());
        for (TreatmentCode code : TreatmentCode.values()) {
            assertThat(all).as("a strategy for %s", code)
                    .anySatisfy(s -> assertThat(s.appliesTo()).contains(code));
        }
    }
}
