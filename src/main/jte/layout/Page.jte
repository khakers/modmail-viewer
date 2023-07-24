@import static com.github.khakers.modmailviewer.jte.JteContext.*
@import com.github.khakers.modmailviewer.Main
@import com.github.khakers.modmailviewer.ModmailViewer
@import com.github.khakers.modmailviewer.Page
@import com.github.khakers.modmailviewer.auth.Role
@import com.github.khakers.modmailviewer.util.RoleUtils
@import gg.jte.Content
@import com.github.khakers.modmailviewer.UpdateChecker


@param Page page
@param Content content


<!doctype html>
<html lang="en" xmlns="http://www.w3.org/1999/html">
<head>
    <title>${page.getTitle()}</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    @template.macros.HeaderImports()
</head>

<body>
    @template.macros.navbar(page = page)
        <main class="content mb-5" up-main data-nsfw="${page.isNSFW()}" up-main>
            ${content}
        </main>
    <%--  Update Nag  --%>
    @if(RoleUtils.isAtLeastRole(page.getCurrentUserRole(), Role.ADMINISTRATOR) && Main.updateChecker.isUpdateAvailable())
        <div class="toast-container position-fixed bottom-0 end-0 p-3">
        <div class="toast show" role="alert" aria-live="assertive" aria-atomic="true" id="updateToast">
            <div class="toast-header">
                <i class="bi bi-arrow-repeat fs-5 me-1"></i>
                <strong class="me-auto">${localize("UPDATE_TOAST_HEADER")}</strong>
                !{var updateTime = Main.updateChecker.getUpdateFoundTime().get();}
                <small><time datetime="${updateTime.toString()}" data-timestamp="${updateTime.toString()}" data-timestamp-type="relative"></time></small>
                <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
            <div class="toast-body">
                ${localize("UPDATE_TOAST_BODY_TEXT")}
                <hr>
                ${localize("UPDATE_TOAST_BODY_VERSIONS", ModmailViewer.VERSION, Main.updateChecker.getLatestVersion().get().asVersionString())}
                <br>
                <a href="https://github.com/khakers/modmail-viewer/releases/${Main.updateChecker.getLatestVersion().get().asVersionString()}" class="">View update notes</a>
            </div>
        </div>
        </div>
    @endif
    @template.macros.footer(page = page)
</body>
</html>