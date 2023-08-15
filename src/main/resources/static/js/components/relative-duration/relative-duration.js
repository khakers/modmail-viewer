class RelativeDuration extends HTMLElement {
    constructor() {
        // Always call super first in constructor
        super();

        // Element functionality written in here
        let duration = this.getAttribute("duration");

        this.textContent = humanizeDuration(duration, {round: true, largest: 2});

    }
}

customElements.define('relative-duration', RelativeDuration)