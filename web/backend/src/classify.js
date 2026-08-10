import { TRACKER_DOMAINS } from "./trackerList.js";

const byDomain = new Map(TRACKER_DOMAINS.map((d) => [d.domain, d.category]));

export function classify(domain) {
  const parts = domain.toLowerCase().split(".");
  for (let i = 0; i < parts.length - 1; i++) {
    const suffix = parts.slice(i).join(".");
    const category = byDomain.get(suffix);
    if (category) return category;
  }
  return "unknown";
}
