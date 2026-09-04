package lk.icbt.clinic.servlet;

import lk.icbt.clinic.dto.DailyScheduleRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DayRailBuilderTest {

    private DailyScheduleRow row(int hour, String patient, String status) {
        return new DailyScheduleRow(LocalTime.of(hour, 0), "APT-1", patient, "0771234567",
                "Dr. Ruwan Silva", "Tooth Filling", status);
    }

    @Test
    @DisplayName("covers every opening hour from 09:00 to 16:00")
    void coversOpeningHours() {
        List<DayRailBuilder.DaySlot> slots = DayRailBuilder.build(List.of());
        assertThat(slots).hasSize(8);
        assertThat(slots.get(0).time()).isEqualTo("09:00");
        assertThat(slots.get(7).time()).isEqualTo("16:00");
        assertThat(slots).allMatch(s -> !s.isTaken());
    }

    @Test
    @DisplayName("marks a booked hour as taken and names the patient")
    void marksBookedHours() {
        List<DayRailBuilder.DaySlot> slots = DayRailBuilder.build(List.of(row(11, "Kamal Jayasuriya", "SCHEDULED")));
        var eleven = slots.stream().filter(s -> s.time().equals("11:00")).findFirst().orElseThrow();
        assertThat(eleven.isTaken()).isTrue();
        assertThat(eleven.patientName()).isEqualTo("Kamal Jayasuriya");
    }

    @Test
    @DisplayName("a cancelled appointment leaves its slot free")
    void cancelledAppointmentReleasesTheSlot() {
        List<DayRailBuilder.DaySlot> slots = DayRailBuilder.build(List.of(row(14, "Kamal Jayasuriya", "CANCELLED")));
        var two = slots.stream().filter(s -> s.time().equals("14:00")).findFirst().orElseThrow();
        assertThat(two.isTaken()).isFalse();
    }

    @Test
    @DisplayName("a completed appointment still occupies its slot")
    void completedAppointmentStillOccupiesTheSlot() {
        assertThat(DayRailBuilder.build(List.of(row(9, "Kamal Jayasuriya", "COMPLETED"))).get(0).isTaken()).isTrue();
    }
}
