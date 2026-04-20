package utils;

import model.Genre;
import model.Movie;
import model.MovieResponse;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.util.List;
import java.util.stream.Collectors;

public class TableRenderer {

    private static final CellStyle CENTER =
            new CellStyle(CellStyle.HorizontalAlign.CENTER);
    private static final CellStyle LEFT =
            new CellStyle(CellStyle.HorizontalAlign.LEFT);

    // Movie List (without trailer)
    public static void displayMovieTable(MovieResponse movieResponse, String query) {

        int totalResults = movieResponse != null && movieResponse.getTotalResults() != null
                ? movieResponse.getTotalResults()
                : 0;

        System.out.printf("%nTotal results: %d | Query: \"%s\"%n%n",
                totalResults, query);

        Table table = new Table(5, BorderStyle.CLASSIC, ShownBorders.ALL);

        String[] headers = {"ID", "Title", "Release", "Rating", "Trailer"};

        for (String h : headers) {
            table.addCell(h, CENTER);
        }

        List<Movie> movies =
                movieResponse != null ? movieResponse.getResults() : null;

        if (movies == null || movies.isEmpty()) {
            table.addCell("No results found.", CENTER, 5);
        } else {
            for (Movie m : movies) {
                table.addCell(String.valueOf(m.getId()), CENTER);
                table.addCell(safe(m.getTitle()));
                table.addCell(safe(m.getReleaseDate()));
                table.addCell(m.getVoteAverage() != null
                        ? String.format("%.1f", m.getVoteAverage())
                        : "N/A", CENTER);
                table.addCell("N/A", CENTER);
            }
        }

        System.out.println(table.render());
    }

    // Movie List (with trailers)
    public static void displayMovieTableWithTrailers(
            MovieResponse movieResponse,
            String query,
            List<String> trailerUrls) {

        int totalResults = movieResponse != null && movieResponse.getTotalResults() != null
                ? movieResponse.getTotalResults()
                : 0;

        System.out.printf("%nTotal results: %d | Query: \"%s\"%n%n",
                totalResults, query);

        Table table = new Table(5, BorderStyle.CLASSIC, ShownBorders.ALL);

        String[] headers = {"ID", "Title", "Release", "Rating", "Trailer"};

        for (String h : headers) {
            table.addCell(h, CENTER);
        }

        List<Movie> movies =
                movieResponse != null ? movieResponse.getResults() : null;

        if (movies == null || movies.isEmpty()) {
            table.addCell("No results found.", CENTER, 5);
        } else {
            for (int i = 0; i < movies.size(); i++) {
                Movie m = movies.get(i);

                String trailer = (trailerUrls != null &&
                        i < trailerUrls.size() &&
                        trailerUrls.get(i) != null)
                        ? trailerUrls.get(i)
                        : "N/A";

                table.addCell(String.valueOf(m.getId()), CENTER);
                table.addCell(safe(m.getTitle()));
                table.addCell(safe(m.getReleaseDate()));
                table.addCell(m.getVoteAverage() != null
                        ? String.format("%.1f", m.getVoteAverage())
                        : "N/A", CENTER);
                table.addCell(trailer, LEFT);
            }
        }

        System.out.println(table.render());
    }

    // Movie Detail
    public static void displayMovieDetail(Movie movie, String trailerUrl) {

        Table table = new Table(4, BorderStyle.CLASSIC, ShownBorders.ALL);

        table.addCell(" MOVIE DETAIL ", CENTER, 4);

        addRow(table, "Title", safe(movie.getTitle()));
        addRow(table, "Release", safe(movie.getReleaseDate()));
        addRow(table, "Rating",
                movie.getVoteAverage() != null
                        ? String.format("%.1f / 10", movie.getVoteAverage())
                        : "N/A");

        String genres = "N/A";
        if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
            genres = movie.getGenres().stream()
                    .map(Genre::getName)
                    .collect(Collectors.joining(", "));
        }

        addRow(table, "Genres", genres);
        addRow(table, "Trailer", trailerUrl != null ? trailerUrl : "N/A");

        System.out.println(table.render());
    }

    private static void addRow(Table table, String label, String value) {
        table.addCell(" " + label, CENTER);
        table.addCell(" " + value, LEFT, 3);
    }

    public static void tableMenu() {

        Table tableMenu = new Table(1, BorderStyle.CLASSIC, ShownBorders.ALL);

        tableMenu.addCell("MOVIE SEARCH", CENTER, 1);
        tableMenu.addCell("1-> Search Movie");
        tableMenu.addCell("2-> Show Up Coming Movie");
        tableMenu.addCell("3-> Show Popular Movies");
        tableMenu.addCell("4-> List Movie Category");
        tableMenu.addCell("0->Exit");

        System.out.println(tableMenu.render());
        System.out.print("[-] Choose an option: ");
    }

    private static String safe(String s) {
        return s != null ? s : "N/A";
    }
}