package lk.icbt.clinic.billing;

import lk.icbt.clinic.model.TreatmentCode;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the pricing policy for a treatment.
 * <p>
 * Without Spring there is no container to discover {@code BillingStrategy}
 * implementations automatically, so the list is assembled by hand once, in
 * {@link lk.icbt.clinic.util.AppContext} -- the composition root. Each
 * strategy still declares the treatments it handles through
 * {@link BillingStrategy#appliesTo()}, so adding a pricing rule means adding
 * one class and one line in {@code AppContext}; this factory itself is never
 * edited.
 */
public class BillingStrategyFactory {

    private final Map<TreatmentCode, BillingStrategy> byTreatment = new EnumMap<>(TreatmentCode.class);

    public BillingStrategyFactory(List<BillingStrategy> strategies) {
        for (BillingStrategy strategy : strategies) {
            for (TreatmentCode code : strategy.appliesTo()) {
                BillingStrategy existing = byTreatment.putIfAbsent(code, strategy);
                if (existing != null) {
                    throw new IllegalStateException(
                            "Two billing strategies claim " + code + ": "
                                    + existing.getClass().getSimpleName() + " and "
                                    + strategy.getClass().getSimpleName());
                }
            }
        }
    }

    public BillingStrategy strategyFor(TreatmentCode code) {
        BillingStrategy strategy = byTreatment.get(code);
        if (strategy == null) {
            throw new IllegalStateException("No billing strategy is registered for " + code);
        }
        return strategy;
    }
}
