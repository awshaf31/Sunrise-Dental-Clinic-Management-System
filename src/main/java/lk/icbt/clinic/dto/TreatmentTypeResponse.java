package lk.icbt.clinic.dto;

import lk.icbt.clinic.model.TreatmentType;
import java.math.BigDecimal;

public class TreatmentTypeResponse {
    public final Long id;
    public final String code;
    public final String name;
    public final BigDecimal baseFee;

    public TreatmentTypeResponse(TreatmentType t) {
        this.id = t.getId();
        this.code = t.getCode().name();
        this.name = t.getName();
        this.baseFee = t.getBaseFee();
    }
}
