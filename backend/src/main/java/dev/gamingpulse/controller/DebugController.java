// backend/src/main/java/dev/gamingpulse/controller/DebugController.java
package dev.gamingpulse.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Value("${gamingpulse.n8n-url:http://gamingpulse-n8n:5678}")
    private String n8nUrl;

    @GetMapping("/n8n")
    public Map<String, Object> debugN8n() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("configured_url", n8nUrl);
        result.put("java_net_preferIPv4Stack", System.getProperty("java.net.preferIPv4Stack"));
        result.put("JAVA_TOOL_OPTIONS", System.getenv("JAVA_TOOL_OPTIONS"));

        // 1. DNS Auflösung
        try {
            String host = URI.create(n8nUrl).getHost();
            result.put("dns_host", host);
            InetAddress[] addresses = InetAddress.getAllByName(host);
            List<String> ips = new ArrayList<>();
            for (InetAddress a : addresses) ips.add(a.toString());
            result.put("dns_resolved", ips);
        } catch (Exception e) {
            result.put("dns_error", e.getMessage());
        }

        // 2. TCP Connect Test
        try {
            String host = URI.create(n8nUrl).getHost();
            Instant tcpStart = Instant.now();
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, 5678), 3000);
            socket.close();
            result.put("tcp_connect_ms", Duration.between(tcpStart, Instant.now()).toMillis());
            result.put("tcp_status", "OK");
        } catch (Exception e) {
            result.put("tcp_status", "FAILED");
            result.put("tcp_error", e.getMessage());
        }

        // 3. HTTP Request mit vollem Stack-Trace
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(n8nUrl + "/healthz"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            Instant httpStart = Instant.now();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            result.put("http_status", response.statusCode());
            result.put("http_body", response.body().substring(0, Math.min(200, response.body().length())));
            result.put("http_ms", Duration.between(httpStart, Instant.now()).toMillis());
        } catch (Exception e) {
            result.put("http_error_type", e.getClass().getName());
            result.put("http_error_message", e.getMessage());
            StringJoiner sj = new StringJoiner("\n");
            for (StackTraceElement el : e.getStackTrace()) sj.add(el.toString());
            result.put("http_stacktrace", sj.toString());
            if (e.getCause() != null) {
                result.put("http_cause", e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
            }
        }

        return result;
    }
}