# Snitch Web Dashboard

Self-hosted network dashboard: turns raw device/connection data into plain-language
labels ("ad tracker", "needed to work") for people without a networking background.
This is the network-wide counterpart to the Android app in `app/` — buildable and
runnable entirely on a laptop, no phone or router required to get started.

## Status

Scaffold only. The backend currently serves **mock data** (`backend/src/data.js`) —
four sample devices with pre-classified connections — so the dashboard UI can be
built and iterated on before real DNS capture exists. Swapping in live data later
means replacing `data.js`'s source, not the API shape or the frontend.

## Run locally

```sh
cd web
npm install
npm run dev
```

- Backend: http://localhost:8788 (`/api/health`, `/api/devices`, `/api/devices/:id`)
- Frontend: http://localhost:5174

## Layout

- `backend/`: Express API. `src/data.js` holds mock devices/connections;
  `src/categories.js` holds the plain-language category definitions
  (ad_tracker / analytics / functional / unknown) with colors and explanations.
- `frontend/`: Vite + vanilla JS. Renders a device list with risk badges; click a
  device to expand its connection breakdown.

## Next steps (not yet built)

- Replace mock data with a real DNS query source (local DNS server or reading an
  existing Pi-hole/router log), keyed by source IP/MAC for device attribution.
- Domain classification against a maintained tracker/ad list instead of hardcoded
  sample categories.
- Persistence (currently everything is in-memory and resets on restart).
