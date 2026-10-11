import java.io.*;
import java.util.*;

public class MovieFlix {
    public static class Movie {
        public int id;
        public String title;
        public String genre;
        public String language;
        public int year;
        public String director;
        public String description;
        public String poster;

        public Movie(int id, String title, String genre, String language, int year, String director, String description, String poster) {
            this.id = id;
            this.title = title;
            this.genre = genre;
            this.language = language;
            this.year = year;
            this.director = director;
            this.description = description;
            this.poster = poster;
        }
    }

    private List<Movie> movies = new ArrayList<>();
    private Map<String, Movie> titleMap = new HashMap<>();

    public void loadDataset(String csvPath) {
        movies.clear();
        titleMap.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] parts = parseCSVLine(line);
                if (parts.length >= 8) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String title = parts[1].trim();
                        String genre = parts[2].trim();
                        String language = parts[3].trim();
                        int year = Integer.parseInt(parts[4].trim());
                        String director = parts[5].trim();
                        String description = parts[6].trim();
                        String poster = parts[7].trim();

                        Movie m = new Movie(id, title, genre, language, year, director, description, poster);
                        movies.add(m);
                        titleMap.put(title.toLowerCase(), m);
                    } catch (Exception e) {
                        // Skip malformed records
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading dataset: " + e.getMessage());
        }
    }

    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
    }

    public List<Movie> getAllMovies() {
        return movies;
    }

    // 1. HashMap Exact Search
    public Movie exactSearch(String title) {
        if (title == null) return null;
        return titleMap.get(title.toLowerCase().trim());
    }

    // 2. KMP Keyword Search
    public List<Movie> kmpSearch(String keyword) {
        List<Movie> results = new ArrayList<>();
        if (keyword == null || keyword.isEmpty()) return results;
        String pat = keyword.toLowerCase();
        for (Movie m : movies) {
            if (kmpMatch(m.title.toLowerCase(), pat) || kmpMatch(m.genre.toLowerCase(), pat) || kmpMatch(m.director.toLowerCase(), pat)) {
                results.add(m);
            }
        }
        return results;
    }

    private boolean kmpMatch(String txt, String pat) {
        int M = pat.length();
        int N = txt.length();
        if (M > N) return false;
        int[] lps = new int[M];
        computeLPSArray(pat, M, lps);
        int i = 0, j = 0;
        while (i < N) {
            if (pat.charAt(j) == txt.charAt(i)) {
                i++; j++;
            }
            if (j == M) {
                return true;
            } else if (i < N && pat.charAt(j) != txt.charAt(i)) {
                if (j != 0) j = lps[j - 1];
                else i++;
            }
        }
        return false;
    }

    private void computeLPSArray(String pat, int M, int[] lps) {
        int len = 0;
        int i = 1;
        lps[0] = 0;
        while (i < M) {
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
    }

    // 3. Levenshtein Fuzzy Search
    public List<Movie> fuzzySearch(String query, int threshold) {
        List<Movie> results = new ArrayList<>();
        if (query == null) return results;
        String q = query.toLowerCase().trim();
        for (Movie m : movies) {
            int dist = computeLevenshtein(m.title.toLowerCase(), q);
            if (dist <= threshold || m.title.toLowerCase().contains(q)) {
                results.add(m);
            }
        }
        return results;
    }

    private int computeLevenshtein(String s1, String s2) {
        int[] prev = new int[s2.length() + 1];
        for (int j = 0; j <= s2.length(); j++) prev[j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            int[] curr = new int[s2.length() + 1];
            curr[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            prev = curr;
        }
        return prev[s2.length()];
    }

    // 4. Merge Sort implementation for sorting by Title or Year
    public void sortMovies(List<Movie> list, String sortBy, boolean ascending) {
        if (list == null || list.size() <= 1) return;
        mergeSort(list, 0, list.size() - 1, sortBy, ascending);
    }

    private void mergeSort(List<Movie> list, int l, int r, String sortBy, boolean ascending) {
        if (l < r) {
            int m = l + (r - l) / 2;
            mergeSort(list, l, m, sortBy, ascending);
            mergeSort(list, m + 1, r, sortBy, ascending);
            merge(list, l, m, r, sortBy, ascending);
        }
    }

    private void merge(List<Movie> list, int l, int m, int r, String sortBy, boolean ascending) {
        int n1 = m - l + 1;
        int n2 = r - m;
        List<Movie> L = new ArrayList<>(n1);
        List<Movie> R = new ArrayList<>(n2);
        for (int i = 0; i < n1; i++) L.add(list.get(l + i));
        for (int j = 0; j < n2; j++) R.add(list.get(m + 1 + j));

        int i = 0, j = 0, k = l;
        while (i < n1 && j < n2) {
            boolean condition;
            if (sortBy.equalsIgnoreCase("year")) {
                condition = ascending ? (L.get(i).year <= R.get(j).year) : (L.get(i).year >= R.get(j).year);
            } else {
                int cmp = L.get(i).title.compareToIgnoreCase(R.get(j).title);
                condition = ascending ? (cmp <= 0) : (cmp >= 0);
            }
            if (condition) {
                list.set(k++, L.get(i++));
            } else {
                list.set(k++, R.get(j++));
            }
        }
        while (i < n1) list.set(k++, L.get(i++));
        while (j < n2) list.set(k++, R.get(j++));
    }

    // CRUD methods
    public synchronized void addMovie(Movie m) {
        movies.add(m);
        titleMap.put(m.title.toLowerCase(), m);
        saveDataset("MovieFlix_Final_Dataset.csv");
    }

    public synchronized boolean updateMovie(int id, Movie updated) {
        for (int i = 0; i < movies.size(); i++) {
            if (movies.get(i).id == id) {
                movies.set(i, updated);
                titleMap.put(updated.title.toLowerCase(), updated);
                saveDataset("MovieFlix_Final_Dataset.csv");
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteMovie(int id) {
        Iterator<Movie> it = movies.iterator();
        while (it.hasNext()) {
            Movie m = it.next();
            if (m.id == id) {
                it.remove();
                titleMap.remove(m.title.toLowerCase());
                saveDataset("MovieFlix_Final_Dataset.csv");
                return true;
            }
        }
        return false;
    }

    public void saveDataset(String csvPath) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvPath))) {
            pw.println("id,title,genre,language,year,director,description,poster");
            for (Movie m : movies) {
                pw.printf("%d,\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\"\n",
                        m.id, escape(m.title), escape(m.genre), escape(m.language), m.year, escape(m.director), escape(m.description), escape(m.poster));
            }
        } catch (Exception e) {
            System.err.println("Error saving dataset: " + e.getMessage());
        }
    }

    private String escape(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }
}