@import com.github.khakers.modmailviewer.Main
@import com.github.khakers.modmailviewer.data.internal.TicketStatus
@import com.github.khakers.modmailviewer.page.dashboard.DashboardPage
@import static com.github.khakers.modmailviewer.jte.JteContext.*


@param DashboardPage page


@template.layout.Page(page = page, content = @`
    <div class="container pt-5">
        <div id="dashboardControlPanel" class="mb-3 p-2 ps-3 border rounded">
            <form class="row justify-content-between" id="dailyTicketsForm" up-autosubmit up-target="#dashboardContent">
                <div class="col">
                    <div class="row">
                        <div class="col-auto">
                            <label for="chartPeriodSelect" class="col-form-label">Time Period:</label>
                        </div>
                        <div class="col">
                            <select class="form-select" name="period" id="chartPeriodSelect"
                                    aria-label="Time Period Select">
                                <option selected="${page.period==15}" value="15">15 days</option>
                                <option selected="${page.period==30}" value="30">30 days</option>
                                <option selected="${page.period==60}" value="60">60 days</option>
                                <option selected="${page.period==90}" value="90">90 days</option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="col">
                    <button type="submit" id="dashboardReloadButton" class="btn btn-outline-primary float-end">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                             class="bi bi-arrow-clockwise" viewBox="0 0 16 16">
                            <path fill-rule="evenodd"
                                  d="M8 3a5 5 0 1 0 4.546 2.914.5.5 0 0 1 .908-.417A6 6 0 1 1 8 2v1z"/>
                            <path d="M8 4.466V.534a.25.25 0 0 1 .41-.192l2.36 1.966c.12.1.12.284 0 .384L8.41 4.658A.25.25 0 0 1 8 4.466z"/>
                        </svg>
                    </button>
                </div>
            </form>
        </div>
        <div class="row row-cols-1 row-cols-md-3 g-3 justify-content-between" id="dashboardContent">
            <div class="col">
                <%-- Total Tickets --%>
                <div class="card">
                    <h6 class="card-header"><i class="bi bi-inbox"></i> Total Tickets</h6>
                    <div class="card-body">
                        <h6 class="card-title fs-1">${Main.db.getTotalTickets(TicketStatus.ALL)}</h6>
                    </div>
                </div>
            </div>
            <div class="col">
                <%-- Open Tickets --%>
                <div class="card ">
                    <h6 class="card-header "><i class="bi bi-envelope-paper"></i> Open Tickets</h6>
                    <div class="card-body <%--text-bg-warning--%>">
                        <h6 class="card-title fs-1">${Main.db.getTotalTickets(TicketStatus.OPEN)}</h6>
                    </div>
                </div>
            </div>
            <div class="col">
                <%-- Closed Tickets --%>
                <div class="card">
                    <h6 class="card-header"><i class="bi bi-envelope-check"></i> Closed Tickets</h6>
                    <div class="card-body">
                        <h6 class="card-title fs-1">${Main.db.getTotalTickets(TicketStatus.CLOSED)}</h6>
                    </div>
                </div>
            </div>
            <div class="col">
                <%-- Unanswered Tickets --%>
                <div class="card">
                    <h6 class="card-header"><i class="bi bi-envelope-exclamation"></i> Unanswered Tickets</h6>
                    <div class="card-body">
                        <h6 class="card-title fs-1">${Main.metricsAccessor.getUnansweredTicketCount()}</h6>
                    </div>
                </div>
            </div>
            <div class="col">
                <%-- Average Reply Time --%>
                <div class="card">
                    <h6 class="card-header"><i class="bi bi-stopwatch"></i> Average Reply Time</h6>
                    <div class="card-body">
                        <h6 class="card-title fs-1">
                            <relative-duration
                                    duration="${Main.metricsAccessor.getAverageFirstReplyTime(page.period).toMillis()}"></relative-duration>
                        </h6>
                    </div>
                </div>
            </div>
            <div class="col">
                <%-- Average Resolution Time --%>
                <div class="card">
                    <h6 class="card-header"><i class="bi bi-stopwatch"></i> Average Resolution Time</h6>
                    <div class="card-body">
                        <h6 class="card-title fs-1">
                            <relative-duration
                                    duration="${Main.metricsAccessor.getAverageResolutionTime(page.period).toMillis()}"></relative-duration>
                        </h6>
                    </div>
                </div>
            </div>
        </div>
        <div class="row pt-3 g-3">
            <div class="col-12 col-lg-5 col-xl-4">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">Ticket Closers</h5>
                        <h6 class="card-subtitle mb-2 text-body-secondary">All time</h6>
                    </div>
                    <canvas class="chart" id="ticketClosersChart" width="400" height="400"
                            data-chart-data="${Main.metricsAccessor.getTicketClosersJson()}"></canvas>
                </div>
            </div>
            <div class="col">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">Tickets closed per day</h5>
                    </div>
                    <canvas class="chart" id="ticketActionsPerDayChart" width="400"
                            data-chart-data="${Main.metricsAccessor.getTicketsPerDayJson(page.period)}"></canvas>
                </div>
            </div>
        </div>
    </div>
`)