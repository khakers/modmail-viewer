class Spoiler extends HTMLElement {
    constructor() {
        // Always call super first in constructor
        super();

        // Element functionality written in here

        this.setAttribute("class", "hidden");
        this.setAttribute("role", "button");

        const content = document.createElement("span");
        content.textContent = this.textContent;
        this.textContent = null;

        this.appendChild(content);

        this.addEventListener("click", (event) => {
            let target = event.target;
            target.classList.remove("hidden");
            target.setAttribute("role", "presentation");
        });
    }
}

customElements.define('text-spoiler', Spoiler)