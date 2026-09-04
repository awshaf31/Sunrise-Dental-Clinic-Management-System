<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout title="Help" activeNav="help">
    <jsp:attribute name="body">

        <div class="page-head">
            <div>
                <div class="eyebrow">For new staff</div>
                <h1>How to use this system</h1>
            </div>
        </div>

        <div class="grid grid-2">

            <section class="card">
                <div class="card-head"><h2>Book an appointment</h2></div>
                <div class="card-body">
                    <ol style="margin:0; padding-left:20px; line-height:1.9">
                        <li>Open <strong>Book appointment</strong> from the menu.</li>
                        <li>Type the patient's name, address and contact number. If they have visited
                            before, the system reuses their existing record automatically.</li>
                        <li>Choose the dentist. Their day appears on the left: amber rows are already
                            taken, plain ruled rows are free.</li>
                        <li>Pick a free time. Booking a taken slot is refused, so two patients can never
                            be given the same chair.</li>
                        <li>Press <strong>Book appointment</strong>. Write the appointment number on the
                            patient's card — it is how you find them again.</li>
                    </ol>
                </div>
            </section>

            <section class="card">
                <div class="card-head"><h2>Find a patient</h2></div>
                <div class="card-body">
                    <ol style="margin:0; padding-left:20px; line-height:1.9">
                        <li>Open <strong>Find appointment</strong>.</li>
                        <li>Type the appointment number from the patient's card, for example
                            <span class="mono">APT-20260902-001</span>.</li>
                        <li>The full record opens, with the patient's details and their visit.</li>
                    </ol>
                    <p style="margin-bottom:0; color:var(--ink-3); font-size:.86rem">
                        Do not have the number? Open <strong>Today</strong> and find them in the day's list.
                    </p>
                </div>
            </section>

            <section class="card">
                <div class="card-head"><h2>Print a bill</h2></div>
                <div class="card-body">
                    <ol style="margin:0; padding-left:20px; line-height:1.9">
                        <li>Open the appointment.</li>
                        <li>Press <strong>Prepare bill</strong>. The total is the consultation fee plus
                            the treatment charge.</li>
                        <li>Press <strong>Print bill</strong> and hand the receipt to the patient.</li>
                    </ol>
                    <p style="margin-bottom:0; color:var(--ink-3); font-size:.86rem">
                        Pressing it twice is safe. The same bill is shown again — the patient is never
                        charged a second time.
                    </p>
                </div>
            </section>

            <section class="card">
                <div class="card-head"><h2>Good to know</h2></div>
                <div class="card-body">
                    <dl class="detail" style="grid-template-columns:1fr">
                        <dt>Cancelling</dt>
                        <dd style="padding-top:0">Cancelling releases the slot for another patient. The
                            record stays, so the visit history is never lost.</dd>
                        <dt>Opening hours</dt>
                        <dd style="padding-top:0">Appointments can be booked from 09:00 to 17:00, on
                            today's date or later.</dd>
                        <dt>Signing out</dt>
                        <dd style="padding-top:0">Always sign out when you leave the desk.</dd>
                        <dt>Reports</dt>
                        <dd style="padding-top:0; border-bottom:none">The manager uses
                            <strong>Reports</strong> to see the day's takings broken down by treatment.</dd>
                    </dl>
                </div>
            </section>

        </div>

    </jsp:attribute>
</t:layout>
