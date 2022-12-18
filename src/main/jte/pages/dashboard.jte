@import com.github.khakers.modmailviewer.auth.SiteUser
@import com.github.khakers.modmailviewer.data.ModMailLogEntry
@import java.util.List
@import java.util.Map
@param SiteUser user

@param int totalTickets
@param int openTickets
@param int closedTickets

@param Map<String, Integer> moderatorCloses

@param List<ModMailLogEntry> recentEntries

<!doctype html>
<html lang="en" xmlns="http://www.w3.org/1999/html">
<head>
    <title>Modmail dashboard</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="/webjars/bootstrap/5.2.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="/webjars/bootstrap-icons/1.10.2/font/bootstrap-icons.css">
    <link rel="stylesheet" href="/styles.css">
    <%--    <script src="/webjars/chartjs-plugin-autocolors/0.0.3/dist/chartjs-plugin-autocolors.min.js"></script>--%>
</head>
<body>


<div class="page-container">
    @template.macros.navbar(user = user)
    <div class="content">
        <div class="container pt-5">
            <div class="row row-cols-1 row-cols-md-3 g-3">
                <div class="col">
                    <%-- Total Tickets --%>
                    <div class="card">
                        <h5 class="card-header">Total Tickets</h5>
                        <div class="card-body">
                            <h6 class="card-title fs-1">${totalTickets}</h6>
                        </div>
                    </div>
                </div>
                <div class="col">
                    <%-- Open Tickets --%>
                    <div class="card ">
                        <h5 class="card-header "><i class="bi bi-folder2-open"></i> Open Tickets</h5>
                        <div class="card-body <%--text-bg-warning--%>">
                            <h6 class="card-title fs-1">${openTickets}</h6>
                        </div>
                    </div>
                </div>
                <div class="col">
                    <%-- Closed Tickets --%>
                    <div class="card <%--text-bg-success--%>">
                        <h5 class="card-header"><i class="bi bi-check-lg"></i> Closed Tickets</h5>
                        <div class="card-body">
                            <h6 class="card-title fs-1">${closedTickets}</h6>
                        </div>
                    </div>
                </div>
            </div>
            <%--    row-cols-1 row-cols-md-2 row-cols-xl-3--%>
            <div class="row pt-5 g-3">
                <div class="col-12 col-lg-5 col-xl-4">
                    <div class="card">
                        <div class="card-body">
                            <h5 class="card-title">Ticket Closers</h5>
<%--                            <p class="card-text">This is a wider card with supporting text below as a natural lead-in to--%>
<%--                                additional content. This content is a little longer.</p>--%>
<%--                            <p class="card-text"><small class="text-muted">Last updated 3 mins ago</small></p>--%>
                        </div>
                        <canvas id="ticketClosersChart" width="400" height="400"></canvas>
<%--                        <code class="card-footer">${moderatorCloses.toString()}</code>--%>
                    </div>
                </div>
                <div class="col">
                    <div class="card">
                        <div class="card-body">
                            <h5 class="card-title">Tickets closed per day</h5>
                            <form id="dailyTicketsForm">
                            <select class="form-select" name="dailyTicketsPeriodSelect" id="dailyTicketsPeriodSelect" aria-label="Default select example">
                                <option selected value="30">30 days</option>
                                <option value="60">60 days</option>
                                <option value="90">90 days</option>
                            </select>
                            </form>
                            <%--                            <p class="card-text">This is a wider card with supporting text below as a natural lead-in to--%>
                            <%--                                additional content. This content is a little longer.</p>--%>
                        </div>
                        <canvas id="ticketActionsPerDayChart" width="400" height="400" style="max-height: 480px;"></canvas>
<%--                        <code class="card-footer"></code>--%>
                    </div>
<%--                    <div class="card">--%>
<%--                        <div class="card-header">--%>
<%--                            Recent Messages--%>
<%--                        </div>--%>
<%--                        <div class="card-body">--%>
<%--                            @if(recentEntries == null || recentEntries.isEmpty())--%>
<%--                                <p class="card-text">No recent entries could be retrieved</p>--%>
<%--                            @else--%>
<%--                                <ul class="list-group list-group-flush">--%>
<%--                                    @for(var entry : recentEntries)--%>
<%--                                        <li class="list-group-item">--%>
<%--                                            <a class="fw-bold fs-5" href="/logs/${entry.getKey()}">${entry.getKey()}</a>--%>
<%--                                            @if(entry.isOpen())--%>
<%--                                                <span class="badge rounded-pill text-bg-success fs-6 fw-normal">Open</span>--%>
<%--                                            @else--%>
<%--                                                <span class="badge rounded-pill text-bg-danger fs-6 fw-semibold"><i--%>
<%--                                                            class="bi bi-x-circle"></i> Closed</span>--%>
<%--                                            @endif--%>
<%--                                            <p>${entry.getMessages().get(entry.getMessages().size()-1).getContent()}</p>--%>
<%--                                            <span class="text-muted"--%>
<%--                                                  timestamp="${entry.getMessages().get(entry.getMessages().size()-1).getCreationTime().toString()}"--%>
<%--                                                  timestamp-type="detailed"></span>--%>
<%--                                        </li>--%>
<%--                                    @endfor--%>
<%--                                </ul>--%>
<%--                            @endif--%>
<%--                        </div>--%>
<%--                    </div>--%>
                </div>
            </div>
        </div>
    </div>
    @template.macros.footer()
</div>


<script src="/timeformat.js"></script>
<script src="/webjars/chart.js/3.9.1/dist/chart.js"></script>
<script src="/webjars/chartjs-plugin-autocolors/0.1.0/dist/chartjs-plugin-autocolors.min.js"></script>
<script src="/rendercharts.js"></script>
<script src="/webjars/bootstrap/5.2.3/dist/js/bootstrap.bundle.min.js"></script>

<%--<script src="https://cdn.jsdelivr.net/npm/chart.js@3.9.1/dist/chart.min.js" integrity="sha256-+8RZJua0aEWg+QVVKg4LEzEEm/8RFez5Tb4JBNiV5xA=" crossorigin="anonymous"></script>--%>
<script>
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))
</script>
</body>
</html>