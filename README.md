# MovieFlix
## Team Members

| S.No | Team Member Name | ID Number |
|------|------------------|-----------|
| 1 | Hemaswi | 2520030069 |
| 2 | Simi Ayana | 2520030254 |
| 3 | S.Rushvini | 2520030542|

## Supervisor

**Dr. V. Sireesha**  
Professor, Department of CSE

---

## Abstract

**MovieFlix** is a movie-focused web application with a dark-themed browser interface and a Java backend. The homepage is designed to help users discover movies through a search panel and featured movie cards. The frontend uses HTML, CSS, and JavaScript, while Java source files handle application and server-side responsibilities.

The project also contains `AlgorithmLab.java` for DSA-related work. DSA topics should be described as implemented only after confirming that the corresponding Java methods perform the actual algorithms.

## Objectives

- Develop a movie discovery interface.
- Provide a search field for movie titles, genres, or keywords.
- Display featured movies using poster cards and movie details.
- Connect the frontend to Java server-side code.
- Organize movie-related request handling.
- Explore and document relevant Data Structures and Algorithms concepts.

## Features

Based on the application output shown in the project demo:

- **MovieFlix navigation bar** at the top of the page.
- **Movie discovery heading:** “Discover Cinematic Masterpieces”.
- **Movie search field** for entering a title, genre, or keyword.
- **Find button** to submit the search.
- **Featured Films section** containing movie cards.
- **Movie poster artwork** displayed on the cards.
- **Movie titles and category/genre labels** shown with the cards.
- **Trending indicators** on selected cards.
- **Dark-themed user interface** with contrasting accent colors.
- Java backend and separate request handlers for movie-related operations, depending on the current implementation.

> Search, CRUD, and algorithm-lab operations should be listed as fully working features only after they have been tested in the running application.

## Example Output

When MovieFlix opens in the browser, the page shows a navigation bar, a movie search panel, and a Featured Films section.

### Homepage preview (text representation)

```text
MovieFlix

             Discover Cinematic Masterpieces
      Search by title, genre, or type keyword for amazing movies.

      [ Enter a movie title / genre / keyword             ] [ Find ]

Featured Films

+---------------------+  +---------------------+  +---------------------+  +---------------------+
|    Movie poster     |  |    Movie poster     |  |    Movie poster     |  |    Movie poster     |
| The Dark Knight     |  | Movie title         |  | RRR                 |  | Movie title         |
| Genre/category      |  | Genre/category      |  | Genre/category      |  | Genre/category      |
| Trending (if shown) |  | Trending (if shown) |  | Trending (if shown) |  | Trending (if shown) |
+---------------------+  +---------------------+  +---------------------+  +---------------------+
```

This is a simplified text representation of the demonstrated page, not a literal screenshot. Replace generic movie titles with the exact titles shown by your running application if you want the README example to match every card.

### Example search

**Input:** `The Dark Knight`

**Action:** Enter the movie title in the search field and click **Find**.

**Expected behavior:** If search is connected to the movie data and the title exists, the application should show the matching movie result or details in the format implemented by the project.

```text
Search query: The Dark Knight
Button clicked: Find
Result: Matching movie information (when available in the configured dataset)
```

## Technologies Used

- **Java** — backend and application logic
- **HTML5** — webpage structure
- **CSS3** — styling and layout
- **JavaScript** — browser-side interactions
- **VS Code** — development environment

## Data Structures and Algorithms (DSA)

The project includes `AlgorithmLab.java`. Document only the topics whose implementation is confirmed in that source file.

### String Searching and Matching

- **Naive Pattern Matching** — checks possible text positions for a pattern.
- **Knuth–Morris–Pratt (KMP)** — uses prefix/suffix information to reduce repeated comparisons.
- **Rabin–Karp** — uses hashing to compare a pattern against text windows.
- **Levenshtein Edit Distance** — uses dynamic programming to count the minimum insertions, deletions, and substitutions needed to transform one string into another.

### Sorting

- **Merge Sort** — divides a collection, sorts the smaller sections, and merges them.
- **Parallel Merge Sort** — performs merge-sort work concurrently, if implemented in the project.

### Supporting Concepts

- **Lists and collections** — useful for storing groups of movie records.
- **Maps and sets** — useful for key-based lookup and tracking unique values, if used in the source.
- **File/data handling** — loading or processing the configured movie data.
- **HTTP request routing** — directing browser requests to the relevant Java handler.

**Important:** If a method only returns a descriptive message instead of carrying out the algorithm, describe it as a placeholder rather than a completed implementation.

## Project Structure

```text
Movie/
├── public/
│   ├── app.js                    # Browser-side JavaScript
│   ├── index.html                # Main webpage
│   └── style.css                 # Page styling
├── AlgorithmLab.java             # DSA/algorithm lab
├── MovieFlix.java                # Movie application logic
├── Server.java                   # Java server and request handlers
├── MovieFlix_Final_Database...   # Movie data/database file
└── README.md                     # Project documentation
```

Compiled `.class` files may also appear after compilation. They are generated output; normally, edit the `.java` source files instead.

## Requirements

- Java Development Kit (JDK)
- Visual Studio Code or another Java-compatible IDE
- A modern web browser

Check Java from the VS Code terminal:

```bash
java -version
javac -version
```

## How to Run

1. Open the main `Movie` folder in VS Code.
2. Select **Terminal → New Terminal**.
3. Ensure the terminal is in the project root.
4. Compile the Java source files:

   ```bash
   javac *.java
   ```

5. Run the class that contains the server's `main` method. If it is `Server`, use:

   ```bash
   java Server
   ```

6. Check the terminal for the local URL and port.
7. Open that address in your browser.

If the actual entry-point class or compilation requirements differ, follow the instructions in your Java source code.

## Main Source Files

- `public/index.html` — webpage structure.
- `public/style.css` — colors, spacing, layout, and visual styling.
- `public/app.js` — browser interactions and server requests, if configured.
- `Server.java` — server startup and request routing, depending on its implementation.
- `MovieFlix.java` — movie-related application logic.
- `AlgorithmLab.java` — algorithm-lab code.
- `MovieFlix_Final_Database...` — movie data file; replace the shortened name with the exact filename and document its format.

## Testing Checklist

- [ ] Java files compile without errors.
- [ ] Server starts and displays a local address.
- [ ] Homepage opens in a browser.
- [ ] Poster images and CSS load correctly.
- [ ] Featured movie cards display as expected.
- [ ] Search returns matching results, if implemented.
- [ ] CRUD operations work, if enabled.
- [ ] Invalid input is handled appropriately.
- [ ] Algorithm-lab methods return correct results.

## Troubleshooting

- **`javac` is not recognized:** Install a JDK and configure your system `PATH`.
- **Compilation errors:** Run the compile command from the project root and inspect missing-class or dependency errors.
- **Homepage does not open:** Confirm the server is running and use the exact URL and port printed in the terminal.
- **Movie data is missing:** Check the exact data filename and path expected by the Java code.
- **Search does not return results:** Check the browser console, server terminal, and configured data source.

## Future Enhancements

- Add filters for genre, release year, and rating if the dataset supports them.
- Add sorting and pagination for movie results.
- Improve form validation and user-friendly error messages.
- Complete and test each algorithm in `AlgorithmLab.java`.
- Document algorithm time and space complexity after verifying each implementation.
- Improve mobile responsiveness and accessibility.

## Conclusion

MovieFlix presents a movie discovery homepage with a search panel and featured movie cards, backed by Java application code. This README describes the visible interface, project organization, setup, example usage, and DSA concepts that can be verified in the source.

## Author

Add your name or team members here.

## License

Add a license if you plan to distribute this project publicly.
