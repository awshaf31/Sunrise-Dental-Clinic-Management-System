package lk.icbt.clinic.dto;

import lk.icbt.clinic.model.Dentist;

public class DentistResponse {
    public final Long id;
    public final String name;
    public final String specialization;

    public DentistResponse(Dentist d) {
        this.id = d.getId();
        this.name = d.getName();
        this.specialization = d.getSpecialization();
    }
}
