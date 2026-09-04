package lk.icbt.clinic.model;

/**
 * A configurable clinic parameter -- the consultation fee, opening hours.
 * Held as data so the manager can change them without a code release.
 */
public class ClinicSetting {

    private String key;
    private String value;
    private String description;

    public ClinicSetting() {
    }

    public ClinicSetting(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
