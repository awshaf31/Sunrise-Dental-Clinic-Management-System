package lk.icbt.clinic.billing;

import lk.icbt.clinic.model.TreatmentCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BillingStrategyFactoryTest {

    private BillingStrategyFactory factory;

    @BeforeEach
    void setUp() {
        factory = new BillingStrategyFactory(List.of(
                new ConsultationOnlyBilling(), new StandardTreatmentBilling(), new RootCanalBilling()));
    }

    @Test
    @DisplayName("resolves the strategy that declares the requested treatment")
    void resolvesTheDeclaringStrategy() {
        assertThat(factory.strategyFor(TreatmentCode.CONSULT)).isInstanceOf(ConsultationOnlyBilling.class);
        assertThat(factory.strategyFor(TreatmentCode.ROOT_CANAL)).isInstanceOf(RootCanalBilling.class);
        assertThat(factory.strategyFor(TreatmentCode.FILLING)).isInstanceOf(StandardTreatmentBilling.class);
    }

    @Test
    @DisplayName("every treatment the clinic offers can be priced")
    void everyTreatmentCodeIsCovered() {
        for (TreatmentCode code : TreatmentCode.values()) {
            assertThat(factory.strategyFor(code)).as("strategy for %s", code).isNotNull();
        }
    }

    @Test
    @DisplayName("rejects two strategies claiming the same treatment")
    void rejectsAmbiguousRegistration() {
        assertThatThrownBy(() -> new BillingStrategyFactory(
                List.of(new RootCanalBilling(), new RootCanalBilling())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ROOT_CANAL");
    }
}
