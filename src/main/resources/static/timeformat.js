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
    { amount: 60, name: 'seconds' },
    { amount: 60, name: 'minutes' },
    { amount: 24, name: 'hours' },
    { amount: 7, name: 'days' },
    { amount: 4.34524, name: 'weeks' },
    { amount: 12, name: 'months' },
    { amount: Number.POSITIVE_INFINITY, name: 'years' }
]

function formatTimeSince(date) {
    let duration = (date - new Date()) / 1000

    for (let i = 0; i <= DIVISIONS.length; i++) {
        const division = DIVISIONS[i]
        if (Math.abs(duration) < division.amount) {
            return relativeTimeFormat.format(Math.round(duration), division.name)
        }
        duration /= division.amount
    }
}

function formatType(type, date) {
    if (type === "relative" || type == null) {
        return  formatTimeSince(date)
    } else if (type === "basic") {
        return  timeFormat.format(date)
    } else if (type === "detailed") {
        return  detailedTimeFormat.format(date)
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