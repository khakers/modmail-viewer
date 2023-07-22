@import com.github.khakers.modmailviewer.Config
@import com.github.khakers.modmailviewer.Page
@import com.github.khakers.modmailviewer.auth.AuthHandler
@import com.github.khakers.modmailviewer.auth.Role
@import com.github.khakers.modmailviewer.util.DiscordUtils
@import com.github.khakers.modmailviewer.util.RoleUtils


@import static com.github.khakers.modmailviewer.jte.JteContext.*


@param Page page

!{var user = page.getCurrentUser();}
!{var authRole = page.getCurrentUserRole();}

<svg xmlns="http://www.w3.org/2000/svg" style="display: none;">
    <symbol id="check2" viewBox="0 0 16 16">
        <path d="M13.854 3.646a.5.5 0 0 1 0 .708l-7 7a.5.5 0 0 1-.708 0l-3.5-3.5a.5.5 0 1 1 .708-.708L6.5 10.293l6.646-6.647a.5.5 0 0 1 .708 0z"></path>
    </symbol>
    <symbol id="circle-half" viewBox="0 0 16 16">
        <path d="M8 15A7 7 0 1 0 8 1v14zm0 1A8 8 0 1 1 8 0a8 8 0 0 1 0 16z"></path>
    </symbol>
    <symbol id="moon-stars-fill" viewBox="0 0 16 16">
        <path d="M6 .278a.768.768 0 0 1 .08.858 7.208 7.208 0 0 0-.878 3.46c0 4.021 3.278 7.277 7.318 7.277.527 0 1.04-.055 1.533-.16a.787.787 0 0 1 .81.316.733.733 0 0 1-.031.893A8.349 8.349 0 0 1 8.344 16C3.734 16 0 12.286 0 7.71 0 4.266 2.114 1.312 5.124.06A.752.752 0 0 1 6 .278z"></path>
        <path d="M10.794 3.148a.217.217 0 0 1 .412 0l.387 1.162c.173.518.579.924 1.097 1.097l1.162.387a.217.217 0 0 1 0 .412l-1.162.387a1.734 1.734 0 0 0-1.097 1.097l-.387 1.162a.217.217 0 0 1-.412 0l-.387-1.162A1.734 1.734 0 0 0 9.31 6.593l-1.162-.387a.217.217 0 0 1 0-.412l1.162-.387a1.734 1.734 0 0 0 1.097-1.097l.387-1.162zM13.863.099a.145.145 0 0 1 .274 0l.258.774c.115.346.386.617.732.732l.774.258a.145.145 0 0 1 0 .274l-.774.258a1.156 1.156 0 0 0-.732.732l-.258.774a.145.145 0 0 1-.274 0l-.258-.774a1.156 1.156 0 0 0-.732-.732l-.774-.258a.145.145 0 0 1 0-.274l.774-.258c.346-.115.617-.386.732-.732L13.863.1z"></path>
    </symbol>
    <symbol id="sun-fill" viewBox="0 0 16 16">
        <path d="M8 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM8 0a.5.5 0 0 1 .5.5v2a.5.5 0 0 1-1 0v-2A.5.5 0 0 1 8 0zm0 13a.5.5 0 0 1 .5.5v2a.5.5 0 0 1-1 0v-2A.5.5 0 0 1 8 13zm8-5a.5.5 0 0 1-.5.5h-2a.5.5 0 0 1 0-1h2a.5.5 0 0 1 .5.5zM3 8a.5.5 0 0 1-.5.5h-2a.5.5 0 0 1 0-1h2A.5.5 0 0 1 3 8zm10.657-5.657a.5.5 0 0 1 0 .707l-1.414 1.415a.5.5 0 1 1-.707-.708l1.414-1.414a.5.5 0 0 1 .707 0zm-9.193 9.193a.5.5 0 0 1 0 .707L3.05 13.657a.5.5 0 0 1-.707-.707l1.414-1.414a.5.5 0 0 1 .707 0zm9.193 2.121a.5.5 0 0 1-.707 0l-1.414-1.414a.5.5 0 0 1 .707-.707l1.414 1.414a.5.5 0 0 1 0 .707zM4.464 4.465a.5.5 0 0 1-.707 0L2.343 3.05a.5.5 0 1 1 .707-.707l1.414 1.414a.5.5 0 0 1 0 .708z"></path>
    </symbol>
</svg>
<nav class="navbar navbar-expand-lg fixed-top bg-body-tertiary border-bottom border-tertiary">
    <div class="container-fluid">
        <span class="navbar-brand h1" href="#">${Config.BRANDING}</span>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent"
                aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link" aria-current="page" href="/" up-follow up-target=".content" up-history='true'>Home</a>
                </li>
                @if(user.isPresent() && user.get().isRealUser() && RoleUtils.isAtLeastRole(authRole, Role.ADMINISTRATOR))
                    <li class="nav-item">
                        <a class="nav-link" aria-current="page" href="/admin" up-follow up-target=".content"
                           up-history='true'>${localize("NAV_PAGE_ADMIN")}</a>
                    </li>
                @endif
            </ul>
            <div class="row gx-3 ms-1 align-items-start me-2">
                <ul class="nav-item col-lg-1 col-12 dropdown my-0 ps-0" id="themeSelector">
                    <button class="btn btn-link nav-link py-2 px-0 px-lg-2 dropdown-toggle d-flex align-items-center"
                            id="bd-theme"
                            type="button"
                            aria-expanded="false"
                            data-bs-toggle="dropdown"
                            data-bs-display="static">
                        <svg class="bi my-1 theme-icon-active">
                            <use href="#circle-half"></use>
                        </svg>
                        <span class="d-lg-none ms-2">Toggle theme</span>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="bd-theme">
                        <li>
                            <button type="button" class="dropdown-item d-flex align-items-center"
                                    data-bs-theme-value="light">
                                <svg class="bi me-2 opacity-50 theme-icon">
                                    <use href="#sun-fill"></use>
                                </svg>
                                Light
                                <svg class="bi ms-auto d-none">
                                    <use href="#check2"></use>
                                </svg>
                            </button>
                        </li>
                        <li>
                            <button type="button" class="dropdown-item d-flex align-items-center"
                                    data-bs-theme-value="dark">
                                <svg class="bi me-2 opacity-50 theme-icon">
                                    <use href="#moon-stars-fill"></use>
                                </svg>
                                Dark
                                <svg class="bi ms-auto d-none">
                                    <use href="#check2"></use>
                                </svg>
                            </button>
                        </li>
                        <li>
                            <button type="button" class="dropdown-item d-flex align-items-center active"
                                    data-bs-theme-value="auto">
                                <svg class="bi me-2 opacity-50 theme-icon">
                                    <use href="#circle-half"></use>
                                </svg>
                                Auto
                                <svg class="bi ms-auto d-none">
                                    <use href="#check2"></use>
                                </svg>
                            </button>
                        </li>
                    </ul>
                </ul>
                <div class="col-12 col-lg d-flex mx-2 color text-primary-emphasis">
                    @if(user.isPresent() && user.get().isRealUser())
                        <span class="navbar-text me-3 text-emphasis">
                    Signed in as <span>${user.get().getUsername()}</span><span>${DiscordUtils.getDiscriminatorString(user.get())}</span>
                </span>
                        <form name="logoutForm" action="/logout" method="post">
                            <input type="submit" class="btn btn-outline-danger" value="Logout"/>
                        </form>
                    @endif
                </div>
            </div>
            <%--            <div>--%>
            <%--                <img src="${user.getAvatarUrl()}" class="navbar-brand rounded-circle img-fluid pl-2" width="40" style="size: inherit;">--%>
            <%--            </div>--%>
        </div>
    </div>
</nav>
<div id="alertPlaceholder" class="position-absolute"></div>
