package com.example.khangnt.mobifonefeedback.feedbacks;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.example.khangnt.mobifonefeedback.R;
import com.example.khangnt.mobifonefeedback.feedbacks.model.Feedback;
import com.example.khangnt.mobifonefeedback.feedbacks.model.FeedbackResponse;
import com.example.khangnt.mobifonefeedback.helper.AppConfig;
import com.example.khangnt.mobifonefeedback.helper.FeedbackProvider;
import com.example.khangnt.mobifonefeedback.helper.SessionManager;
import com.example.khangnt.mobifonefeedback.loginandregister.LoginActivity;
import com.example.khangnt.mobifonefeedback.rest.ApiClient;
import com.example.khangnt.mobifonefeedback.rest.ApiInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackListActivity extends AppCompatActivity implements
        FeedbackAdapter.OnItemClickListener {

    private int LIMIT = 20;
    private static String TAG = "xxxx";//FeedbackListActivity.class.getSimpleName();
    private Context mContext = null;
    private Handler handler;
    private RecyclerView mRecyclerView;
    private FloatingActionButton btnCreateFb;
    private TextView tvEmptyView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressDialog progressDialog;
    private FeedbackAdapter mFeedbackAdapter;
    private List<Feedback> feedbackList = new ArrayList<>();
    private int totalFeedbackOfUser = 0;
    private SessionManager sessionManager;
    private Toolbar mToolbar;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isFirstLaunch;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "isFirstLaunch";
    private static final String KEY_IS_FIRST_LAUNCH = "isFirstLaunch";
    private final static String API_KEY = "992cc11cebdb045df5b492a089271336";
    private int current_page;
    private int total_pages;
    private int total_results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_list);
        mContext = this;
        pref = mContext.getSharedPreferences(PREF_NAME, 0);
        editor = pref.edit();
        isFirstLaunch = pref.getBoolean(KEY_IS_FIRST_LAUNCH, true);

        if (isFirstLaunch) {
            editor.putBoolean(KEY_IS_FIRST_LAUNCH, false);
            editor.commit();
            // download du lieu tu server ve va luu vao trong device database
        }

        handler = new Handler();
        sessionManager = new SessionManager(this);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.feedback_list_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        btnCreateFb = (FloatingActionButton) findViewById(R.id.btnCreateFb);
        tvEmptyView = (TextView) findViewById(R.id.txt_Empty);
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        showDialog();
        final ApiInterface apiService =
                ApiClient.getClient(ApiClient.BASE_URL).create(ApiInterface.class);

        Call<FeedbackResponse> call = apiService.getTopRatedMovies(1, API_KEY);
        call.enqueue(new Callback<FeedbackResponse>() {
            @Override
            public void onResponse(Call<FeedbackResponse> call, Response<FeedbackResponse> response) {
                hideDialog();
                int statusCode = response.code();
                current_page = response.body().getPage();
                total_pages = response.body().getTotalPages();
                total_results = response.body().getTotalResults();
                feedbackList = response.body().getResults();
                mFeedbackAdapter = new FeedbackAdapter(mContext, feedbackList,
                        mRecyclerView, total_results);
                mRecyclerView.setAdapter(mFeedbackAdapter);

                mFeedbackAdapter.setOnLoadMoreListener(new FeedbackAdapter.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        if (current_page <= total_pages) {
                            Log.d(TAG, "Before loading, current page is: " + current_page);
                            Log.d(TAG, "Before loading, the number of movie is: " + feedbackList.size());
                            feedbackList.add(null);
                            mFeedbackAdapter.notifyItemInserted(feedbackList.size() - 1);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    feedbackList.remove(feedbackList.size() - 1);
                                    mFeedbackAdapter.notifyItemRemoved(feedbackList.size());
                                    current_page = current_page+1;
                                    Call<FeedbackResponse> call1 = apiService.getTopRatedMovies(current_page, API_KEY);
                                    Log.d(TAG, "Loading page: " + current_page);
                                    call1.enqueue(new Callback<FeedbackResponse>() {
                                        @Override
                                        public void onResponse(Call<FeedbackResponse> call, Response<FeedbackResponse> response) {
                                            int statusCode = response.code();
                                            current_page = response.body().getPage();
                                            total_pages = response.body().getTotalPages();
                                            total_results = response.body().getTotalResults();

                                            List<Feedback> movie_tmp = response.body().getResults();
                                            feedbackList.addAll(movie_tmp);
                                            mFeedbackAdapter.notifyDataSetChanged();
                                            mFeedbackAdapter.setLoaded();
                                            movie_tmp.clear();
                                            Log.d(TAG, "After loading, current page is: " + current_page);
                                            Log.d(TAG, "After loading, the number of movie is: " + feedbackList.size());
                                        }

                                        @Override
                                        public void onFailure(Call<FeedbackResponse> call, Throwable t) {
                                            // Log error here since request failed
                                            Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, t.toString());
                                            current_page = current_page-1;
                                            mFeedbackAdapter.setLoaded();
                                        }
                                    });
                                }
                            }, 1000);

                        } else {
                            Toast.makeText(getApplicationContext(), "Loading data completed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onFailure(Call<FeedbackResponse> call, Throwable t) {
                // Log error here since request failed
                hideDialog();
                Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, t.toString());
            }
        });

        btnCreateFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to new feedback screen
                Intent newFeedback = new Intent(mContext, NewFeedbackActivity.class);
                startActivity(newFeedback);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Download du lieu cua nhung PAKH pending va update du lieu vao database device
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(AppConfig.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    String message = intent.getStringExtra("message");
                    Toast.makeText(getApplicationContext(),
                            "Push notification: " + message, Toast.LENGTH_LONG).show();

                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            FeedbackProvider.deleteUserID(this);
            sessionManager.setLogin(false);
            sessionManager.setTimeStartLogin(0l);
            Intent loginActivity = new Intent(this, LoginActivity.class);
            startActivity(loginActivity);
            finish();
            return true;
        }

        if (id == R.id.refresh) {
            return true;
        }

        if (id == R.id.show_chart) {
            Intent t = new Intent(this, ShowChartActivity.class);
            startActivity(t);
            finish();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(AppConfig.PUSH_NOTIFICATION));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onItemClick(View view, int pos) {
        Intent intent;
        intent = new Intent(mContext, DetailFeedBackActivity.class);

        intent.putExtra("id", feedbackList.get(pos).getId());
        startActivity(intent);
    }

    private void showDialog() {
        progressDialog.setMessage("Loading data....Please wait");
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void setVisibilityEmptylayout(boolean visibility){
        if (visibility) {
            tvEmptyView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

}
