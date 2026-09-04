package lk.icbt.clinic.service;

import lk.icbt.clinic.billing.BillingStrategyFactory;
import lk.icbt.clinic.billing.ConsultationOnlyBilling;
import lk.icbt.clinic.billing.RootCanalBilling;
import lk.icbt.clinic.billing.StandardTreatmentBilling;
import lk.icbt.clinic.dao.AppointmentDao;
import lk.icbt.clinic.dao.BillDao;
import lk.icbt.clinic.dao.ClinicSettingDao;
import lk.icbt.clinic.dao.StaffDao;
import lk.icbt.clinic.exception.AppointmentNotFoundException;
import lk.icbt.clinic.model.Appointment;
import lk.icbt.clinic.model.Bill;
import lk.icbt.clinic.model.ClinicSetting;
import lk.icbt.clinic.model.Dentist;
import lk.icbt.clinic.model.Patient;
import lk.icbt.clinic.model.Staff;
import lk.icbt.clinic.model.StaffRole;
import lk.icbt.clinic.model.TreatmentCode;
import lk.icbt.clinic.model.TreatmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock private BillDao billDao;
    @Mock private AppointmentDao appointmentDao;
    @Mock private ClinicSettingDao settingDao;
    @Mock private StaffDao staffDao;

    private BillingService service;
    private Staff staff;

    @BeforeEach
    void setUp() {
        BillingStrategyFactory factory = new BillingStrategyFactory(List.of(
                new ConsultationOnlyBilling(), new StandardTreatmentBilling(), new RootCanalBilling()));
        service = new BillingService(billDao, appointmentDao, settingDao, staffDao, factory);
        staff = new Staff("reception", "hash", "Nimali Perera", StaffRole.RECEPTIONIST);
        staff.setId(1L);
    }

    private Appointment appointmentFor(TreatmentCode code, String baseFee) {
        TreatmentType t = new TreatmentType(code, code.name(), new BigDecimal(baseFee));
        t.setId(1L);
        Dentist d = new Dentist("Dr. Ruwan Silva", "General Dentistry");
        d.setId(1L);
        Patient p = new Patient("PAT-1", "Kamal Jayasuriya", "12 Galle Road", "0771234567");
        p.setId(1L);
        Appointment a = new Appointment("APT-20260902-001", p, d, t,
                LocalDate.of(2026, 9, 2), LocalTime.of(10, 0), staff);
        a.setId(1L);
        return a;
    }

    private void consultationFeeIs(String amount) {
        when(settingDao.findByKey("CONSULTATION_FEE")).thenReturn(Optional.of(
                new ClinicSetting("CONSULTATION_FEE", amount, null)));
    }

    @Test
    @DisplayName("a bill is the consultation fee plus the treatment fee")
    void totalsConsultationAndTreatment() {
        consultationFeeIs("1500.00");
        when(appointmentDao.findByAppointmentNo("APT-20260902-001"))
                .thenReturn(Optional.of(appointmentFor(TreatmentCode.FILLING, "5000.00")));
        when(billDao.findByAppointmentNo(any())).thenReturn(Optional.empty());
        when(staffDao.findById(1L)).thenReturn(Optional.of(staff));
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        Bill bill = service.issueBill("APT-20260902-001", 1L);

        assertThat(bill.getConsultationFee()).isEqualByComparingTo("1500.00");
        assertThat(bill.getTreatmentFee()).isEqualByComparingTo("5000.00");
        assertThat(bill.getTotalAmount()).isEqualByComparingTo("6500.00");
    }

    @Test
    @DisplayName("the root canal surcharge reaches the bill")
    void appliesTheStrategysSurcharge() {
        consultationFeeIs("1500.00");
        when(appointmentDao.findByAppointmentNo("APT-20260902-001"))
                .thenReturn(Optional.of(appointmentFor(TreatmentCode.ROOT_CANAL, "25000.00")));
        when(billDao.findByAppointmentNo(any())).thenReturn(Optional.empty());
        when(staffDao.findById(1L)).thenReturn(Optional.of(staff));
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        Bill bill = service.issueBill("APT-20260902-001", 1L);

        assertThat(bill.getTreatmentFee()).isEqualByComparingTo("27500.00");
        assertThat(bill.getTotalAmount()).isEqualByComparingTo("29000.00");
    }

    @Test
    @DisplayName("a consultation-only visit is billed the consultation fee alone")
    void consultationOnlyBillsTheConsultationFee() {
        consultationFeeIs("1500.00");
        when(appointmentDao.findByAppointmentNo("APT-20260902-001"))
                .thenReturn(Optional.of(appointmentFor(TreatmentCode.CONSULT, "0.00")));
        when(billDao.findByAppointmentNo(any())).thenReturn(Optional.empty());
        when(staffDao.findById(1L)).thenReturn(Optional.of(staff));
        when(billDao.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        Bill bill = service.issueBill("APT-20260902-001", 1L);

        assertThat(bill.getTreatmentFee()).isEqualByComparingTo("0.00");
        assertThat(bill.getTotalAmount()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("reprinting a receipt returns the original bill rather than issuing a second")
    void reprintingReturnsTheExistingBill() {
        Appointment appointment = appointmentFor(TreatmentCode.FILLING, "5000.00");
        Bill existing = new Bill(appointment, new BigDecimal("1500.00"), new BigDecimal("5000.00"), staff);
        when(billDao.findByAppointmentNo("APT-20260902-001")).thenReturn(Optional.of(existing));

        Bill bill = service.issueBill("APT-20260902-001", 1L);

        assertThat(bill).isSameAs(existing);
        verify(billDao, never()).save(any());
    }

    @Test
    @DisplayName("billing an unknown appointment fails clearly")
    void unknownAppointmentFails() {
        when(billDao.findByAppointmentNo("APT-NOPE")).thenReturn(Optional.empty());
        when(appointmentDao.findByAppointmentNo("APT-NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.issueBill("APT-NOPE", 1L))
                .isInstanceOf(AppointmentNotFoundException.class);
    }

    @Test
    @DisplayName("a missing consultation fee setting fails loudly rather than billing zero")
    void missingConsultationFeeFails() {
        when(settingDao.findByKey("CONSULTATION_FEE")).thenReturn(Optional.empty());
        when(appointmentDao.findByAppointmentNo("APT-20260902-001"))
                .thenReturn(Optional.of(appointmentFor(TreatmentCode.FILLING, "5000.00")));
        when(billDao.findByAppointmentNo(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.issueBill("APT-20260902-001", 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CONSULTATION_FEE");
    }
}
