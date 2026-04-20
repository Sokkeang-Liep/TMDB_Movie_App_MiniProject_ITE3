package service;

import model.*;
import tools.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class MovieServiceImpl implements MovieService {

    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJlYmJkZTdjYTQyYWQyMGMzOTg4NTUxYjRmNjJjM2E0OCIsIm5iZiI6MTc3NjAwNzAzMS4yODE5OTk4LCJzdWIiOiI2OWRiYjc3NzJjYzVkMWZiOGQzNTg0ZmYiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.4MF9JwA9X4vwHtzfqovv7WnqiCXuU72LWOIwzW_wfUc";

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final ObjectMapper mapper = new ObjectMapper();

    // HTTP Request Builder
    private HttpRequest buildRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    // Generic API Caller
    private <T> T getFromApi(String url, Class<T> clazz) {
        HttpRequest request = buildRequest(url);

        try {
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return mapper.readValue(response.body(), clazz);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("API Error: " + e.getMessage());
        }
    }

    private MovieResponse fetchMovieList(String url) {
        return getFromApi(url, MovieResponse.class);
    }

    // Attach Trailer to Movies
    private void attachTrailers(MovieResponse movieResponse) {
        if (movieResponse == null || movieResponse.getResults() == null) return;

        for (Movie movie : movieResponse.getResults()) {
            String url = String.format("%s/movie/%d/videos", BASE_URL, movie.getId());

            TrailerResponse trailerData = getFromApi(url, TrailerResponse.class);

            if (trailerData.getResults() != null) {
                for (Trailer t : trailerData.getResults()) {
                    if ("Trailer".equalsIgnoreCase(t.getType())) {
                        movie.setTailer(t); // your model method
                        break;
                    }
                }
            }
        }
    }

    // 1. Search Movie
    @Override
    public MovieResponse searchMovies(String query, int page) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String url = String.format(
                "%s/search/movie?query=%s&page=%d",
                BASE_URL,
                encodedQuery,
                page
        );

        MovieResponse response = fetchMovieList(url);
        attachTrailers(response);
        return response;
    }

    // 2. Popular Movies
    @Override
    public MovieResponse getPopularMovies(int page) {
        String url = String.format(
                "%s/movie/popular?page=%d",
                BASE_URL,
                page
        );

        MovieResponse response = fetchMovieList(url);
        attachTrailers(response);
        return response;
    }

    // 3. Genres
    @Override
    public List<Genre> getGenres() {
        String url = String.format("%s/genre/movie/list", BASE_URL);
        GenreResponse response = getFromApi(url, GenreResponse.class);
        return response.getGenres();
    }

    // 3. Search By Genre
    @Override
    public MovieResponse getMoviesByGenre(Integer page, String genreId) {
        String encodedGenre = URLEncoder.encode(genreId, StandardCharsets.UTF_8);

        String url = String.format(
                "%s/discover/movie?with_genres=%s&page=%d",
                BASE_URL,
                encodedGenre,
                page
        );

        MovieResponse response = getFromApi(url, MovieResponse.class);
        attachTrailers(response);
        return response;
    }

    @Override
    public MovieResponse getUpcomingMovies(int page) {

        String url = String.format(
                "%s/movie/upcoming?page=%d",
                BASE_URL,
                page
        );

        MovieResponse movieResponse = fetchMovieList(url);
        attachTrailers(movieResponse);

        return movieResponse;
    }

    // Movie Detail
    @Override
    public Movie getMovieDetail(int movieId) {
        String url = String.format("%s/movie/%d", BASE_URL, movieId);
        return getFromApi(url, Movie.class);
    }

    // Trailer URL
    @Override
    public String getTrailerUrl(int movieId) {
        String url = String.format("%s/movie/%d/videos", BASE_URL, movieId);

        VideoResponse response = getFromApi(url, VideoResponse.class);

        if (response.getResults() == null || response.getResults().isEmpty()) {
            return null;
        }

        return response.getResults().stream()
                .filter(v ->
                        "YouTube".equalsIgnoreCase(v.getSite()) &&
                                "Trailer".equalsIgnoreCase(v.getType()))
                .map(v -> "https://www.youtube.com/watch?v=" + v.getKey())
                .findFirst()
                .orElse(null);
    }
}