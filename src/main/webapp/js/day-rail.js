/*
 * Keeps the day rail on the booking form in step with the dentist and date the
 * receptionist has chosen, so a clash is visible while they are still filling
 * the form in rather than after they submit it.
 *
 * This is a convenience, not a control. The booking is refused by the service
 * layer and by a database constraint regardless of what this shows, so a
 * failure here can never let a double booking through.
 */
(function () {
    'use strict';

    var dentist = document.getElementById('dentistId');
    var date = document.getElementById('appointmentDate');
    var time = document.getElementById('appointmentTime');
    var track = document.getElementById('rail-track');
    var count = document.getElementById('rail-count');

    if (!dentist || !date || !track) {
        return;
    }

    var HOURS = ['09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00'];

    function render(bookedTimes) {
        var booked = {};
        bookedTimes.forEach(function (t) {
            booked[t.slice(0, 5)] = true;
        });

        track.replaceChildren();

        HOURS.forEach(function (hour) {
            var taken = booked[hour] === true;
            var selected = time && time.value === hour;

            var row = document.createElement('div');
            row.className = 'slot ' + (taken ? 'is-taken' : 'is-free') + (selected ? ' is-selected' : '');

            var label = document.createElement('span');
            label.className = 'slot-time';
            label.textContent = hour;

            var body = document.createElement('span');
            body.className = 'slot-body';
            body.textContent = taken ? 'Booked' : '';

            row.append(label, body);
            track.append(row);
        });

        var free = HOURS.length - Object.keys(booked).length;
        count.textContent = free + (free === 1 ? ' slot free' : ' slots free');
    }

    function refresh() {
        if (!dentist.value || !date.value) {
            count.textContent = 'Pick a dentist';
            return;
        }

        var url = '/appointments/booked-times?dentistId=' + encodeURIComponent(dentist.value) +
            '&date=' + encodeURIComponent(date.value);

        fetch(url, { headers: { 'Accept': 'application/json' } })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('unavailable');
                }
                return response.json();
            })
            .then(render)
            .catch(function () {
                // Say what the reader can rely on instead of failing silently.
                count.textContent = 'Availability unavailable';
            });
    }

    dentist.addEventListener('change', refresh);
    date.addEventListener('change', refresh);
    if (time) {
        time.addEventListener('change', refresh);
    }

    refresh();
})();
