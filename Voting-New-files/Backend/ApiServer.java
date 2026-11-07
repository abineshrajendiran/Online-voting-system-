package com.voting.connection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiServer {

    static VoterDAO voterDAO = new VoterDAO();
    static VoteDAO voteDAO = new VoteDAO();
    static PartyDAO partyDAO = new PartyDAO();

    public static void main(String[] args) throws Exception {
    	int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "9090"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // GET /api/parties -> list of parties
        server.createContext("/api/parties", ex -> {
            StringBuilder sb = new StringBuilder("[");
            List<Party> parties = partyDAO.getAllParties();
            for (int i = 0; i < parties.size(); i++) {
                Party p = parties.get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(p.getPartyId())
                  .append(",\"name\":\"").append(esc(p.getPartyName()))
                  .append("\",\"code\":\"").append(esc(p.getPartyCode())).append("\"}");
            }
            sb.append("]");
            send(ex, 200, "application/json", sb.toString().getBytes(StandardCharsets.UTF_8));
        });

        // POST /api/vote -> body: voterId, name, password, partyId
        server.createContext("/api/vote", ex -> {
            String json;
            try {
                Map<String, String> p = parse(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                json = handleVote(
                        p.getOrDefault("voterId", "").trim().toUpperCase(),
                        p.getOrDefault("name", "").trim(),
                        p.getOrDefault("password", "").trim(),
                        p.getOrDefault("partyId", "").trim());
            } catch (Exception e) {
                json = result(false, "Error: " + e.getMessage());
            }
            send(ex, 200, "application/json", json.getBytes(StandardCharsets.UTF_8));
        });

        // Serves the web page files
        server.createContext("/", ex -> {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/")) path = "/vote.html";
            if (!(path.equals("/vote.html") || path.equals("/vote.js") || path.equals("/style.css"))) {
                send(ex, 404, "text/plain", "Not found".getBytes());
                return;
            }
            String type = path.endsWith(".css") ? "text/css"
                    : path.endsWith(".js") ? "application/javascript" : "text/html; charset=utf-8";
            send(ex, 200, type, Files.readAllBytes(Paths.get(path.substring(1))));
        });

        server.start();
        System.out.println("Server running on port " + port);
    }

    // Same steps as Main.processVoter()
    static String handleVote(String voterId, String name, String password, String partyIdText) {

        if (!VoterDAO.isValidFormat(voterId)) {
            return result(false, "Invalid Voter ID. Use 3 capital letters + 7 digits.");
        }

        if (!voterDAO.exists(voterId)) {
            if (name.isEmpty() || password.isEmpty()) {
                return result(false, "New voter. Enter your name and create a password.");
            }
            if (!voterDAO.register(voterId, name, password)) {
                return result(false, "Registration failed.");
            }
        } else {
            Voter voter = voterDAO.login(voterId, password);
            if (voter == null) {
                return result(false, "Wrong password.");
            }
        }

        if (voteDAO.hasVoted(voterId)) {
            return result(false, "You are already voted. One voter can vote only one time.");
        }

        int partyId;
        try {
            partyId = Integer.parseInt(partyIdText);
        } catch (NumberFormatException e) {
            return result(false, "Invalid party number. Vote not saved.");
        }

        boolean validParty = false;
        for (Party p : partyDAO.getAllParties()) {
            if (p.getPartyId() == partyId) {
                validParty = true;
                break;
            }
        }
        if (!validParty) {
            return result(false, "Party number not in the list. Vote not saved.");
        }

        if (voteDAO.submitVote(voterId, partyId)) {
            return result(true, "Your vote has been recorded. Thank you!");
        }
        return result(false, "Vote could not be saved.");
    }

    static String result(boolean ok, String message) {
        return "{\"ok\":" + ok + ",\"message\":\"" + esc(message) + "\"}";
    }

    static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }

    static Map<String, String> parse(String body) throws Exception {
        Map<String, String> m = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                m.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
            }
        }
        return m;
    }

    static void send(HttpExchange ex, int code, String type, byte[] data) throws IOException {
        ex.getResponseHeaders().add("Content-Type", type);
        ex.sendResponseHeaders(code, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }
}