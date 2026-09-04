package lk.icbt.clinic.util;

import lk.icbt.clinic.billing.BillingStrategy;
import lk.icbt.clinic.billing.BillingStrategyFactory;
import lk.icbt.clinic.billing.ConsultationOnlyBilling;
import lk.icbt.clinic.billing.RootCanalBilling;
import lk.icbt.clinic.billing.StandardTreatmentBilling;
import lk.icbt.clinic.dao.*;
import lk.icbt.clinic.service.AppointmentService;
import lk.icbt.clinic.service.AuthService;
import lk.icbt.clinic.service.BillingService;
import lk.icbt.clinic.service.ReportService;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;

/**
 * The composition root: the one place dependencies are wired together by
 * hand.
 * <p>
 * Spring's {@code ApplicationContext} does this work automatically, scanning
 * for {@code @Component} classes and injecting their collaborators. Without
 * a framework, that wiring has to happen somewhere explicit -- this class is
 * that "somewhere". Every constructor call below is exactly the dependency
 * injection Spring would otherwise perform behind an annotation; the
 * difference is that it is visible, in order, in one file.
 * <p>
 * Built once by {@link AppInitializer} when the web application starts, and
 * held in the {@code ServletContext} for every servlet to share.
 */
public final class AppContext {

    public final Clock clock;

    public final StaffDao staffDao;
    public final DentistDao dentistDao;
    public final TreatmentTypeDao treatmentTypeDao;
    public final PatientDao patientDao;
    public final AppointmentDao appointmentDao;
    public final BillDao billDao;
    public final ClinicSettingDao clinicSettingDao;
    public final ReportDao reportDao;

    public final BillingStrategyFactory billingStrategyFactory;

    public final AuthService authService;
    public final AppointmentService appointmentService;
    public final BillingService billingService;
    public final ReportService reportService;

    public AppContext() {
        this.clock = Clock.system(ZoneId.of("Asia/Colombo"));

        this.staffDao = new JdbcStaffDao();
        this.dentistDao = new JdbcDentistDao();
        this.treatmentTypeDao = new JdbcTreatmentTypeDao();
        this.patientDao = new JdbcPatientDao();
        this.appointmentDao = new JdbcAppointmentDao();
        this.billDao = new JdbcBillDao();
        this.clinicSettingDao = new JdbcClinicSettingDao();
        this.reportDao = new JdbcReportDao();

        List<BillingStrategy> strategies = List.of(
                new ConsultationOnlyBilling(),
                new StandardTreatmentBilling(),
                new RootCanalBilling());
        this.billingStrategyFactory = new BillingStrategyFactory(strategies);

        this.authService = new AuthService(staffDao);
        this.appointmentService = new AppointmentService(
                appointmentDao, patientDao, dentistDao, treatmentTypeDao, staffDao, clock);
        this.billingService = new BillingService(
                billDao, appointmentDao, clinicSettingDao, staffDao, billingStrategyFactory);
        this.reportService = new ReportService(reportDao, appointmentDao);
    }

    /**
     * Test-only constructor: lets a servlet test supply mocked services
     * directly, without the production constructor's JDBC DAOs ever
     * connecting to a real database. This is the servlet-testing equivalent
     * of what Spring's {@code @MockBean} does inside a loaded application
     * context -- built by hand here, since there is no context to load.
     */
    public AppContext(AuthService authService, AppointmentService appointmentService,
                      BillingService billingService, ReportService reportService,
                      DentistDao dentistDao, TreatmentTypeDao treatmentTypeDao) {
        this.clock = Clock.systemDefaultZone();
        this.staffDao = null;
        this.dentistDao = dentistDao;
        this.treatmentTypeDao = treatmentTypeDao;
        this.patientDao = null;
        this.appointmentDao = null;
        this.billDao = null;
        this.clinicSettingDao = null;
        this.reportDao = null;
        this.billingStrategyFactory = null;
        this.authService = authService;
        this.appointmentService = appointmentService;
        this.billingService = billingService;
        this.reportService = reportService;
    }
}
