const app = document.getElementById("app");
const expanded = new Set();
const detailCache = new Map();

function relativeTime(iso) {
  const diffMs = Date.now() - new Date(iso).getTime();
  const mins = Math.round(diffMs / 60_000);
  if (mins < 1) return "just now";
  if (mins < 60) return `${mins}m ago`;
  return `${Math.round(mins / 60)}h ago`;
}

function renderConnections(connections) {
  return connections
    .map(
      (c) => `
      <div class="connection-row">
        <span class="connection-domain">${c.domain}</span>
        <span class="category-pill category-${c.category.color}">${c.category.label}</span>
        <span class="connection-count">${c.count} · ${relativeTime(c.lastSeen)}</span>
        <span class="connection-explanation">${c.category.explanation}</span>
      </div>`
    )
    .join("");
}

function renderDevice(device) {
  const isExpanded = expanded.has(device.id);
  const detail = detailCache.get(device.id);
  const trackerTotal = device.totals.ad_tracker;
  return `
    <div class="device-card" data-id="${device.id}">
      <div class="device-card-header">
        <div>
          <div class="device-name">${device.name}</div>
          <div class="device-sub">${trackerTotal} ad-tracker connections today</div>
        </div>
        <span class="risk-badge risk-${device.riskLevel}">${device.riskLevel} risk</span>
      </div>
      ${
        isExpanded
          ? `<div class="connections">${
              detail ? renderConnections(detail.connections) : "<div class=\"loading\">Loading…</div>"
            }</div>`
          : ""
      }
    </div>`;
}

async function loadDevices() {
  app.innerHTML = `
    <header>
      <h1>Snitch</h1>
      <p>What's actually happening on your network, in plain language.</p>
    </header>
    <div class="loading">Loading devices…</div>`;

  let devices;
  try {
    const res = await fetch("/api/devices");
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    devices = await res.json();
  } catch (err) {
    app.innerHTML += `<div class="error">Couldn't load devices: ${err.message}. Is the backend running?</div>`;
    return;
  }

  render(devices);
}

function render(devices) {
  app.innerHTML = `
    <header>
      <h1>Snitch</h1>
      <p>What's actually happening on your network, in plain language.</p>
    </header>
    <div class="device-list">
      ${devices.map(renderDevice).join("")}
    </div>`;

  app.querySelectorAll(".device-card").forEach((card) => {
    card.addEventListener("click", async () => {
      const id = card.dataset.id;
      if (expanded.has(id)) {
        expanded.delete(id);
        render(devices);
        return;
      }
      expanded.add(id);
      render(devices);
      if (!detailCache.has(id)) {
        const res = await fetch(`/api/devices/${id}`);
        const detail = await res.json();
        detailCache.set(id, detail);
        render(devices);
      }
    });
  });
}

loadDevices();
