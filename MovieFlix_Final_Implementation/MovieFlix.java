import java.io.*;
import java.util.*;

/*
 * MOVIEFLIX - DSA-3 FINAL IMPLEMENTATION
 *
 * Algorithms:
 * 1. HashMap           - exact title retrieval
 * 2. Merge Sort        - sorting movie records
 * 3. Binary Search     - searching sorted titles
 * 4. KMP               - keyword/pattern matching
 * 5. Levenshtein       - fuzzy title matching
 *
 * Dataset:
 * MovieFlix_Final_Dataset.csv
 */

public class MovieFlix {

    // ---------------- MOVIE MODEL ----------------
    static class Movie {
        int id;
        String title;
        String language;
        String genre;
        int year;
        String director;
        String description;

        Movie(int id, String title, String language, String genre,
              int year, String director, String description) {
            this.id = id;
            this.title = title;
            this.language = language;
            this.genre = genre;
            this.year = year;
            this.director = director;
            this.description = description;
        }

        String csvLine() {
            return id + "," + title + "," + language + "," + genre + "," +
                   year + "," + director + "," + description;
        }

        @Override
        public String toString() {
            return String.format(
                "%-4d %-32s %-10s %-12s %-6d %-25s",
                id, title, language, genre, year, director
            );
        }
    }

    static final String DATA_FILE = "MovieFlix_Final_Dataset.csv";

    static ArrayList<Movie> movies = new ArrayList<>();
    static HashMap<String, Movie> titleMap = new HashMap<>();

    // =========================================================
    // MAIN
    // =========================================================
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try {
            loadDataset();
        } catch (Exception e) {
            System.out.println("\nERROR: Could not load " + DATA_FILE);
            System.out.println("Keep MovieFlix.java and " + DATA_FILE +
                               " in the same folder.");
            System.out.println("Details: " + e.getMessage());
            sc.close();
            return;
        }

        while (true) {
            printMenu();
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    displayAll();
                    break;
                case "2":
                    exactSearchHashMap(sc);
                    break;
                case "3":
                    binarySearchTitle(sc);
                    break;
                case "4":
                    kmpKeywordSearch(sc);
                    break;
                case "5":
                    fuzzySearch(sc);
                    break;
                case "6":
                    sortByTitle();
                    break;
                case "7":
                    sortByYear();
                    break;
                case "8":
                    searchByLanguage(sc);
                    break;
                case "9":
                    searchByGenre(sc);
                    break;
                case "10":
                    addMovie(sc);
                    break;
                case "11":
                    updateMovie(sc);
                    break;
                case "12":
                    deleteMovie(sc);
                    break;
                case "13":
                    saveDataset();
                    break;
                case "14":
                    reloadDataset();
                    break;
                case "15":
                    executionDemo();
                    break;
                case "0":
                    System.out.println("\nMovieFlix closed.");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 0-15.");
            }
        }
    }

    // =========================================================
    // MENU
    // =========================================================
    static void printMenu() {
        System.out.println("\n==========================================================");
        System.out.println("                    MOVIEFLIX");
        System.out.println("             Intelligent Movie Search");
        System.out.println("==========================================================");
        System.out.println(" 1. Display all movies");
        System.out.println(" 2. Exact title search       [HashMap]");
        System.out.println(" 3. Title search             [Binary Search]");
        System.out.println(" 4. Keyword search           [KMP]");
        System.out.println(" 5. Approximate title search [Fuzzy / Levenshtein]");
        System.out.println(" 6. Sort by title            [Merge Sort]");
        System.out.println(" 7. Sort by release year      [Merge Sort]");
        System.out.println(" 8. Search by language");
        System.out.println(" 9. Search by genre");
        System.out.println("10. Add movie                [Dataset Change]");
        System.out.println("11. Update movie              [Dataset Change]");
        System.out.println("12. Delete movie              [Dataset Change]");
        System.out.println("13. Save dataset              [Write CSV]");
        System.out.println("14. Reload dataset            [Read Updated CSV]");
        System.out.println("15. Algorithm execution demo");
        System.out.println(" 0. Exit");
        System.out.println("==========================================================");
    }

    // =========================================================
    // DATASET LOADING
    // =========================================================
    static void loadDataset() throws IOException {
        movies.clear();
        titleMap.clear();

        BufferedReader br = new BufferedReader(new FileReader(DATA_FILE));

        String header = br.readLine();
        if (header == null) {
            br.close();
            throw new IOException("Dataset is empty.");
        }

        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] p = parseCSVLine(line);

            if (p.length < 7) continue;

            try {
                int id = Integer.parseInt(p[0].trim());
                String title = p[1].trim();
                String language = p[2].trim();
                String genre = p[3].trim();
                int year = Integer.parseInt(p[4].trim());
                String director = p[5].trim();
                String description = p[6].trim();

                Movie m = new Movie(
                    id, title, language, genre, year, director, description
                );

                movies.add(m);
                titleMap.put(normalize(title), m);

            } catch (NumberFormatException e) {
                System.out.println("Skipped invalid row: " + line);
            }
        }

        br.close();

        System.out.println("\nDataset loaded successfully.");
        System.out.println("Movies loaded: " + movies.size());
        System.out.println("HashMap records: " + titleMap.size());
    }

    // Simple CSV parser supporting quoted fields.
    static String[] parseCSVLine(String line) {
        ArrayList<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                insideQuotes = !insideQuotes;
            } else if (c == ',' && !insideQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    static String normalize(String s) {
        return s.trim().toLowerCase();
    }

    // =========================================================
    // DISPLAY
    // =========================================================
    static void displayAll() {
        System.out.println("\n---------------- MOVIE DATASET ----------------");
        System.out.printf("%-4s %-32s %-10s %-12s %-6s %-25s%n",
                "ID", "TITLE", "LANG", "GENRE", "YEAR", "DIRECTOR");
        System.out.println("------------------------------------------------------------------------------------------");

        for (Movie m : movies) {
            System.out.println(m);
        }

        System.out.println("\nTotal movies: " + movies.size());
    }

    static void printMovie(Movie m) {
        System.out.println("\n---------------- MOVIE DETAILS ----------------");
        System.out.println("Movie ID    : " + m.id);
        System.out.println("Title       : " + m.title);
        System.out.println("Language    : " + m.language);
        System.out.println("Genre       : " + m.genre);
        System.out.println("Release Year: " + m.year);
        System.out.println("Director    : " + m.director);
        System.out.println("Description : " + m.description);
        System.out.println("-----------------------------------------------");
    }

    // =========================================================
    // HASHMAP EXACT SEARCH
    // =========================================================
    static void exactSearchHashMap(Scanner sc) {
        System.out.print("\nEnter exact movie title: ");
        String query = sc.nextLine().trim();

        Movie result = titleMap.get(normalize(query));

        System.out.println("\n[HashMap Exact Search]");
        System.out.println("Key searched: " + query);

        if (result != null) {
            System.out.println("HashMap lookup successful.");
            printMovie(result);
        } else {
            System.out.println("No exact title found.");
        }
    }

    // =========================================================
    // MERGE SORT
    // =========================================================
    static ArrayList<Movie> mergeSort(ArrayList<Movie> list,
                                      Comparator<Movie> comparator) {
        if (list.size() <= 1) return list;

        int mid = list.size() / 2;

        ArrayList<Movie> left =
            new ArrayList<>(list.subList(0, mid));

        ArrayList<Movie> right =
            new ArrayList<>(list.subList(mid, list.size()));

        left = mergeSort(left, comparator);
        right = mergeSort(right, comparator);

        return merge(left, right, comparator);
    }

    static ArrayList<Movie> merge(ArrayList<Movie> left,
                                  ArrayList<Movie> right,
                                  Comparator<Movie> comparator) {
        ArrayList<Movie> result = new ArrayList<>();

        int i = 0;
        int j = 0;

        while (i < left.size() && j < right.size()) {
            if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }

        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));

        return result;
    }

    static void sortByTitle() {
        ArrayList<Movie> sorted = mergeSort(
            new ArrayList<>(movies),
            Comparator.comparing(m -> normalize(m.title))
        );

        System.out.println("\n[Merge Sort - Alphabetical Title Order]");
        for (Movie m : sorted) {
            System.out.println(m);
        }
    }

    static void sortByYear() {
        ArrayList<Movie> sorted = mergeSort(
            new ArrayList<>(movies),
            Comparator.comparingInt(m -> m.year)
        );

        System.out.println("\n[Merge Sort - Release Year Order]");
        for (Movie m : sorted) {
            System.out.println(m);
        }
    }

    // =========================================================
    // BINARY SEARCH
    // =========================================================
    static int binarySearch(ArrayList<Movie> sorted, String target) {
        int low = 0;
        int high = sorted.size() - 1;

        String key = normalize(target);

        while (low <= high) {
            int mid = low + (high - low) / 2;

            String midTitle = normalize(sorted.get(mid).title);

            int comparison = midTitle.compareTo(key);

            if (comparison == 0) {
                return mid;
            } else if (comparison < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return -1;
    }

    static void binarySearchTitle(Scanner sc) {
        System.out.print("\nEnter movie title: ");
        String query = sc.nextLine();

        ArrayList<Movie> sorted = mergeSort(
            new ArrayList<>(movies),
            Comparator.comparing(m -> normalize(m.title))
        );

        int result = binarySearch(sorted, query);

        System.out.println("\n[Binary Search]");
        System.out.println("Search key: " + query);
        System.out.println("Dataset sorted using Merge Sort first.");

        if (result >= 0) {
            System.out.println("Binary Search: Movie found.");
            printMovie(sorted.get(result));
        } else {
            System.out.println("Binary Search: Movie not found.");
        }
    }

    // =========================================================
    // KMP PATTERN MATCHING
    // =========================================================
    static int[] buildLPS(String pattern) {
        int[] lps = new int[pattern.length()];

        int length = 0;
        int i = 1;

        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(length)) {
                length++;
                lps[i] = length;
                i++;
            } else if (length != 0) {
                length = lps[length - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }

        return lps;
    }

    static boolean kmpContains(String text, String pattern) {
        text = normalize(text);
        pattern = normalize(pattern);

        if (pattern.isEmpty()) return true;

        int[] lps = buildLPS(pattern);

        int i = 0;
        int j = 0;

        while (i < text.length()) {

            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;

                if (j == pattern.length()) {
                    return true;
                }

            } else if (j > 0) {
                j = lps[j - 1];

            } else {
                i++;
            }
        }

        return false;
    }

    static void kmpKeywordSearch(Scanner sc) {
        System.out.print("\nEnter keyword: ");
        String keyword = sc.nextLine().trim();

        if (keyword.isEmpty()) {
            System.out.println("Keyword cannot be empty.");
            return;
        }

        int[] lps = buildLPS(normalize(keyword));

        System.out.println("\n[KMP Pattern Matching]");
        System.out.println("Pattern : " + keyword);
        System.out.println("LPS     : " + Arrays.toString(lps));

        int count = 0;

        for (Movie m : movies) {
            boolean titleMatch = kmpContains(m.title, keyword);
            boolean descriptionMatch = kmpContains(m.description, keyword);

            if (titleMatch || descriptionMatch) {
                System.out.println("MATCH -> " + m.title);
                count++;
            }
        }

        System.out.println("Total matches: " + count);
    }

    // =========================================================
    // FUZZY MATCHING - LEVENSHTEIN DISTANCE
    // =========================================================
    static int levenshteinDistance(String a, String b) {
        a = normalize(a);
        b = normalize(b);

        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {

                int cost =
                    (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;

                int insertion = dp[i][j - 1] + 1;
                int deletion = dp[i - 1][j] + 1;
                int substitution = dp[i - 1][j - 1] + cost;

                dp[i][j] = Math.min(
                    Math.min(insertion, deletion),
                    substitution
                );
            }
        }

        return dp[a.length()][b.length()];
    }

    static double similarity(String a, String b) {
        int maxLength = Math.max(a.length(), b.length());

        if (maxLength == 0) return 100.0;

        int distance = levenshteinDistance(a, b);

        return (1.0 - ((double) distance / maxLength)) * 100.0;
    }

    static void fuzzySearch(Scanner sc) {
        System.out.print("\nEnter approximate movie title: ");
        String query = sc.nextLine().trim();

        if (query.isEmpty()) {
            System.out.println("Query cannot be empty.");
            return;
        }

        class Match {
            Movie movie;
            int distance;
            double similarity;

            Match(Movie movie, int distance, double similarity) {
                this.movie = movie;
                this.distance = distance;
                this.similarity = similarity;
            }
        }

        ArrayList<Match> matches = new ArrayList<>();

        for (Movie m : movies) {
            int distance = levenshteinDistance(query, m.title);
            double sim = similarity(query, m.title);

            // Academic/demo threshold: show reasonably similar titles.
            if (sim >= 60.0) {
                matches.add(new Match(m, distance, sim));
            }
        }

        matches.sort((a, b) -> {
            if (a.distance != b.distance) {
                return Integer.compare(a.distance, b.distance);
            }
            return Double.compare(b.similarity, a.similarity);
        });

        System.out.println("\n[Fuzzy Matching - Levenshtein Distance]");
        System.out.println("Query: " + query);

        if (matches.isEmpty()) {
            System.out.println("No sufficiently similar movie title found.");
            return;
        }

        int limit = Math.min(5, matches.size());

        System.out.println("Similar movie suggestions:");

        for (int i = 0; i < limit; i++) {
            Match x = matches.get(i);

            System.out.printf(
                "%d. %s | Edit Distance: %d | Similarity: %.1f%%%n",
                i + 1,
                x.movie.title,
                x.distance,
                x.similarity
            );
        }
    }

    // =========================================================
    // LANGUAGE / GENRE SEARCH
    // =========================================================
    static void searchByLanguage(Scanner sc) {
        System.out.print("\nEnter language: ");
        String language = normalize(sc.nextLine());

        int count = 0;

        System.out.println("\nMovies in language: " + language);

        for (Movie m : movies) {
            if (normalize(m.language).equals(language)) {
                System.out.println(m);
                count++;
            }
        }

        System.out.println("Total: " + count);
    }

    static void searchByGenre(Scanner sc) {
        System.out.print("\nEnter genre: ");
        String genre = normalize(sc.nextLine());

        int count = 0;

        System.out.println("\nMovies in genre: " + genre);

        for (Movie m : movies) {
            if (normalize(m.genre).equals(genre)) {
                System.out.println(m);
                count++;
            }
        }

        System.out.println("Total: " + count);
    }

    // =========================================================
    // DATASET MODIFICATION
    // =========================================================
    static void addMovie(Scanner sc) {
        System.out.println("\n[ADD MOVIE]");

        int id = nextId();

        System.out.println("New Movie ID: " + id);

        System.out.print("Title: ");
        String title = sc.nextLine().trim();

        if (title.isEmpty()) {
            System.out.println("Title cannot be empty.");
            return;
        }

        if (titleMap.containsKey(normalize(title))) {
            System.out.println("A movie with this title already exists.");
            return;
        }

        System.out.print("Language: ");
        String language = sc.nextLine().trim();

        System.out.print("Genre: ");
        String genre = sc.nextLine().trim();

        int year = readInt(sc, "Release Year: ");

        System.out.print("Director: ");
        String director = sc.nextLine().trim();

        System.out.print("Description: ");
        String description = sc.nextLine().trim();

        Movie m = new Movie(
            id, title, language, genre, year, director, description
        );

        movies.add(m);
        titleMap.put(normalize(title), m);

        System.out.println("Movie added successfully.");
        System.out.println("IMPORTANT: Choose option 13 to save it to the CSV.");
    }

    static void updateMovie(Scanner sc) {
        System.out.print("\nEnter exact title to update: ");
        String query = sc.nextLine().trim();

        Movie m = titleMap.get(normalize(query));

        if (m == null) {
            System.out.println("Movie not found.");
            return;
        }

        printMovie(m);

        System.out.println("\nEnter new values. Press ENTER to keep existing value.");

        System.out.print("New Title [" + m.title + "]: ");
        String title = sc.nextLine().trim();
        if (!title.isEmpty()) {
            titleMap.remove(normalize(m.title));
            m.title = title;
            titleMap.put(normalize(m.title), m);
        }

        System.out.print("New Language [" + m.language + "]: ");
        String language = sc.nextLine().trim();
        if (!language.isEmpty()) m.language = language;

        System.out.print("New Genre [" + m.genre + "]: ");
        String genre = sc.nextLine().trim();
        if (!genre.isEmpty()) m.genre = genre;

        System.out.print("New Year [" + m.year + "]: ");
        String year = sc.nextLine().trim();
        if (!year.isEmpty()) {
            try {
                m.year = Integer.parseInt(year);
            } catch (NumberFormatException e) {
                System.out.println("Invalid year. Old year retained.");
            }
        }

        System.out.print("New Director [" + m.director + "]: ");
        String director = sc.nextLine().trim();
        if (!director.isEmpty()) m.director = director;

        System.out.print("New Description [" + m.description + "]: ");
        String description = sc.nextLine().trim();
        if (!description.isEmpty()) m.description = description;

        System.out.println("Movie updated in memory.");
        System.out.println("IMPORTANT: Choose option 13 to save the change to CSV.");
    }

    static void deleteMovie(Scanner sc) {
        System.out.print("\nEnter exact title to delete: ");
        String query = sc.nextLine().trim();

        Movie m = titleMap.get(normalize(query));

        if (m == null) {
            System.out.println("Movie not found.");
            return;
        }

        movies.remove(m);
        titleMap.remove(normalize(m.title));

        System.out.println("Movie deleted from memory.");
        System.out.println("IMPORTANT: Choose option 13 to save the change to CSV.");
    }

    static int nextId() {
        int max = 0;

        for (Movie m : movies) {
            if (m.id > max) max = m.id;
        }

        return max + 1;
    }

    static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);

            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    // =========================================================
    // SAVE / RELOAD
    // =========================================================
    static void saveDataset() {
        try {
            PrintWriter out = new PrintWriter(
                new BufferedWriter(
                    new FileWriter(DATA_FILE)
                )
            );

            out.println(
                "Movie_ID,Title,Language,Genre,Release_Year,Director,Description"
            );

            for (Movie m : movies) {
                out.println(m.id + "," +
                            toCSV(m.title) + "," +
                            toCSV(m.language) + "," +
                            toCSV(m.genre) + "," +
                            m.year + "," +
                            toCSV(m.director) + "," +
                            toCSV(m.description));
            }

            out.close();

            System.out.println("\nDataset saved successfully.");
            System.out.println("File: " + DATA_FILE);

        } catch (IOException e) {
            System.out.println("Could not save dataset: " + e.getMessage());
        }
    }

    static String toCSV(String value) {
        if (value.contains(",") || value.contains("\"") ||
            value.contains("\n")) {

            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }

    static void reloadDataset() {
        try {
            loadDataset();
            System.out.println("Updated CSV reloaded successfully.");
        } catch (IOException e) {
            System.out.println("Reload failed: " + e.getMessage());
        }
    }

    // =========================================================
    // COMPLETE EXECUTION DEMO
    // =========================================================
    static void executionDemo() {
        System.out.println("\n==========================================================");
        System.out.println("              ALGORITHM EXECUTION DEMO");
        System.out.println("==========================================================");

        // HASHMAP
        System.out.println("\n1. HASHMAP EXACT SEARCH");

        String hashQuery = movies.isEmpty() ? "" : movies.get(0).title;

        Movie hashResult = titleMap.get(normalize(hashQuery));

        System.out.println("Input: " + hashQuery);
        System.out.println(
            hashResult != null
            ? "Output: Movie retrieved successfully -> " + hashResult.title
            : "Output: Movie not found"
        );

        // MERGE SORT
        System.out.println("\n2. MERGE SORT");

        ArrayList<Movie> sorted = mergeSort(
            new ArrayList<>(movies),
            Comparator.comparing(m -> normalize(m.title))
        );

        System.out.println("Output: Movie titles sorted alphabetically.");

        int show = Math.min(5, sorted.size());
        for (int i = 0; i < show; i++) {
            System.out.println((i + 1) + ". " + sorted.get(i).title);
        }

        // BINARY SEARCH
        System.out.println("\n3. BINARY SEARCH");

        if (!sorted.isEmpty()) {
            String target = sorted.get(sorted.size() / 2).title;

            int index = binarySearch(sorted, target);

            System.out.println("Input: " + target);
            System.out.println(
                index >= 0
                ? "Output: Movie found at sorted index " + index
                : "Output: Movie not found"
            );
        }

        // KMP
        System.out.println("\n4. KMP PATTERN MATCHING");

        String text =
            "A team of astronauts travels through space to explore a new planet.";

        String pattern = "space";

        System.out.println("Text: " + text);
        System.out.println("Pattern: " + pattern);
        System.out.println(
            "LPS: " + Arrays.toString(buildLPS(pattern))
        );
        System.out.println(
            "Output: " +
            (kmpContains(text, pattern)
                ? "PATTERN FOUND"
                : "PATTERN NOT FOUND")
        );

        // FUZZY
        System.out.println("\n5. FUZZY MATCHING");

        String wrong = "Interstelar";
        String correct = "Interstellar";

        int distance =
            levenshteinDistance(wrong, correct);

        double sim =
            similarity(wrong, correct);

        System.out.println("User query: " + wrong);
        System.out.println("Candidate: " + correct);
        System.out.println("Edit Distance: " + distance);
        System.out.printf("Similarity: %.1f%%%n", sim);
        System.out.println(
            sim >= 60
            ? "Output: SIMILAR MATCH -> " + correct
            : "Output: NO MATCH"
        );

        System.out.println("\n==========================================================");
        System.out.println("             EXECUTION DEMO COMPLETE");
        System.out.println("==========================================================");
    }
}
