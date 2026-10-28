import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DashboardServer {
    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        // Windows JDKs can fail to connect selector sockets in a redirected temp directory.
        // Keep these short-lived sockets in the existing build directory instead.
        if (System.getProperty("os.name").startsWith("Windows")
                && System.getProperty("jdk.net.unixdomain.tmpdir") == null) {
            Files.createDirectories(Path.of("bin"));
            System.setProperty("jdk.net.unixdomain.tmpdir", "bin");
        }
        Path web = Path.of("web").toAbsolutePath().normalize();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/api/simulate", DashboardServer::simulate);
        server.createContext("/", exchange -> {
            if (!exchange.getRequestMethod().equals("GET")) {
                send(exchange, 405, "text/plain", "Method not allowed");
                return;
            }
            String filename = switch (exchange.getRequestURI().getPath()) {
                case "/", "/index.html" -> "index.html";
                case "/style.css" -> "style.css";
                case "/app.js" -> "app.js";
                default -> null;
            };
            if (filename == null || !Files.isRegularFile(web.resolve(filename))) {
                send(exchange, 404, "text/plain", "Not found");
                return;
            }
            String mime = filename.endsWith("css") ? "text/css" : filename.endsWith("js")
                    ? "text/javascript" : "text/html";
            send(exchange, 200, mime, Files.readString(web.resolve(filename)));
        });
        server.start();
        System.out.println("Traffic simulator: http://127.0.0.1:" + port);
        System.out.println("Press Ctrl+C to stop.");
    }

    private static void simulate(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            send(exchange, 405, "text/plain", "Method not allowed");
            return;
        }
        try {
            Map<String, String> params = new HashMap<>();
            String query = exchange.getRequestURI().getRawQuery();
            if (query != null) for (String pair : query.split("&")) {
                String[] parts = pair.split("=", 2);
                if (parts.length == 2) params.put(parts[0], URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            }
            int duration = Integer.parseInt(params.getOrDefault("duration", "3600"));
            int green = Integer.parseInt(params.getOrDefault("green", "30"));
            double rate = Double.parseDouble(params.getOrDefault("rate", "0.45"));
            long seed = Long.parseLong(params.getOrDefault("seed", "42"));
            if (duration < 60 || duration > 7200 || green < 5 || green > 120
                    || !Double.isFinite(rate) || rate < 0 || rate > 1 || seed < 0 || seed > 2147483647L) {
                throw new IllegalArgumentException();
            }
            SimulationResult fixed = new Intersection(green).simulate(duration, false, rate, seed);
            SimulationResult adaptive = new Intersection(green).simulate(duration, true, rate, seed);
            send(exchange, 200, "application/json", "{\"duration\":" + duration + ",\"green\":" + green
                    + ",\"rate\":" + rate + ",\"seed\":" + seed + ",\"fixed\":" + fixed.toJson()
                    + ",\"adaptive\":" + adaptive.toJson() + "}");
        } catch (IllegalArgumentException exception) {
            send(exchange, 400, "application/json", "{\"error\":\"Use 60–7200 seconds, 5–120 seconds of green, an arrival probability from 0 to 1, and a seed from 0 to 2147483647.\"}");
        }
    }

    private static void send(HttpExchange exchange, int status, String mime, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", mime + "; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) { output.write(bytes); }
    }
}
