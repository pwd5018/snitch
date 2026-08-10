import { classify } from "./classify.js";

// sourceIp -> Map(domain -> { category, count, lastSeen })
const devices = new Map();

export function recordQuery({ domain, sourceIp }) {
  if (!devices.has(sourceIp)) devices.set(sourceIp, new Map());
  const connections = devices.get(sourceIp);
  const lastSeen = new Date().toISOString();
  const existing = connections.get(domain);
  if (existing) {
    existing.count += 1;
    existing.lastSeen = lastSeen;
  } else {
    connections.set(domain, { category: classify(domain), count: 1, lastSeen });
  }
}

function toDevice(sourceIp, connections) {
  return {
    id: sourceIp,
    name: sourceIp,
    kind: "unknown",
    connections: [...connections.entries()].map(([domain, c]) => ({ domain, ...c })),
  };
}

export function listDevices() {
  return [...devices.entries()].map(([sourceIp, connections]) => toDevice(sourceIp, connections));
}

export function getDevice(id) {
  const connections = devices.get(id);
  if (!connections) return null;
  return toDevice(id, connections);
}
