package service;

import model.Genre;
import model.Movie;
import model.MovieResponse;

import java.util.List;

public interface MovieService {
    MovieResponse searchMovies(String query, int page);
    List<Genre> getGenres();
    MovieResponse getMoviesByGenre(Integer page, String genreId);
    Movie getMovieDetail(int movieId);
    String getTrailerUrl(int movieId);
    MovieResponse getPopularMovies(int page);




}
