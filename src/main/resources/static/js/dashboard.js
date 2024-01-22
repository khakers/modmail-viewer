const autocolors = window['chartjs-plugin-autocolors'];


Chart.register(autocolors);



let style = getComputedStyle(document.body);

let font = {
    family: style.getPropertyValue("--bs-body-font-family"),
    size: 14,
    style: 'normal',
    lineHeight: style.getPropertyValue("--bs-body-line-height"),
    weight: 500
}

Chart.defaults.color = style.getPropertyValue('--bs-secondary-color');
Chart.defaults.font = font;
Chart.defaults.borderColor = style.getPropertyValue("--bs-border-color");
Chart.defaults.plugins.legend.labels.color = style.getPropertyValue('--bs-secondary-color');
Chart.defaults.plugins.legend.labels.font = font;

const charts = [];

// Monitor theme change in order to update the chart default colors to be visible
let observer = new MutationObserver((mutations, observer) => {
    mutations.forEach((mutation) => {
        if (mutation.type === "attributes" && mutation.attributeName === "data-bs-theme") {
            let secondaryColor = style.getPropertyValue('--bs-secondary-color');
            let borderColor = style.getPropertyValue("--bs-border-color");
            // console.log(mutation.target.attributes["data-bs-theme"]);
            Chart.defaults.color = secondaryColor;
            Chart.defaults.borderColor = borderColor;
            Chart.defaults.plugins.legend.labels.color = secondaryColor;
            // console.log(Chart.defaults.color);
            // console.log(Chart.defaults.borderColor);
            charts.forEach((chart) => {
                chart.borderColor = borderColor;
                chart.options.plugins.legend.labels.color = secondaryColor;

                if (chart.options.scales.x !== undefined) {
                    chart.options.scales.x.ticks.color = secondaryColor;
                }
                if (chart.options.scales.y !== undefined) {
                    chart.options.scales.y.ticks.color = secondaryColor;
                }
                // console.log(chart);
                chart.update('none');
            })
        }
    });
});

observer.observe(document.documentElement, {
    attributes: true
});

up.compiler('#ticketClosersChart', (element) => {
    const ticketClosersCanvas = element;

    let ticketClosersChartData = JSON.parse(element.dataset.chartData);
    // console.log(ticketClosersChartData)

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
    // console.log(ticketClosersChart.options);
    charts.push(ticketClosersChart);
    // console.log(charts)

    up.destructor(element, () => {
        //remove the chart from the charts array by comparing the canvas to element
        charts.splice(charts.findIndex((chart) => chart.canvas === element), 1);

        ticketClosersChart.destroy();
    });
});

up.compiler('#ticketActionsPerDayChart', (element) => {
    const ticketActionsPerDayCanvas = element;

    let ticketActionsPerDayData = JSON.parse(ticketActionsPerDayCanvas.dataset.chartData);
    // console.log(ticketActionsPerDayData)


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
                    borderJoinStyle: 'round',
                    pointStyle: false
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
                    pointStyle: false
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

    up.destructor(element, () => {
        //remove the chart from the charts array by comparing the canvas to element
        charts.splice(charts.findIndex((chart) => chart.canvas === element), 1);

        ticketActionsPerDayChart.destroy();
    });
});