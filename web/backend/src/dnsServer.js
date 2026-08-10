import dns2 from "dns2";

const { Packet, UDPClient } = dns2;

export function startDnsServer({ port, address = "0.0.0.0", upstream, onQuery }) {
  const resolveUpstream = UDPClient({ dns: upstream });

  const server = dns2.createServer({
    udp: true,
    handle: async (request, send, rinfo) => {
      const response = Packet.createResponseFromRequest(request);
      const [question] = request.questions;

      if (question) {
        const name = question.name.replace(/\.$/, "");
        onQuery({ domain: name, sourceIp: rinfo.address });
        try {
          const upstreamResponse = await resolveUpstream(name);
          response.answers = upstreamResponse.answers;
        } catch (err) {
          response.header.rcode = Packet.RCODE.SERVFAIL;
        }
      }

      send(response);
    },
  });

  server.on("requestError", (err) => {
    console.error("dns request error:", err.message);
  });

  server.on("error", (err, type) => {
    if (err.code === "EACCES") {
      console.error(
        `dns server: permission denied binding ${type} port ${port}. ` +
          `Ports below 1024 need elevated privileges — run with sudo, or set DNS_PORT to something like 5300 for local testing.`
      );
    } else {
      console.error(`dns server ${type} error:`, err.message);
    }
  });

  server.listen({ udp: { port, address } });

  return server;
}
