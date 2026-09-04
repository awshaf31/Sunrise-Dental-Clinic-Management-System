package lk.icbt.clinic.dao;

import lk.icbt.clinic.model.ClinicSetting;
import java.util.Optional;

public interface ClinicSettingDao {

    Optional<ClinicSetting> findByKey(String key);
}
