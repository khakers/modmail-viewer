"use strict";

import '/webjars/glightbox/3.2.0/dist/js/glightbox.min.js';

const CSS_URL = new URL("/webjars/plyr/3.7.8/dist/plyr.css",window.location.origin);
const JS_URL = new URL("/webjars/plyr/3.7.8/dist/plyr.min.js",window.location.origin);
const ICON_URL = new URL("/webjars/plyr/3.7.8/dist/plyr.svg",window.location.origin);

const lightbox = GLightbox({
    plyr: {
        css: CSS_URL.toString(),
        js: JS_URL.toString(),
        config: {
            iconUrl: ICON_URL.toString(),
        }
    }
});
