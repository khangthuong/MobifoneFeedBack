package com.example.khangnt.mobifonefeedback.feedbacks;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import com.example.khangnt.mobifonefeedback.R;
import com.example.khangnt.mobifonefeedback.feedbacks.model.Feedback;
import com.example.khangnt.mobifonefeedback.feedbacks.model.FeedbackResponse;
import com.example.khangnt.mobifonefeedback.rest.ApiClient;
import com.example.khangnt.mobifonefeedback.rest.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailFeedBackActivity extends AppCompatActivity {

    private String TAG = DetailFeedBackActivity.class.getSimpleName();
    private Context mContext;
    private TextView txtContent, txtReply;
    private android.support.v7.widget.Toolbar mToolbar;
    private final static String API_KEY = "992cc11cebdb045df5b492a089271336";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_feed_back);
        mContext = this;
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        txtContent = (TextView)findViewById(R.id.txt_view_content);
        txtReply = (TextView)findViewById(R.id.txt_view_reply);
        txtContent.setMovementMethod(new ScrollingMovementMethod());
        txtReply.setMovementMethod(new ScrollingMovementMethod());

        Intent intent = getIntent();
        Integer id = intent.getIntExtra("id", 1);

        final ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);
        Call<Feedback> call = apiService.getMovieDetails(id, API_KEY);

        call.enqueue(new Callback<Feedback>() {
            @Override
            public void onResponse(Call<Feedback> call, Response<Feedback> response) {
                setTitle(response.body().getOriginalTitle());
                txtContent.setText(response.body().getOverview());
                txtReply.setText(response.body().getOverview());
            }

            @Override
            public void onFailure(Call<Feedback> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_SHORT).show();
            }
        });


    }
}
