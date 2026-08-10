# Snitch Web Dashboard

Self-hosted network dashboard: turns raw device/connection data into plain-language
labels ("ad tracker", "usage analytics") for people without a networking background.
This is the network-wide counterpart to the Android app in `app/` — buildable and
runnable entirely on a laptop, no phone or router required to get started.

## Status

Real DNS capture. The backend runs an actual DNS server (`backend/src/dnsServer.js`,
built on `dns2`) that forwards queries to a real upstream resolver (Cloudflare by
default) and logs each one — domain + source IP — before replying. Every logged
domain is classified against a small curated tracker/ad/analytics list
(`backend/src/trackerList.js`) by suffix match; anything not on the list is labeled
"Unknown" rather than assumed safe. Data is in-memory only and resets on restart —
there's no persistence yet.

## Run locally

```sh
cd web
npm install
npm run dev
```

- Backend API: http://localhost:8788 (`/api/health`, `/api/devices`, `/api/devices/:id`)
- DNS server: udp/53 by default — **binding port 53 needs root/admin** (`sudo npm run dev`
  on macOS/Linux, or run as Administrator on Windows). For local testing without
  elevated privileges, set `DNS_PORT` to something unprivileged, e.g.
  `DNS_PORT=5300 npm run dev -w backend`, and query it directly (there's no `dig`
  requirement — any DNS client pointed at `127.0.0.1:5300` works).
- Frontend: http://localhost:5174 — polls the backend every 5s, so the dashboard
  updates live as queries come in.

## Capturing real traffic

Point a device's DNS settings at this machine's LAN IP (with the server running on
port 53) and browse something on that device. Every query it makes will show up
under its IP on the dashboard within a few seconds. This captures only whatever
device you've explicitly pointed at it — nothing network-wide happens automatically.

## Layout

- `backend/`:
  - `src/dnsServer.js` — the DNS server: forwards to upstream, calls back into the
    store on every query.
  - `src/store.js` — in-memory device/connection store, keyed by source IP.
  - `src/classify.js` + `src/trackerList.js` — suffix-match domain classifier and
    the curated starter list it matches against (not a comprehensive blocklist).
  - `src/categories.js` — plain-language category definitions (ad_tracker /
    analytics / functional / unknown) with colors and explanations.
  - `src/server.js` — Express API wiring the above together.
- `frontend/`: Vite + vanilla JS. Renders a device list with risk badges; click a
  device to expand its connection breakdown. Polls for updates.

## Next steps (not yet built)

- Device naming — devices currently show as raw IPs; no way yet to label "Kids'
  Tablet" etc.
- Bigger/maintained tracker list instead of the ~60-entry curated starter set.
- Persistence across restarts.
- Reading from an existing Pi-hole/router query log as an alternative capture
  source, for whole-network coverage instead of one device at a time.
