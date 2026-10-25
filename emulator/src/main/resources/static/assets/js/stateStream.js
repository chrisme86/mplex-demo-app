// server pushes an "update" event (SSE) whenever a device state changes
const source = new EventSource('/stream');
source.addEventListener('update', updateStatesTable);

// reload only the states table (th:fragment="statesTable")
async function updateStatesTable() {
    try {
        const response = await fetch('/states');
        const html = await response.text();
        const fragment = new DOMParser().parseFromString(html, 'text/html');
        document.getElementById('states').innerHTML = fragment.getElementById('states').innerHTML;
    } catch (err) {
        console.error('Error while updating the states table: ', err);
    }
}
