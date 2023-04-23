const autocolors = window['chartjs-plugin-autocolors'];


Chart.register(autocolors);


let style = getComputedStyle(document.body)
Chart.defaults.color = style.getPropertyValue('--bs-secondary-text');
Chart.defaults.font.size = 14;
Chart.defaults.font.family = style.getPropertyValue('--bs-body-font-family')
Chart.defaults.font.weight = style.getPropertyValue("--bs-body-font-weight")
Chart.defaults.font.lineHeight = style.getPropertyValue("--bs-body-line-height")
Chart.defaults.borderColor = style.getPropertyValue("--bs-border-color")


const charts = [];

// Monitor theme change in order to update the chart default colors to be visible+
let observer = new MutationObserver((mutations, observer) => {
    mutations.forEach((mutation) => {
        if (mutation.type === "attributes" && mutation.attributeName === "data-bs-theme") {
            console.log(mutation.target.attributes["data-bs-theme"]);
            Chart.defaults.color = getComputedStyle(document.body).getPropertyValue('--bs-secondary-text');
            Chart.defaults.borderColor = getComputedStyle(document.body).getPropertyValue("--bs-border-color");
            console.log(Chart.defaults.color);
            console.log(Chart.defaults.borderColor);
            charts.forEach((chart) => {
                chart.borderColor = getComputedStyle(document.body).getPropertyValue("--bs-border-color");
                chart.update('none');
                console.log(chart)
            })
        }
    });
});

observer.observe(document.documentElement, {
    attributes: true
})


const ticketClosersCanvas = document.getElementById('ticketClosersChart');

let ticketClosersChartData = JSON.parse(ticketClosersCanvas.dataset.chartData);
console.log(ticketClosersChartData)

const ticketClosersChart = new Chart(ticketClosersCanvas.getContext('2d'), {
    type: 'doughnut',
    data: {
        //https://github.com/kurkle/chartjs-plugin-autocolors/issues/20
        // The autocolor plugin is dumb and if the data doesn't exist it won't ever get a color when it updates
        labels: ticketClosersChartData.labels,
        datasets: [{
            label: 'Tickets closed',
            data: ticketClosersChartData.data,
            hoverOffset: 8,
            offset: 4,
            borderWidth: 0,
            borderJoinStyle: 'bevel',
            // borderColor: 'rgba(0,0,0,0)'
        }]
    },

    options: {
        normalized: true,
        layout: {
            padding: 15
        },
        plugins: {
            autocolors: {
                enabled: true,
                mode: 'data'
            }
        }
    }
});
charts.push(ticketClosersChart);


// const ticketClosersChart = new Chart(ticketClosersCanvas, {
//     type: 'bar',
//     data: {
//         labels: [],
//         datasets: [{
//             label: 'Tickets Closed',
//             data: [],
//             hoverOffset: 8,
//             borderWidth: 1
//         }]
//     },
//
//     options: {
//         indexAxis: 'y',
//         normalized: true,
//         layout: {
//             padding: 15
//         },
//     }
// });

const ticketActionsPerDayCanvas = document.getElementById('ticketActionsPerDayChart');

let ticketActionsPerDayData = JSON.parse(ticketActionsPerDayCanvas.dataset.chartData);
console.log(ticketActionsPerDayData)


const ticketActionsPerDayChart = new Chart(ticketActionsPerDayCanvas.getContext('2d'), {
    type: 'line',
    response: true,
    data: {
        labels: ticketActionsPerDayData.labels,
        datasets: [
            {
                label: 'Tickets Closed',
                data: ticketActionsPerDayData.data[1],
                // hoverOffset: 8,
                // borderWidth: 1
                // backgroundColor: 'rgb(210,41,41)',
                // borderColor: 'rgb(210,41,41)',
                backgroundColor: style.getPropertyValue('--bs-red'),
                borderColor: style.getPropertyValue('--bs-red'),

                tension: 0.3,
                borderWidth: 4,
                borderJoinStyle: 'round'
            },
            {
                label: 'New Tickets',
                data: ticketActionsPerDayData.data[0],
                // backgroundColor: 'rgb(16,129,29)',
                // borderColor: 'rgb(16,129,29)',
                backgroundColor: style.getPropertyValue('--bs-green'),
                borderColor: style.getPropertyValue('--bs-green'),

                tension: 0.3,
                borderWidth: 4,
            }
        ]
    },
    scales: {
        y: {
            min: 0,
            ticks: {
                stepsize: 1
            }
        },
        default: {
            axis: 'y',
            min: 0
        }
    },

    options: {
        interaction: {
            intersect: false,
            mode: 'index',
        },
        plugins: {
            autocolors: {
                enabled: true
            }
        },
        normalized: true,
        layout: {
            padding: 15
        },
    }
});
charts.push(ticketActionsPerDayChart)


function refreshTicketClosersChart() {
    fetch('/api/ticketclosers')
        .then((response) => response.json())
        .then((data) => {
            console.log(data)
            // data.ord
            ticketClosersChart.data.datasets[0].data = [];
            ticketClosersChart.data.labels = [];

            for (const key in data) {
                ticketClosersChart.data.datasets[0].data.push(data[key]);
                ticketClosersChart.data.labels.push(key);
            }
            // ticketClosersChart.reset()
            // ticketClosersChart._autocolor = undefined
            ticketClosersChart.update();
            // console.log(labels)
            // console.log(values)
        });
}

document.getElementById("dailyTicketsPeriodSelect").addEventListener("change", ev => {
    refreshTicketActionsPerDayChart();
});

function refreshTicketActionsPerDayChart() {
    const selector = document.getElementById("dailyTicketsPeriodSelect");
    // selector.value

    fetch('/api/stats/ticketactivity?' + new URLSearchParams({
        period: selector.value, status: 'CLOSED'
    }))
        .then((response) => response.json())
        .then((data) => {
            console.log(data);
            ticketActionsPerDayChart.data.labels = data.labels;
            ticketActionsPerDayChart.data.datasets[0].data = data.data;
            ticketActionsPerDayChart.update();
        })
    fetch('/api/stats/ticketactivity?' + new URLSearchParams({
        period: selector.value, status: 'OPEN'
    }))
        .then((response) => response.json())
        .then((data) => {
            console.log(data);
            ticketActionsPerDayChart.data.datasets[1].data = data.data;
            ticketActionsPerDayChart.update();
        })
}