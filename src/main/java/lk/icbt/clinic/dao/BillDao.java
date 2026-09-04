package lk.icbt.clinic.dao;

import lk.icbt.clinic.model.Bill;
import java.util.Optional;

public interface BillDao {

    Optional<Bill> findByAppointmentNo(String appointmentNo);

    Bill save(Bill bill);
}
