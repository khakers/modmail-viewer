@import static com.github.khakers.modmailviewer.jte.JteContext.*

@param com.github.khakers.modmailviewer.Page page
<footer>
    <div class="footer mt-auto py-3 ps-3 bg-body-tertiary border-top border-3">
        <div class="row align-items-center me-0 justify-content-xl-between">
            <div class="col-sm-12 col-xl-5 align-items-center text-center text-xl-start navbar-text pb-0 pt-0 text-sm-nowrap" up-hungry>
                ${localize("FOOTER_VERSION_TEXT", com.github.khakers.modmailviewer.ModmailViewer.VERSION)}
                |
                ${localize("FOOTER_RENDERED_IN_TEXT", page.getRenderTime())}
            </div>
            <div class="col-sm-12 col-xl-5 d-inline-block justify-content-center justify-content-xl-end gap-3 text-center text-xl-end text-xl-start">
                <div class="row justify-content-center justify-content-xl-end gx-2 g-3">
                    <span class="col-sm-4 col-xl-5 navbar-text pb-0 pt-0 mb-0 text-nowrap">
                        ${localize("FOOTER_LICENSE_TEXT")}</span>
<%--                    <span class="col-sm-auto">--%>
<%--                        <a class="icon-link icon-link-hover" href="/licenseReport.txt">--%>
<%--                            <svg xmlns="http://www.w3.org/2000/svg" class="bi" aria-hidden="true" viewBox="0 0 16 16">--%>
<%--                                <path d="M5 10.5a.5.5 0 0 1 .5-.5h2a.5.5 0 0 1 0 1h-2a.5.5 0 0 1-.5-.5zm0-2a.5.5 0 0 1 .5-.5h5a.5.5 0 0 1 0 1h-5a.5.5 0 0 1-.5-.5zm0-2a.5.5 0 0 1 .5-.5h5a.5.5 0 0 1 0 1h-5a.5.5 0 0 1-.5-.5zm0-2a.5.5 0 0 1 .5-.5h5a.5.5 0 0 1 0 1h-5a.5.5 0 0 1-.5-.5z"/>--%>
<%--                                <path d="M3 0h10a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2v-1h1v1a1 1 0 0 0 1 1h10a1 1 0 0 0 1-1V2a1 1 0 0 0-1-1H3a1 1 0 0 0-1 1v1H1V2a2 2 0 0 1 2-2z"/>--%>
<%--                                <path d="M1 5v-.5a.5.5 0 0 1 1 0V5h.5a.5.5 0 0 1 0 1h-2a.5.5 0 0 1 0-1H1zm0 3v-.5a.5.5 0 0 1 1 0V8h.5a.5.5 0 0 1 0 1h-2a.5.5 0 0 1 0-1H1zm0 3v-.5a.5.5 0 0 1 1 0v.5h.5a.5.5 0 0 1 0 1h-2a.5.5 0 0 1 0-1H1z"/>--%>
<%--                            </svg>--%>
<%--                            ${localize("FOOTER_LICENSES_LINK")}--%>
<%--                        </a>--%>
<%--                    </span>--%>
                    <span class="col-sm-auto">
                        <a class="icon-link icon-link-hover" href="https://github.com/khakers/modmail-viewer">
                            <svg xmlns="http://www.w3.org/2000/svg" class="bi bi-github" viewBox="0 0 16 16" aria-hidden="true">
                                <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.012 8.012 0 0 0 16 8c0-4.42-3.58-8-8-8z"/>
                            </svg> Github
                        </a>
                    </span>
                    <span class="col-sm-auto">
                        <a class="icon-link icon-link-hover" href="https://github.com/khakers/modmail-viewer/issues/new?assignees=&labels=bug&template=bug_report.yml&title=%5BBug%5D%3A+&version=${org.owasp.encoder.Encode.forUriComponent(com.github.khakers.modmailviewer.ModmailViewer.PRECISE_GIT_VERSION_ID)}">
                            <svg xmlns="http://www.w3.org/2000/svg" class="bi bi-bug" viewBox="0 0 16 16">
                                <path d="M4.355.522a.5.5 0 0 1 .623.333l.291.956A4.979 4.979 0 0 1 8 1c1.007 0 1.946.298 2.731.811l.29-.956a.5.5 0 1 1 .957.29l-.41 1.352A4.985 4.985 0 0 1 13 6h.5a.5.5 0 0 0 .5-.5V5a.5.5 0 0 1 1 0v.5A1.5 1.5 0 0 1 13.5 7H13v1h1.5a.5.5 0 0 1 0 1H13v1h.5a1.5 1.5 0 0 1 1.5 1.5v.5a.5.5 0 1 1-1 0v-.5a.5.5 0 0 0-.5-.5H13a5 5 0 0 1-10 0h-.5a.5.5 0 0 0-.5.5v.5a.5.5 0 1 1-1 0v-.5A1.5 1.5 0 0 1 2.5 10H3V9H1.5a.5.5 0 0 1 0-1H3V7h-.5A1.5 1.5 0 0 1 1 5.5V5a.5.5 0 0 1 1 0v.5a.5.5 0 0 0 .5.5H3c0-1.364.547-2.601 1.432-3.503l-.41-1.352a.5.5 0 0 1 .333-.623zM4 7v4a4 4 0 0 0 3.5 3.97V7H4zm4.5 0v7.97A4 4 0 0 0 12 11V7H8.5zM12 6a3.989 3.989 0 0 0-1.334-2.982A3.983 3.983 0 0 0 8 2a3.983 3.983 0 0 0-2.667 1.018A3.989 3.989 0 0 0 4 6h8z"/>
                            </svg>
                            ${localize("FOOTER_BUG_REPORT_TEXT")}
                        </a>
                    </span>
                </div>
            </div>
        </div>
    </div>
</footer>
