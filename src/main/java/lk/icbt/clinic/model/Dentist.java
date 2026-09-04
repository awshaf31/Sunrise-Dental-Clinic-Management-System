package lk.icbt.clinic.model;

/** A dentist patients can be booked with. */
public class Dentist {

    private Long id;
    private String name;
    private String specialization;
    private boolean active = true;

    public Dentist() {
    }

    public Dentist(String name, String specialization) {
        this.name = name;
        this.specialization = specialization;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
