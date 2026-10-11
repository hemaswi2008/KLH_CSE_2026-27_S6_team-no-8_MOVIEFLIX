import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class Server {
    // In-memory database collections
    private static List<Movie> movies = new ArrayList<>();
    private static Map<String, Movie> movieTitleMap = new HashMap<>();

    public static class Movie {
        int id;
        String title;
        String genre;
        String language;
        int year;
        String director;
        String cast;
        double rating;
        String description;
        String poster;

        public Movie(int id, String title, String genre, String language, int year, 
                     String director, String cast, double rating, String description, String poster) {
            this.id = id;
            this.title = title;
            this.genre = genre;
            this.language = language;
            this.year = year;
            this.director = director;
            this.cast = cast;
            this.rating = rating;
            this.description = description;
            this.poster = poster;
        }

        public String toJson() {
            return String.format(
                "{\"id\":%d,\"title\":\"%s\",\"genre\":\"%s\",\"language\":\"%s\",\"year\":%d,\"director\":\"%s\",\"cast\":\"%s\",\"rating\":%.1f,\"description\":\"%s\",\"poster\":\"%s\"}",
                id, escapeJson(title), escapeJson(genre), escapeJson(language), year, 
                escapeJson(director), escapeJson(cast), rating, escapeJson(description), escapeJson(poster)
            );
        }

        private String escapeJson(String val) {
            if (val == null) return "";
            return val.replace("\"", "\\\"").replace("\n", " ");
        }
    }

    public static void main(String[] args) throws IOException {
        loadDataset();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // API endpoints
        server.createContext("/api/movies", new MoviesHandler());
        server.createContext("/api/search", new SearchHandler());
        server.createContext("/api/similar", new SimilarHandler());
        
        // Static frontend files handler
        server.createContext("/", new StaticHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("MovieFlix Server started on http://localhost:8080");
    }

    private static void loadDataset() {
        movies.clear();
        movieTitleMap.clear();
        
        // Pointing directly to the file in your root folder
        String csvPath = "MovieFlix_Final_Dataset.csv";
        File file = new File(csvPath);

        if (!file.exists()) {
            System.out.println("Error: 'MovieFlix_Final_Dataset.csv' not found in the root folder!");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line = br.readLine(); // Skip header
            int count = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                // Remove BOM if present on the first data row
                if (count == 0 && line.startsWith("\ufeff")) {
                    line = line.substring(1);
                }

                List<String> tokens = parseCsvLine(line);
                if (tokens.size() >= 10) {
                    try {
                        int id = Integer.parseInt(tokens.get(0).trim());
                        String title = tokens.get(1).trim();
                        String genre = tokens.get(2).trim();
                        String language = tokens.get(3).trim();
                        int year = Integer.parseInt(tokens.get(4).trim());
                        String director = tokens.get(5).trim();
                        String cast = tokens.get(6).trim();
                        double rating = Double.parseDouble(tokens.get(7).trim());
                        String description = tokens.get(8).trim();
                        String poster = tokens.get(9).trim();

                        Movie m = new Movie(id, title, genre, language, year, director, cast, rating, description, poster);
                        movies.add(m);
                        movieTitleMap.put(title.toLowerCase(), m);
                        count++;
                    } catch (Exception e) {
                        // Skip malformed lines
                    }
                }
            }
            System.out.println("Successfully Loaded " + movies.size() + " movies from dataset.");
        } catch (IOException e) {
            System.out.println("Failed to load dataset: " + e.getMessage());
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens;
    }

    // --- ALGORITHM 1: HashMap Exact Search O(1) ---
    private static Movie hashMapExactSearch(String query) {
        return movieTitleMap.get(query.toLowerCase().trim());
    }

    // --- ALGORITHM 2: KMP Substring Pattern Matching O(N + M) ---
    private static boolean kmpSearch(String text, String pattern) {
        if (pattern == null || pattern.isEmpty()) return true;
        if (text == null) return false;
        
        String t = text.toLowerCase();
        String p = pattern.toLowerCase();
        
        int[] lps = computeLPS(p);
        int i = 0, j = 0;
        while (i < t.length()) {
            if (p.charAt(j) == t.charAt(i)) {
                i++; j++;
            }
            if (j == p.length()) {
                return true;
            } else if (i < t.length() && p.charAt(j) != t.charAt(i)) {
                if (j != 0) j = lps[j - 1];
                else i++;
            }
        }
        return false;
    }

    private static int[] computeLPS(String pat) {
        int[] lps = new int[pat.length()];
        int len = 0, i = 1;
        lps[0] = 0;
        while (i < pat.length()) {
            if (pat.charAt(i) == pat.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }

    // --- ALGORITHM 3: Levenshtein Distance Fuzzy Search O(M x N) ---
    private static int computeLevenshtein(String a, String b) {
        int[] dp = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) dp[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            int prev = dp[0];
            dp[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int temp = dp[j];
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[j] = prev;
                } else {
                    dp[j] = 1 + Math.min(Math.min(dp[j], dp[j - 1]), prev);
                }
                prev = temp;
            }
        }
        return dp[b.length()];
    }

    // --- ALGORITHM 4: Merge Sort O(N log N) ---
    private static void mergeSort(List<Movie> list, String sortBy, boolean ascending) {
        if (list.size() <= 1) return;
        int mid = list.size() / 2;
        List<Movie> left = new ArrayList<>(list.subList(0, mid));
        List<Movie> right = new ArrayList<>(list.subList(mid, list.size()));

        mergeSort(left, sortBy, ascending);
        mergeSort(right, sortBy, ascending);

        merge(list, left, right, sortBy, ascending);
    }

    private static void merge(List<Movie> list, List<Movie> left, List<Movie> right, String sortBy, boolean ascending) {
        int i = 0, j = 0, k = 0;
        while (i < left.size() && j < right.size()) {
            Movie m1 = left.get(i);
            Movie m2 = right.get(j);
            int comp = compareMovies(m1, m2, sortBy);
            boolean condition = ascending ? (comp <= 0) : (comp >= 0);
            if (condition) {
                list.set(k++, m1);
                i++;
            } else {
                list.set(k++, m2);
                j++;
            }
        }
        while (i < left.size()) list.set(k++, left.get(i++));
        while (j < right.size()) list.set(k++, right.get(j++));
    }

    private static int compareMovies(Movie m1, Movie m2, String sortBy) {
        if ("title".equals(sortBy)) {
            return m1.title.compareToIgnoreCase(m2.title);
        } else if ("year".equals(sortBy)) {
            return Integer.compare(m1.year, m2.year);
        } else { // default rating
            return Double.compare(m1.rating, m2.rating);
        }
    }

    // --- ALGORITHM 5: Binary Search O(log N) ---
    private static Movie binarySearchByTitle(List<Movie> sortedList, String titleQuery) {
        int low = 0, high = sortedList.size() - 1;
        String q = titleQuery.toLowerCase().trim();
        
        System.out.println("\n--- Starting Binary Search for: '" + q + "' ---");
        
        while (low <= high) {
            int mid = low + (high - low) / 2;
            Movie m = sortedList.get(mid);
            System.out.println("-> Checking middle index " + mid + ": " + m.title);
            
            int res = m.title.toLowerCase().compareTo(q);
            if (res == 0) {
                System.out.println("-> Found exact match: " + m.title + "!");
                return m;
            }
            if (res < 0) {
                System.out.println("-> '" + q + "' comes after '" + m.title + "', moving right.");
                low = mid + 1;
            } else {
                System.out.println("-> '" + q + "' comes before '" + m.title + "', moving left.");
                high = mid - 1;
            }
        }
        System.out.println("-> Movie not found via Binary Search.");
        return null;
    }

    // HTTP Handlers
    static class MoviesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < movies.size(); i++) {
                sb.append(movies.get(i).toJson());
                if (i < movies.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendResponse(exchange, sb.toString());
        }
    }

    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = "";
            String type = "kmp";
            String lang = "All";
            String genre = "All";
            String sortField = "rating";
            String sortOrder = "desc";

            String queryParams = exchange.getRequestURI().getQuery();
            if (queryParams != null) {
                for (String param : queryParams.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length > 1) {
                        String val = java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                        if (pair[0].equals("q")) query = val;
                        else if (pair[0].equals("type")) type = val;
                        else if (pair[0].equals("lang")) lang = val;
                        else if (pair[0].equals("genre")) genre = val;
                        else if (pair[0].equals("sort")) sortField = val;
                        else if (pair[0].equals("order")) sortOrder = val;
                    }
                }
            }

            List<Movie> results = new ArrayList<>();

            if (type.equals("exact")) {
                Movie m = hashMapExactSearch(query);
                if (m != null) results.add(m);
            } else if (type.equals("binary")) {
                List<Movie> sortedCopy = new ArrayList<>(movies);
                mergeSort(sortedCopy, "title", true);
                Movie m = binarySearchByTitle(sortedCopy, query);
                if (m != null) results.add(m);
            } else if (type.equals("fuzzy")) {
                Movie bestMatch = null;
                int minCost = Integer.MAX_VALUE;
                for (Movie m : movies) {
                    int cost = computeLevenshtein(query.toLowerCase(), m.title.toLowerCase());
                    if (cost < minCost) {
                        minCost = cost;
                        bestMatch = m;
                    }
                }
                if (bestMatch != null && minCost <= 5) {
                    results.add(bestMatch);
                }
            } else { // KMP Substring search
                for (Movie m : movies) {
                    if (query.isEmpty() || 
                        kmpSearch(m.title, query) || 
                        kmpSearch(m.cast, query) || 
                        kmpSearch(m.director, query) || 
                        kmpSearch(m.genre, query)) {
                        results.add(m);
                    }
                }
            }

            // Apply Filters
            List<Movie> filtered = new ArrayList<>();
            for (Movie m : results) {
                boolean matchLang = lang.equals("All") || m.language.equalsIgnoreCase(lang);
                boolean matchGenre = genre.equals("All") || m.genre.toLowerCase().contains(genre.toLowerCase());
                if (matchLang && matchGenre) {
                    filtered.add(m);
                }
            }

            // Apply Merge Sort
            boolean ascending = sortOrder.equals("asc");
            mergeSort(filtered, sortField, ascending);

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < filtered.size(); i++) {
                sb.append(filtered.get(i).toJson());
                if (i < filtered.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendResponse(exchange, sb.toString());
        }
    }

    static class SimilarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String title = "";
            String queryParams = exchange.getRequestURI().getQuery();
            if (queryParams != null) {
                for (String param : queryParams.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length > 1 && pair[0].equals("title")) {
                        title = java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    }
                }
            }

            Movie target = movieTitleMap.get(title.toLowerCase().trim());
            List<Movie> similar = new ArrayList<>();
            if (target != null) {
                for (Movie m : movies) {
                    if (m.id != target.id && (m.genre.equalsIgnoreCase(target.genre) || m.director.equalsIgnoreCase(target.director))) {
                        similar.add(m);
                    }
                }
            }

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < Math.min(3, similar.size()); i++) {
                sb.append(similar.get(i).toJson());
                if (i < Math.min(3, similar.size()) - 1) sb.append(",");
            }
            sb.append("]");
            sendResponse(exchange, sb.toString());
        }
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            
            File file = new File("public" + path);
            if (!file.exists()) {
                String response = "404 (Not Found)";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            byte[] bytes = Files.readAllBytes(file.toPath());
            String mime = "text/plain";
            if (path.endsWith(".html")) mime = "text/html";
            else if (path.endsWith(".css")) mime = "text/css";
            else if (path.endsWith(".js")) mime = "application/javascript";

            exchange.getResponseHeaders().set("Content-Type", mime);
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}