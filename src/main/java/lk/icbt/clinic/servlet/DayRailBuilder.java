package lk.icbt.clinic.servlet;

import lk.icbt.clinic.dto.DailyScheduleRow;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Turns a day's appointments into the ruled column the receptionist reads.
 * <p>
 * A plain static method rather than an injected service: it has no
 * dependencies of its own, so there is nothing for a constructor to receive.
 */
public final class DayRailBuilder {

    private static final LocalTime OPENING = LocalTime.of(9, 0);
    private static final LocalTime CLOSING = LocalTime.of(17, 0);

    private DayRailBuilder() {
    }

    public static List<DaySlot> build(List<DailyScheduleRow> schedule) {
        List<DaySlot> slots = new ArrayList<>();
        for (LocalTime hour = OPENING; hour.isBefore(CLOSING); hour = hour.plusHours(1)) {
            slots.add(new DaySlot(format(hour), holderOf(schedule, hour)));
        }
        return slots;
    }

    /**
     * A cancelled appointment releases its slot, so it must not count as
     * taken -- otherwise cancelling would quietly make the hour unbookable.
     */
    private static String holderOf(List<DailyScheduleRow> schedule, LocalTime hour) {
        return schedule.stream()
                .filter(row -> !"CANCELLED".equals(row.status()))
                .filter(row -> row.appointmentTime().getHour() == hour.getHour())
                .map(DailyScheduleRow::patientName)
                .findFirst()
                .orElse(null);
    }

    private static String format(LocalTime time) {
        return "%02d:%02d".formatted(time.getHour(), time.getMinute());
    }

    public record DaySlot(String time, String patientName) {
        public boolean isTaken() {
            return patientName != null;
        }
    }
}
