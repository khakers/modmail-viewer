const searchbox = document.getElementById("SearchBox");


const input = document.getElementById("SearchBoxInput");
const searchButton = document.getElementById("SearchButton");
const searchClearButton = document.getElementById("ClearSearchButton");

searchButton.addEventListener("click", search);
input.addEventListener('keydown', event => {
    if (event.key === "Enter") {
        search();
    }
})
searchClearButton.addEventListener("click", (event) => {
    update('search', '');
    input.value = '';
})

const searchParams = new URLSearchParams(window.location.search);
input.value = searchParams.get('search');

function search() {
    update('search', input.value);
}

