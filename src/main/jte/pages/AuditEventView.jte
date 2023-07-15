@import com.github.khakers.modmailviewer.page.audit.AuditEventPage
@import static com.github.khakers.modmailviewer.jte.JteContext.*


@param AuditEventPage page

!{var auditEvent = page.auditEvent;}


@template.layout.Page(page = page, content = @`
    <div class="container">
        <table class="table table-bordered">
            <thead>
                    <tr>
                        <th scope="col">${localize("AUDIT_PAGE_TABLE_HEADER_FIELD")}</th>
                        <th scope="col">${localize("AUDIT_PAGE_TABLE_HEADER_VALUE")}</th>
                    </tr>
            </thead>
            <tbody>
            <tr>
                <th scope="row">${localize("AUDIT_PARAM_EVENT_ID")}</th>
                <td>${auditEvent.id().toString()}</td>
            </tr>
            <tr>
                <th scope="row">${localize("AUDIT_PARAM_ACTION")}</th>
                <td>${auditEvent.action()}</td>
            </tr>
            <tr>
                <th scope="row">${localize("AUDIT_PARAM_DESCRIPTION")}</th>
                <td>${auditEvent.description()}</td>
            </tr>
            <tr>
                <th scope="row">${localize("AUDIT_PARAM_TIME")}</th>
                <td>${auditEvent.timestamp().toString()}</td>
            </tr>
            <tr>
                <th scope="row">${localize("AUDIT_PARAM_USER_ID")}</th>
                <td>${auditEvent.actor().userId()}</td>
            </tr>
            <tr>
                <th scope="row">${localize("AUDIT_PARAM_USER")}</th>
                <td>${auditEvent.actor().username()}</td>
            </tr>
            <tr>
                <th scope="row">${localize("AUDIT_PARAM_ACTOR_IP")}</th>
                <td>${auditEvent.actor().ip()}</td>
            </tr>
            <tr>
                <th scope="row">${localize("AUDIT_PARAM_ACTOR_COUNTRY")}</th>
                <td>${auditEvent.actor().country()}</td>
            </tr>
            <tr>
                <th scope="row">${localize("AUDIT_PARAM_ACTOR_USER_AGENT")}</th>
                <td>${auditEvent.actor().userAgent()}</td>
            </tr>
            <tr>
                <th scope="row">${localize("AUDIT_PARAM_ACTOR_ROLE")}</th>
                <td>${auditEvent.actor().role().toString()}</td>
            </tr>
            <tr>
                <th scope="row">${localize("AUDIT_PARAM_ACTOR_SOURCE")}</th>
                <td>${auditEvent.actor().source()}</td>
            </tr>
            </tbody>
        </table>
    </div>
`)
