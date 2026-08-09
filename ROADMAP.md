# Snitch Roadmap

## v1 — Traffic flagging & Security/Privacy Audit (in progress)

Single device (your own phone). Status:

- **Done**: Security/Privacy Audit screen (PackageManager scan, requested-vs-granted
  permissions, risk flags) — Room-backed, fully working.
- **Done**: `VpnService` skeleton — tunnel lifecycle proven on a narrow, unused test
  subnet. No real traffic flows through it yet.
- **Remaining for v1**:
  - Real default-route interception + local TLS-decrypting proxy (self-signed CA you
    trust manually on-device). Apps with cert pinning fail through the proxy — logged
    as "pinned/blocked," not a crash.
  - `ConnectivityManager.getConnectionOwnerUid` wiring — map each connection back to
    its owning app.
  - **Flag apps sending excessive data** — per-app byte-count thresholds, once the
    proxy is actually logging connections.
  - **Identify what's being sent** — decrypted request/response inspection per app,
    logged to Room (`ConnectionLogEntity`, already reserved a slot in the schema from
    round 1 so this doesn't require a schema rework).
  - `NetworkStatsManager` per-app data usage summary (works without the VPN running).
  - APK manifest static-analysis flags: `debuggable`, `allowBackup`, cleartext traffic
    allowed, exported components without a permission guard, outdated `targetSdkVersion`.

## v2 — AI translation layer

Turns raw per-app traffic (once v1's proxy exists — nothing to classify before then)
into plain-language labels a non-technical parent can read at a glance: "ad tracker,"
"telemetry," "actually functional," etc.

- **Backend**: starts with a **cloud API** (Claude) — the simplest path to a working
  version. Built behind a classification interface from the start (not hardcoded to
  one provider) so a **self-hosted local model** (Ollama/llama.cpp on your home
  computer, phone calling over LAN or Tailscale) can be added later as a second
  backend rather than a rewrite — you want both eventually, cloud first.
- **Granularity** (open question, revisit when this round starts): classify each
  connection/domain individually, then roll those up into a per-app summary — probably
  both levels, not one or the other.
- **Privacy note for later**: since cloud classification means traffic metadata (and
  possibly decrypted content snippets) leaves the device per call, this is worth a
  second look once v3's kid-monitoring extension is in play — a stricter default (local
  model, or metadata-only cloud calls with no payload content) may make more sense for
  a child's traffic specifically, even if your own phone's classification stays
  cloud-based.

## v3 — Kid-mode reports

A parent-friendly summary instead of a raw network/audit log, built on top of v2's
classifications. Two phases:

1. **Same device, simplified view** (first step, no new architecture needed) — a
   plain-language report mode within Snitch on your own phone, reusing v1/v2's data,
   just a different presentation than the technical audit view.
2. **Extend to a child's device with a parent dashboard** (later — not scoped in
   detail yet, flagging it now so v1/v2 decisions don't box it out):
   - The Android VPN foreground-notification constraint above applies here directly —
     plan the UX around "unobtrusive, not invisible."
   - Report delivery needs a real design: viewed in-app on the kid's phone vs.
     synced/pushed to the parent's own device. The "local-only Room, no cloud sync"
     decision from round 1 was scoped for a single-device app — it'll need revisiting
     specifically for this cross-device delivery path, not assumed to just carry over.
   - Worth deciding early: what (if anything) is visible to the child about the
     monitoring, separate from the technical "the OS shows a VPN notification" point —
     that's a product/family decision, not just a technical one, and better made
     explicitly than left as a side effect of the notification requirement.

## Sequencing

v2 depends on v1's real traffic capture existing. v3 phase 1 depends on v2's
classifications existing. v3 phase 2 (child device + parent dashboard) is independent
scope that can start whenever — it's an architecture question (delivery + consent/UX),
not blocked on v1/v2 code.
