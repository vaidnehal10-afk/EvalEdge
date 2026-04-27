import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

/**
 * DashboardServer.java
 * Lightweight HTTP server using built-in Java JDK — NO Tomcat needed.
 * Serves StudentDashboard.html and handles /login and /pbl-detail API calls.
 *
 * Usage: called from Main.java when user picks "Dashboard" option.
 */
public class DashboardServer {

    private static final int PORT = 8080;
    private static final DashboardService svc = new DashboardService();

    public static void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Serve the HTML file
        server.createContext("/",            DashboardServer::serveHTML);

        // API endpoints
        server.createContext("/login",       DashboardServer::handleLogin);
        server.createContext("/pbl-detail",  DashboardServer::handlePblDetail);

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   EvalEdge Dashboard is running!             ║");
        System.out.println("║   Open: http://localhost:8080/               ║");
        System.out.println("║   Press Ctrl+C to stop the server.           ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    // ── Serve StudentDashboard.html ───────────────────────────────────────────
    private static void serveHTML(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equals("GET")) { ex.sendResponseHeaders(405, -1); return; }

        // Look for the HTML file next to the running .class files
        File htmlFile = new File("StudentDashboard.html");
        if (!htmlFile.exists()) {
            // Try one level up (if running from a bin/ or out/ folder)
            htmlFile = new File("../StudentDashboard.html");
        }

        if (!htmlFile.exists()) {
            String msg = "StudentDashboard.html not found. Place it in: " + new File(".").getAbsolutePath();
            ex.sendResponseHeaders(404, msg.length());
            ex.getResponseBody().write(msg.getBytes());
            ex.getResponseBody().close();
            return;
        }

        byte[] bytes = Files.readAllBytes(htmlFile.toPath());
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    // ── POST /login ───────────────────────────────────────────────────────────
    private static void handleLogin(HttpExchange ex) throws IOException {
        setCORS(ex);

        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(200, -1); return;
        }

        try {
            // Read POST body: "studentId=1&password=pass123"
           Map<String, String> params;

if (ex.getRequestMethod().equalsIgnoreCase("GET")) {
    String query = ex.getRequestURI().getQuery();
    params = parseQuery(query != null ? query : "");
} else {
    String body = new String(ex.getRequestBody().readAllBytes());
    params = parseQuery(body);
}

int studentId = Integer.parseInt(params.getOrDefault("studentId", "0"));
String password = params.getOrDefault("password", "");
            Map<String, String> profile = svc.loginStudent(studentId, password);

            if (profile == null) {
                sendJSON(ex, 401, "{\"error\":\"Invalid credentials\"}");
                return;
            }

            List<Map<String, String>> pbls = svc.getStudentPBLs(studentId);
            String json = buildLoginJSON(profile, pbls);
            sendJSON(ex, 200, json);

        } catch (Exception e) {
            sendJSON(ex, 500, "{\"error\":\"" + escJson(e.getMessage()) + "\"}");
        }
    }

    // ── GET /pbl-detail?studentId=1&pblId=1 ──────────────────────────────────
    private static void handlePblDetail(HttpExchange ex) throws IOException {
        setCORS(ex);

        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(200, -1); return;
        }

        try {
            String query = ex.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query != null ? query : "");

            int studentId = Integer.parseInt(params.getOrDefault("studentId", "0"));
            int pblId     = Integer.parseInt(params.getOrDefault("pblId",     "0"));

            List<Map<String, String>>              phases = svc.getPhaseDetails(studentId, pblId);
            Map<Integer, List<Map<String, String>>> meets  = svc.getMeetAttendance(studentId, pblId);

            String json = buildDetailJSON(phases, meets);
            sendJSON(ex, 200, json);

        } catch (Exception e) {
            sendJSON(ex, 500, "{\"error\":\"" + escJson(e.getMessage()) + "\"}");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void setCORS(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJSON(HttpExchange ex, int code, String json) throws IOException {
        byte[] bytes = json.getBytes("UTF-8");
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    /** Parses key=value&key2=value2 query strings / POST bodies. */
    private static Map<String, String> parseQuery(String query) throws Exception {
        Map<String, String> map = new LinkedHashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], "UTF-8"),
                        URLDecoder.decode(kv[1], "UTF-8"));
            }
        }
        return map;
    }

    // ── JSON builders ─────────────────────────────────────────────────────────

    private static String buildLoginJSON(Map<String, String> profile,
                                         List<Map<String, String>> pbls) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"student\":").append(mapToJson(profile));
        sb.append(",\"pbls\":[");
        for (int i = 0; i < pbls.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(mapToJson(pbls.get(i)));
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String buildDetailJSON(List<Map<String, String>> phases,
                                          Map<Integer, List<Map<String, String>>> meets) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"phases\":[");
        for (int i = 0; i < phases.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(mapToJson(phases.get(i)));
        }
        sb.append("],\"meets\":{");
        boolean first = true;
        for (Map.Entry<Integer, List<Map<String, String>>> e : meets.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":[");
            List<Map<String, String>> list = e.getValue();
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(mapToJson(list.get(i)));
            }
            sb.append("]");
            first = false;
        }
        sb.append("}}");
        return sb.toString();
    }

    private static String mapToJson(Map<String, String> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escJson(e.getKey())).append("\":");
            sb.append("\"").append(escJson(e.getValue())).append("\"");
            first = false;
        }
        return sb.append("}").toString();
    }

    private static String escJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
