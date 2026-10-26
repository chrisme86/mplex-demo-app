// server pushes an "update" event (SSE) whenever a device state changes
const source = new EventSource('/service_client/stream');
source.addEventListener('update', updateEventsTable);

// reload only the events table (th:fragment="eventsTable")
async function updateEventsTable() {
    try {
        const response = await fetch('/service_client/events');
        const html = await response.text();
        const fragment = new DOMParser().parseFromString(html, 'text/html');
        for (const id of ['connection', 'events']) {
            document.getElementById(id).innerHTML = fragment.getElementById(id).innerHTML;
        }
    } catch (err) {
        console.error('Error while updating the events table: ', err);
    }
}
