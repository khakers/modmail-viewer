// Fill out bootstrap tooltips
up.compiler('[data-bs-toggle="tooltip"]', function (element) {
    new bootstrap.Tooltip(element);
});

up.compiler('.spoilerText', function (element) {
    element.addEventListener("click", (event) => {
        let target = event.target;
        target.classList.remove("hidden");
        target.setAttribute("role", "presentation");
    });
});

up.compiler('pre code', function (element) {
    hljs.highlightElement(element);
});

up.compiler("#nsfwModal", function (element) {
    const bsModal = new bootstrap.Modal(element);
    element.addEventListener('hide.bs.modal', event => {
        document.getElementById("nsfw-backdrop").hidden = true;
    });
    bsModal.show();
});

up.on('up:fragment:inserted', (event) => {
    twemoji.parse(document.body, {
        // The default Twemoji CDN ~will~ died at the end of the year. This tells it to use jsdelivr for emoji images instead
        base: "https://cdn.jsdelivr.net/gh/jdecked/twemoji@v14.0.2/assets/"
    });
});

up.on('up:request:loaded', (event) => {
    if (!event.response.ok) {
        if (event.response.status === 403) {
            event.preventDefault();
            // This looks very odd but it seems to work perfectly
            up.network.loadPage({ url: "" });
        }
    }
});

up.on('up:request:offline', (event) => {
    //handle telling the user we're offline
});


function isDiscordPage(url) {
    return !!url.match(/^https:\/\/discord\.com\/oauth2\/authorize\S*/gm);
}
