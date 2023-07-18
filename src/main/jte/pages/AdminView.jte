@import com.github.khakers.modmailviewer.page.admin.AdminPage
@import java.time.*
@import java.util.TimeZone
@import static com.github.khakers.modmailviewer.jte.JteContext.*


@param AdminPage page


@template.layout.Page(page = page, content = @`
    <div class="container p-5">
        <div class="border rounded p-2">
            <div>
                <h2>${localize("AUDIT_SEARCH_HEADER")}</h2>
                <button class="btn btn-primary">${localize("AUDIT_SEARCH_RESET")}</button>
            </div>
            <form class="row g-3 needs-validation justify-content-md-center" id="searchForm" action="/admin" method="get" novalidate up-submit up-target="#auditEventTable">
                <div class="col">
                    <label for="timezone">${localize("AUDIT_SEARCH_FORM_TIMEZONE")}</label>
                    !{var tzDefault = page.tz.getId().equals("Z") ? "UTC": page.tz.getId();}
                    <select name="tz" id="timezone" class="form-control">
                        @for(var entry : TimeZone.getAvailableIDs())
                            <option value="${entry}" selected="${entry.equals(tzDefault)}">${entry}</option>
                        @endfor
                    </select>
                </div>
                <div class="col">
                    <label>
                        ${localize("AUDIT_SEARCH_FORM_START_DATE")}
                        <div class="input-group">
                            <input type="date" class="form-control" name="startDate" id="startDate" value="${LocalDate.ofInstant(page.startTime, page.tz).toString()}">
                            <input type="time" class="form-control" name="startTime" id="startTime" value="${LocalTime.ofInstant(page.startTime, page.tz).toString()}">
                        </div>
                    </label>
                </div>
                <div class="col">
                    <label for="endDate">
                        ${localize("AUDIT_SEARCH_FORM_END_DATE")}
                        <div class="input-group">
                            <input type="date" class="form-control" name="endDate" id="endDate" value="${LocalDate.ofInstant(page.endTime, page.tz).toString()}">
                            <input type="time" class="form-control" name="endTime" id="endTime" value="${LocalTime.ofInstant(page.endTime, page.tz).withNano(0).toString()}">
                        </div>
                    </label>
                </div>

                <div class="col">
                    <label>${localize("AUDIT_SEARCH_FORM_USERS")}</label>
                    <input type="text" class="form-control" name="users" id="searchUsers" placeholder="comma seperated list of users" pattern="(([\d]{16,20})[,\s]*)*">

                </div>
                <div class="col">
                    <label>
                        ${localize("AUDIT_SEARCH_FORM_ACTIONS")}
                        <input type="text" class="form-control" name="actions" id="searchActions" placeholder="Show all actions" value="" pattern="([A-Z_]+[,\s]*)*">
                    </label>
                </div>
                <div class="col-12">
                    <button id="searchButton" class="btn btn-primary mx-2" type="submit">${localize("AUDIT_SEARCH_FORM_SUBMIT")}</button>

                </div>
            </form>
        </div>
        <div id="auditEventTable" class="overflow-auto">
            <table class="table table-hover">
                <thead>
                <tr>
                    <th scope="col">${localize("AUDIT_PARAM_TIME")}</th>
                    <th scope="col">${localize("AUDIT_PARAM_EVENT_ID")}</th>
                    <th scope="col">${localize("AUDIT_PARAM_USER")}</th>
                    <th scope="col">${localize("AUDIT_PARAM_ROLE")}</th>
                    <th scope="col">${localize("AUDIT_PARAM_SOURCE")}</th>
                    <th scope="col">${localize("AUDIT_PARAM_ACTION")}</th>
                    <th scope="col">${localize("AUDIT_PARAM_DESCRIPTION")}</th>
                </tr>
                </thead>
                <tbody>
                @for(var entry : page.auditEvents)
                    <tr data-id="${entry.id().toString()}">
                        <th scope="row">${entry.timestamp().toString()}</th>
                        <td>
                            <a href="/audit/${entry.id().toString()}" up-layer="new">
                                ${entry.id().toString()}
                            </a>
                        </td>
                        <td>${entry.actor().username()}</td>
                        <td>${entry.actor().role().toString()}</td>
                        <td>${entry.actor().source()}</td>
                        <td>${entry.action()}</td>
                        <td>${entry.description()}</td>
                    </tr>
                @endfor
                </tbody>
            </table>
            @if(page.auditEvents.isEmpty())
                <div class="alert alert-warning text-center" role="alert">
                    ${localize("AUDIT_SEARCH_EMPTY")}
                </div>
            @endif
        </div>
    </div>
`)
