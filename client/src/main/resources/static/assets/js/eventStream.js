// fragment of the current page, eg /public/status (home) or /service_client/status (with Reconnect button)
const statusUrl = document.currentScript.dataset.statusUrl;

// server pushes an "update" event (SSE) whenever a device state or the connection changes
const source = new EventSource('/public/stream');
source.addEventListener('update', updateStatus);

// reload only the status (th:fragment="statusTable")
async function updateStatus() {
    try {
        const response = await fetch(statusUrl);
        const html = await response.text();
        const fragment = new DOMParser().parseFromString(html, 'text/html');
        for (const id of ['connection', 'events']) {
            document.getElementById(id).innerHTML = fragment.getElementById(id).innerHTML;
        }
    } catch (err) {
        console.error('Error while updating the device status: ', err);
    }
}
