import path from "node:path";
import { fileURLToPath } from "node:url";
import fs from "node:fs";
import express from "express";
import cors from "cors";
import { listDevices, getDevice, recordQuery } from "./store.js";
import { CATEGORIES } from "./categories.js";
import { startDnsServer } from "./dnsServer.js";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const publicDir = path.join(__dirname, "..", "public");

const app = express();
app.use(cors());

if (fs.existsSync(publicDir)) {
  app.use(express.static(publicDir));
}

function summarize(device) {
  const totals = { ad_tracker: 0, analytics: 0, functional: 0, unknown: 0 };
  for (const conn of device.connections) {
    totals[conn.category] += conn.count;
  }
  const total = Object.values(totals).reduce((a, b) => a + b, 0);
  const trackerShare = total === 0 ? 0 : totals.ad_tracker / total;
  const riskLevel = trackerShare > 0.3 ? "high" : trackerShare > 0.05 ? "medium" : "low";
  return { id: device.id, name: device.name, kind: device.kind, totals, riskLevel };
}

app.get("/api/health", (_req, res) => {
  res.json({ ok: true });
});

app.get("/api/devices", (_req, res) => {
  res.json(listDevices().map(summarize));
});

app.get("/api/devices/:id", (req, res) => {
  const device = getDevice(req.params.id);
  if (!device) {
    res.status(404).json({ error: "not found" });
    return;
  }
  const connections = device.connections.map((conn) => ({
    ...conn,
    category: CATEGORIES[conn.category],
  }));
  res.json({ ...summarize(device), connections });
});

const port = process.env.PORT || 8788;
app.listen(port, () => {
  console.log(`snitch web backend listening on http://localhost:${port}`);
});

const dnsPort = Number(process.env.DNS_PORT) || 53;
const upstreamDns = process.env.UPSTREAM_DNS || "1.1.1.1";
startDnsServer({ port: dnsPort, upstream: upstreamDns, onQuery: recordQuery });
console.log(
  `snitch dns server listening on udp/${dnsPort}, forwarding to ${upstreamDns}. ` +
    `Point a device's DNS at this machine's IP to start capturing its queries.`
);
