const relativeTimeFormat = new Intl.RelativeTimeFormat('default', {
    numeric: 'auto'
})

const timeFormat = new Intl.DateTimeFormat('default', {
    hour: "numeric", minute: "numeric"
})

const detailedTimeFormat = new Intl.DateTimeFormat('default', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric', hour: 'numeric', minute: "numeric"
})

const DIVISIONS = [
    {amount: 60, name: 'seconds'},
    {amount: 60, name: 'minutes'},
    {amount: 24, name: 'hours'},
    {amount: 7, name: 'days'},
    {amount: 4.34524, name: 'weeks'},
    {amount: 12, name: 'months'},
    {amount: Number.POSITIVE_INFINITY, name: 'years'}
]

function formatTimeSince(date) {
    let duration = (date - new Date()) / 1000

    for (let i = 0; i <= DIVISIONS.length; i++) {
        const division = DIVISIONS[i]
        if (Math.abs(duration) < division.amount) {
            return relativeTimeFormat.format(Math.round(duration), division.name);
        }
        duration /= division.amount
    }
}

const TYPE_FORMATTER = {
    "SHORT_TIME": new Intl.DateTimeFormat('default', {
        hour: "numeric", minute: "numeric"
    }),
    "LONG_TIME": new Intl.DateTimeFormat('default', {
        hour: "numeric", minute: "numeric", second: "numeric"
    }),
    "SHORT_DATE": new Intl.DateTimeFormat('default', {
        year: "numeric", month: "numeric", day: "numeric"
    }),
    "LONG_DATE": new Intl.DateTimeFormat('default', {
        day: "numeric", month: "long", year: "numeric"
    }),
    "SHORT_DATE_TIME": new Intl.DateTimeFormat('default', {
        day: "numeric", month: "long", year: "numeric",
        hour: "numeric", minute: "numeric"
    }),
    "LONG_DATE_TIME": new Intl.DateTimeFormat('default', {
        weekday: "long", day: "numeric", month: "long", year: "numeric",
        hour: "numeric", minute: "numeric"
    }),
    "RELATIVE": new Intl.RelativeTimeFormat('default', {
        numeric: 'auto'
    })
};

function formatType(type, date) {
    switch (type) {
        case "RELATIVE":
        case "relative":
            return formatTimeSince(date);
        case "basic":
            return timeFormat.format(date);
        case "detailed":
            return detailedTimeFormat.format(date);
        // case "RELATIVE":
        //     return formatTimeSince(date);
        default:
            return TYPE_FORMATTER[type].format(date);
    }
}

const matches = document.querySelectorAll("[timestamp]")
matches.forEach((element) => {
    // console.log(element.getAttribute("timestamp"))
    let time = Date.parse(element.getAttribute("timestamp"))
    let type = element.getAttribute("timestamp-type")
    console.log(type)
    element.textContent = formatType(type, time)
    let titleType = element.getAttribute("timestamp-title-type")
    if (titleType != null) {
        element.setAttribute("title", formatType(titleType, time))
    }
})