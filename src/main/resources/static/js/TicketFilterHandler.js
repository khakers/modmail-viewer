
up.compiler('#SearchBoxInput', function (element) {
    element.addEventListener('keydown', event => {
        if (event.key === "Enter") {
            search();
        }
    })
})

up.compiler("#ClearSearchButton", function (element) {
    element.addEventListener('click', () => {
        const url = window.location.href;
        console.log(url);
        const params = up.Params.fromURL(url);
        params.delete('search');

        up.navigate({params: params, url: window.location.pathname});
    })
})

up.compiler('#SearchButton', function (element) {
    element.addEventListener("click", (event) => {
        search();
    })
})


function search() {
    const search = document.getElementById("SearchBoxInput").value;

    const url = window.location.href;
    console.log(url);
    const params = up.Params.fromURL(url);
    params.set('search', search);

    up.navigate({params: params, url: window.location.pathname});
}

