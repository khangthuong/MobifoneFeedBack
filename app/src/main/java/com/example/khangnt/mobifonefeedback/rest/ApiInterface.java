package com.example.khangnt.mobifonefeedback.rest;


import com.example.khangnt.mobifonefeedback.feedbacks.model.Feedback;
import com.example.khangnt.mobifonefeedback.feedbacks.model.FeedbackResponse;
import com.squareup.okhttp.RequestBody;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;


public interface ApiInterface {
    @GET("movie/top_rated")
    Call<FeedbackResponse> getTopRatedMovies(@Query("page") int page, @Query("api_key") String apiKey);

    @GET("movie/{id}")
    Call<Feedback> getMovieDetails(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/images")
    @Streaming
    Call<RequestBody> getImage(@Path("id") int id, @Query("api_key") String apiKey);
}
