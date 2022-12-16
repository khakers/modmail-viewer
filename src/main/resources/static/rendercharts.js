const autocolors = window['chartjs-plugin-autocolors'];


Chart.register(autocolors);

const ctx = document.getElementById('ticketClosers').getContext('2d');

fetch('/api/ticketclosers')
    .then((response) => response.json())
    .then((data) => {
        console.log(data)
        data.ord
        let values = [];
        for (const key in data) {
            values.push(data[key])
        }
        let labels = [];
        for (const key in data) {
            labels.push(key)
        }
        console.log(labels)
        console.log(values)
        const ticketClosersChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    label: '# of Votes',
                    data: values,
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
    })

const dailyTickets = document.getElementById('dailyTickets').getContext('2d');
fetch('/api/stats/dailytickets')
    .then((response) => response.json())
    .then((data) => {
        console.log(data)
        const dailyTicketsChart = new Chart(dailyTickets, {
            type: 'line',
            data: {
                labels: data.labels,
                datasets: [{
                    label: 'Tickets Closed',
                    data: data.data,
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
                    autocolors
                }
            }
        });
    })