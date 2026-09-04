package lk.icbt.clinic.model;

import java.math.BigDecimal;

/**
 * A treatment the clinic offers, and its published fee. The fee lives here
 * rather than in code so the clinic can reprice without a redeployment.
 */
public class TreatmentType {

    private Long id;
    private TreatmentCode code;
    private String name;
    private BigDecimal baseFee;
    private boolean active = true;

    public TreatmentType() {
    }

    public TreatmentType(TreatmentCode code, String name, BigDecimal baseFee) {
        this.code = code;
        this.name = name;
        this.baseFee = baseFee;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TreatmentCode getCode() { return code; }
    public void setCode(TreatmentCode code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getBaseFee() { return baseFee; }
    public void setBaseFee(BigDecimal baseFee) { this.baseFee = baseFee; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
