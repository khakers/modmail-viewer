const autocolors = window['chartjs-plugin-autocolors'];


Chart.register(autocolors);

const ticketClosersCanvas = document.getElementById('ticketClosersChart').getContext('2d');

const ticketClosersChart = new Chart(ticketClosersCanvas, {
    type: 'doughnut',
    data: {
        //https://github.com/kurkle/chartjs-plugin-autocolors/issues/20
        // The autocolor plugin is dumb and if the data doesn't exist it won't ever get a color when it updates
        labels: ["tmp","tmp2","tmp3","tmp4","tmp5","tmp6","tmp7","tmp8","tmp9","tmp10","tmp11","tmp12"],
        datasets: [{
            label: 'Tickets closed',
            data: [0,1,2,3,4,5,6,7,8,9,10,11],
            hoverOffset: 8,
            borderWidth: 1
        }]
    },

    options: {
        normalized: true,
        layout: {
            padding: 15
        },
        plugins: {
            autocolors: {
                mode: 'data'
            }
        }
    }
});

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

const ticketActionsPerDayCanvas = document.getElementById('ticketActionsPerDayChart').getContext('2d');

const ticketActionsPerDayChart = new Chart(ticketActionsPerDayCanvas, {
    type: 'line',
    response: true,
    data: {
        // labels: data.labels,
        datasets: [{
            label: 'Tickets Closed',
            // data: data.data,
            hoverOffset: 8,
            borderWidth: 1
        }]
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
        normalized: true,
        layout: {
            padding: 15
        },
    }
});

refreshTicketClosersChart();
refreshTicketActionsPerDayChart();


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

    fetch('/api/stats/dailytickets?' + new URLSearchParams({
        period: selector.value
    }))
        .then((response) => response.json())
        .then((data) => {
            console.log(data);
            ticketActionsPerDayChart.data.labels = data.labels;
            ticketActionsPerDayChart.data.datasets[0].data = data.data;
            ticketActionsPerDayChart.update();
        })
}