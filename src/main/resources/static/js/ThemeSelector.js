/*!
 * Modified from color mode toggler for Bootstrap's docs (https://getbootstrap.com/)
 * Copyright 2011-2022 The Bootstrap Authors, Khakers
 * Licensed under the Creative Commons Attribution 3.0 Unported License.
 */

'use strict';

const storedTheme = localStorage.getItem('theme');

const getPreferredTheme = () => {
    if (storedTheme) {
        return storedTheme;
    }

    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
};

const setTheme = function (theme) {
    if (theme === 'auto' && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        document.documentElement.setAttribute('data-bs-theme', 'dark');
        document.querySelector("link[title=dark]").removeAttribute("disabled");
        document.querySelector("link[title=light]").setAttribute("disabled", "disabled");
    } else if (theme === 'auto' && window.matchMedia('(prefers-color-scheme: light)').matches) {
        document.documentElement.setAttribute('data-bs-theme', 'light');
        document.querySelector("link[title=light]").removeAttribute("disabled");
        document.querySelector("link[title=dark]").setAttribute("disabled", "disabled");
    } else {
        document.documentElement.setAttribute('data-bs-theme', theme);
        switch (theme) {
            case "dark": {
                document.querySelector("link[title=dark]").removeAttribute("disabled");
                document.querySelector("link[title=light]").setAttribute("disabled", "disabled");
                break;
            }
            case "light": {
                document.querySelector("link[title=light]").removeAttribute("disabled");
                document.querySelector("link[title=dark]").setAttribute("disabled", "disabled");
                break;
            }
        }

    }
};

setTheme(getPreferredTheme());

const showActiveTheme = theme => {
    const activeThemeIcon = document.querySelector('.theme-icon-active use')
    const btnToActive = document.querySelector(`[data-bs-theme-value="${theme}"]`)
    const svgOfActiveBtn = btnToActive.querySelector('svg use').getAttribute('href')

    document.querySelectorAll('[data-bs-theme-value]').forEach(element => {
        element.classList.remove('active');
    });

    btnToActive.classList.add('active');
    activeThemeIcon.setAttribute('href', svgOfActiveBtn);
};

window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
    if (storedTheme !== 'light' || storedTheme !== 'dark') {
        setTheme(getPreferredTheme());
    }
});

up.compiler('[data-bs-theme-value]', function (element) {
    element.addEventListener('click', () => {
        const theme = element.getAttribute('data-bs-theme-value');
        localStorage.setItem('theme', theme);
        setTheme(theme);
        showActiveTheme(theme);
    });
});

