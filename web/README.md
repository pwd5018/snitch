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

## Running on a Synology NAS (recommended for actual home use)

A laptop or desktop works for development, but shouldn't be your real DNS server —
if it sleeps or reboots, it can take the network's DNS down with it (mitigated by
setting a secondary/fallback DNS server at the router, see below, but an always-on
box is still the right long-term home). A Synology NAS running Container Manager is
a good fit: it's already on all the time, and this is the same pattern many people
use to self-host Pi-hole.

**Note:** the Dockerfile/compose setup below has been reviewed carefully but not
build-tested end to end (the sandbox this was built in blocks Docker Hub pulls) — the
first real build happens on your NAS. If it fails, send me the error.

1. Copy the whole `web/` folder (containing `Dockerfile`, `docker-compose.yml`,
   `backend/`, `frontend/`) onto the NAS — e.g. via File Station or an SMB share —
   into a shared folder such as `docker/snitch`.
2. Open **Container Manager** → **Project** → **Create**.
3. Name it (e.g. `snitch`), set the path to the folder from step 1, and choose
   "Create docker-compose.yml" → point it at the existing `docker-compose.yml`
   already in that folder (or paste its contents in).
4. Build and run. Check the container's logs for the two startup lines
   (`snitch web backend listening...` and `snitch dns server listening...`) to
   confirm the DNS server actually bound port 53 — if the NAS itself already runs
   something on port 53 (e.g. Synology's own DNS Server package), you'll see the
   same `EADDRINUSE` error we hit on Windows, and will need to free that port
   or install this into a different network context first.
5. If `network_mode: host` isn't available as an option in your Container
   Manager UI, fall back to explicit port publishing instead — edit
   `docker-compose.yml` to remove `network_mode: host` and add:
   ```yaml
   ports:
     - "53:53/udp"
     - "8788:8788"
   ```
6. Once it's running, open `http://<nas-ip>:8788` for the dashboard (it now serves
   the built frontend directly — no separate frontend process needed in
   production), and follow the router DNS steps from earlier: set the router's
   primary DNS to the NAS's IP, secondary to `1.1.1.1`, so the network keeps
   working even if the NAS or container is ever down.

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
