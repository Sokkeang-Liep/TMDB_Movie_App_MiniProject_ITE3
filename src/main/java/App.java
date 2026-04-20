import model.Genre;
import model.Movie;
import model.MovieResponse;
import service.MovieService;
import service.MovieServiceImpl;
import utils.TableRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {

    private static final MovieService movieService = new MovieServiceImpl();
    private static final Scanner scanner = new Scanner(System.in);

    private static String currentQuery = "";
    private static int currentPage = 1;
    private static int totalPages = 1;
    private static int totalResults = 0;
    private static MovieResponse currentResponse = null;
    private static String currentMode = "";
    private static String currentGenreId = "";

    public static void main(String[] args) {
        mainMenu();
    }

    private static void mainMenu() {

        while (true) {

            TableRenderer.tableMenu();

            String op = scanner.nextLine().trim().toLowerCase();

            switch (op) {

                // SEARCH MOVIE
                case "1" -> {
                    currentMode = "search";

                    System.out.print("[+] Please Enter movie title: ");
                    String query = scanner.nextLine().trim();

                    if (query.isEmpty()) continue;

                    currentQuery = query;
                    currentPage = 1;

                    fetchAndDisplay();
                    paginationLoop();
                }

                // POPULAR MOVIES
                case "2" -> {
                    currentMode = "popular";
                    currentQuery = "Popular Movies";
                    currentPage = 1;

                    fetchAndDisplay();
                    paginationLoop();
                }

                //SEARCH BY GENRE (Category)
                case "3" -> {
                    showGenres();

                    System.out.print("Enter genre ID: ");
                    currentGenreId = scanner.nextLine().trim();

                    currentMode = "genre";
                    currentQuery = "Genre: " + currentGenreId;
                    currentPage = 1;

                    fetchAndDisplay();
                    paginationLoop();
                }

                case "e" -> {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }

                default -> System.out.println("[!] Invalid option.");
            }
        }
    }

    // =====================================================
    // FETCH DATA
    // =====================================================
    private static void fetchAndDisplay() {

        System.out.println("\nLoading...");

        currentResponse = switch (currentMode) {

            case "search" ->
                    movieService.searchMovies(currentQuery, currentPage);

            case "popular" ->
                    movieService.getPopularMovies(currentPage);

            case "genre" ->
                    movieService.getMoviesByGenre(currentPage, currentGenreId);

            default ->
                    movieService.getPopularMovies(currentPage);
        };

        totalPages =
                currentResponse.getTotalPages() != null
                        ? currentResponse.getTotalPages()
                        : 1;

        totalResults =
                currentResponse.getTotalResults() != null
                        ? currentResponse.getTotalResults()
                        : 0;

        List<String> trailers = fetchTrailers(currentResponse);

        TableRenderer.displayMovieTableWithTrailers(
                currentResponse,
                currentQuery,
                trailers
        );

        System.out.printf(
                "Page %d / %d | Total Results: %d%n",
                currentPage,
                totalPages,
                totalResults
        );
    }

    // PAGINATION
    private static void paginationLoop() {

        while (true) {

            System.out.println("""
                    
[n] Next Page
[p] Previous Page
[g] Go To Page
[md] Movie Detail
[b] Back Menu
[e] Exit
""");

            System.out.print("Please Choose Pagination: ");
            String op = scanner.nextLine().trim().toLowerCase();

            switch (op) {

                case "n" -> {
                    if (currentPage < totalPages) {
                        currentPage++;
                        fetchAndDisplay();
                    } else {
                        System.out.println("[!] This is Last page.");
                    }
                }

                case "p" -> {
                    if (currentPage > 1) {
                        currentPage--;
                        fetchAndDisplay();
                    } else {
                        System.out.println("[!] This is the First page.");
                    }
                }

                case "g" -> {
                    System.out.print("Please Enter page: ");
                    try {
                        int page = Integer.parseInt(scanner.nextLine());

                        if (page >= 1 && page <= totalPages) {
                            currentPage = page;
                            fetchAndDisplay();
                        } else {
                            System.out.println("[!] Invalid page.");
                        }

                    } catch (Exception e) {
                        System.out.println("[!] Invalid input.");
                    }
                }

                case "md" -> {
                    System.out.print("Enter movie ID: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine());
                        showMovieDetail(id);
                    } catch (Exception e) {
                        System.out.println("[!] Invalid ID.");
                    }
                }

                case "b" -> {
                    return;
                }

                case "e" -> {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }

                default -> System.out.println("[!] Invalid option.");
            }
        }
    }

    // MOVIE DETAIL
    private static void showMovieDetail(int movieId) {

        try {
            Movie movie = movieService.getMovieDetail(movieId);
            String trailer = movieService.getTrailerUrl(movieId);

            TableRenderer.displayMovieDetail(movie, trailer);

        } catch (Exception e) {
            System.out.println("[!] Cannot load movie detail.");
        }
    }

    // TRAILERS
    private static List<String> fetchTrailers(MovieResponse response) {

        List<String> list = new ArrayList<>();

        if (response != null && response.getResults() != null) {

            for (Movie movie : response.getResults()) {

                list.add(
                        movie.getId() != null
                                ? movieService.getTrailerUrl(movie.getId())
                                : null
                );
            }
        }

        return list;
    }

    // SHOW GENRES
    private static void showGenres() {

        try {
            List<Genre> genres = movieService.getGenres();

            System.out.println("\n===== GENRES =====");

            for (Genre g : genres) {
                System.out.println(g.getId() + " -> " + g.getName());
            }

            System.out.println("==================");

        } catch (Exception e) {
            System.out.println("[!] Cannot load genres.");
        }
    }
}