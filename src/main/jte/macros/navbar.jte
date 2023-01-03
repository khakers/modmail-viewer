@import com.github.khakers.modmailviewer.Config
@import com.github.khakers.modmailviewer.auth.SiteUser
@import io.javalin.http.Context

@param Context ctx
@param SiteUser user

<nav class="navbar navbar-expand-lg fixed-top navbar-dark bg-dark">
    <div class="container-fluid">
        <a class="navbar-brand" href="#">${Config.BRANDING}</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent"
                aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link ${ctx.path().equals("/") ? "active" : ""}" aria-current="page" href="/">Home</a>
                </li>
            </ul>
            <div class="d-flex me-2">
                @if(user.isRealUser())
                    <span class="navbar-text me-3">
                    Signed in as <span>${user.getUsername()}</span><span>#${user.getDiscriminator()}</span>
                </span>
                    <a class="btn btn-outline-danger" href="/logout">signout</a>
                @endif
            </div>

            <%--            <div>--%>
            <%--                <img src="${user.getAvatarUrl()}" class="navbar-brand rounded-circle img-fluid pl-2" width="40" style="size: inherit;">--%>
            <%--            </div>--%>
        </div>
    </div>
</nav>