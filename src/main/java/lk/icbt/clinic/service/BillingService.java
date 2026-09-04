package lk.icbt.clinic.service;

import lk.icbt.clinic.billing.BillingStrategy;
import lk.icbt.clinic.billing.BillingStrategyFactory;
import lk.icbt.clinic.dao.AppointmentDao;
import lk.icbt.clinic.dao.BillDao;
import lk.icbt.clinic.dao.ClinicSettingDao;
import lk.icbt.clinic.dao.StaffDao;
import lk.icbt.clinic.exception.AppointmentNotFoundException;
import lk.icbt.clinic.model.Appointment;
import lk.icbt.clinic.model.Bill;
import lk.icbt.clinic.model.Staff;

import java.math.BigDecimal;

/**
 * Raises the bill for an appointment.
 * <p>
 * The service decides <em>what</em> makes up a bill — a consultation fee plus
 * a treatment charge — while the {@link BillingStrategy} for the treatment
 * decides <em>how much</em> that treatment charge is. Repricing a treatment
 * therefore never touches this class.
 */
public class BillingService {

    private static final String CONSULTATION_FEE_KEY = "CONSULTATION_FEE";

    private final BillDao billDao;
    private final AppointmentDao appointmentDao;
    private final ClinicSettingDao settingDao;
    private final StaffDao staffDao;
    private final BillingStrategyFactory strategyFactory;

    public BillingService(BillDao billDao, AppointmentDao appointmentDao,
                          ClinicSettingDao settingDao, StaffDao staffDao,
                          BillingStrategyFactory strategyFactory) {
        this.billDao = billDao;
        this.appointmentDao = appointmentDao;
        this.settingDao = settingDao;
        this.staffDao = staffDao;
        this.strategyFactory = strategyFactory;
    }

    public Bill issueBill(String appointmentNo, Long staffId) {
        // Reprinting a receipt must not create a second billable record,
        // which would double-count the visit in the revenue report.
        return billDao.findByAppointmentNo(appointmentNo)
                .orElseGet(() -> createBill(appointmentNo, staffId));
    }

    public Bill findByAppointmentNo(String appointmentNo) {
        return billDao.findByAppointmentNo(appointmentNo)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "No bill has been issued for appointment " + appointmentNo));
    }

    private Bill createBill(String appointmentNo, Long staffId) {
        Appointment appointment = appointmentDao.findByAppointmentNo(appointmentNo)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "No appointment found with number " + appointmentNo));

        BigDecimal consultationFee = consultationFee();

        BillingStrategy strategy = strategyFactory.strategyFor(appointment.getTreatmentType().getCode());
        BigDecimal treatmentFee = strategy.treatmentFee(appointment.getTreatmentType().getBaseFee());

        Staff issuedBy = staffDao.findById(staffId)
                .orElseThrow(() -> new IllegalStateException("No staff with id " + staffId));

        try {
            return billDao.save(new Bill(appointment, consultationFee, treatmentFee, issuedBy));
        } catch (IllegalStateException raceLost) {
            // Another request won the uk_bill_appointment race between the
            // findByAppointmentNo check above and this save; re-read rather
            // than fail, since the winning bill is exactly what the caller
            // wanted anyway.
            return findByAppointmentNo(appointmentNo);
        }
    }

    /**
     * Read from configuration rather than hard-coded, so the clinic can
     * change it without a release. A missing value is a misconfiguration,
     * not a free consultation, so it fails rather than defaulting to zero.
     */
    private BigDecimal consultationFee() {
        return settingDao.findByKey(CONSULTATION_FEE_KEY)
                .map(setting -> new BigDecimal(setting.getValue()))
                .orElseThrow(() -> new IllegalStateException(
                        CONSULTATION_FEE_KEY + " is not configured in clinic_setting"));
    }
}
