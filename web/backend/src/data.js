const now = Date.now();
const minutesAgo = (m) => new Date(now - m * 60_000).toISOString();

export const DEVICES = [
  {
    id: "dev-1",
    name: "Living Room Smart TV",
    kind: "tv",
    connections: [
      { domain: "ads.doubleclick.net", category: "ad_tracker", count: 214, lastSeen: minutesAgo(4) },
      { domain: "graph.facebook.com", category: "ad_tracker", count: 58, lastSeen: minutesAgo(12) },
      { domain: "telemetry.samsungtv.com", category: "analytics", count: 39, lastSeen: minutesAgo(20) },
      { domain: "netflix.com", category: "functional", count: 412, lastSeen: minutesAgo(1) },
    ],
  },
  {
    id: "dev-2",
    name: "Mom's iPhone",
    kind: "phone",
    connections: [
      { domain: "api.icloud.com", category: "functional", count: 301, lastSeen: minutesAgo(2) },
      { domain: "app-measurement.com", category: "analytics", count: 84, lastSeen: minutesAgo(7) },
      { domain: "unknown-cdn-3f2a.net", category: "unknown", count: 6, lastSeen: minutesAgo(45) },
    ],
  },
  {
    id: "dev-3",
    name: "Kids' Tablet",
    kind: "tablet",
    connections: [
      { domain: "ads.unity3d.com", category: "ad_tracker", count: 512, lastSeen: minutesAgo(3) },
      { domain: "adjust.com", category: "ad_tracker", count: 190, lastSeen: minutesAgo(6) },
      { domain: "cdn.roblox.com", category: "functional", count: 620, lastSeen: minutesAgo(1) },
    ],
  },
  {
    id: "dev-4",
    name: "Unknown Device (192.168.1.47)",
    kind: "unknown",
    connections: [
      { domain: "185.220.101.7", category: "unknown", count: 11, lastSeen: minutesAgo(30) },
    ],
  },
];
