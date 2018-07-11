package com.example.khangnt.mobifonefeedback.feedbacks;

import android.app.ProgressDialog;
import android.app.VoiceInteractor;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.khangnt.mobifonefeedback.R;
import com.example.khangnt.mobifonefeedback.helper.AppConfig;
import com.example.khangnt.mobifonefeedback.helper.FeedbackProvider;
import com.example.khangnt.mobifonefeedback.http.QueryDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NewFeedbackActivity extends AppCompatActivity {

    private String TAG = NewFeedbackActivity.class.getSimpleName();
    private Context mContext;
    private EditText edtSubject, edtContent;
    private Toolbar mToolbar;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_feedback);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mContext = this;
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        edtContent = (EditText)findViewById(R.id.edt_input_content);
        edtSubject = (EditText)findViewById(R.id.edt_input_subject);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.send) {
            String uid = FeedbackProvider.getUserID(mContext);
            String subject = edtSubject.getText().toString().trim();
            String content = edtContent.getText().toString().trim();

            if (!subject.isEmpty() && !content.isEmpty()) {
                submitFeedback(subject, content, uid);
            } else {
                Toast.makeText(getApplicationContext(),
                        "Please enter the subject and content!", Toast.LENGTH_LONG)
                        .show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void submitFeedback(final String subject, final String content, final String uid) {
        progressDialog.setMessage(getResources().getString(R.string.sending));
        showDialog();
        new QueryDatabase(this)
                .setURL(AppConfig.URL_SUBMIT_NEW_FEEDBACK)
                .setMethod(Request.Method.POST)
                .readFromURL()
                .onListener(new QueryDatabase.VolleyListener() {
                    @Override
                    public Map<String, String> getParams() {
                        Log.d(TAG, "Nhan cac tham so send FB");
                        // Posting parameters to login url
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("subject", subject);
                        params.put("content", content);
                        params.put("uid", uid);
                        return params;
                    }

                    @Override
                    public void onRecieve(String data) {
                        hideDialog();
                        try {
                            JSONObject jObj = new JSONObject(data);
                            boolean error = jObj.getBoolean("error");
                            if (!error) {
                                String feedback = jObj.getString("feedback");
                                Log.d(TAG, feedback);
                                Toast.makeText(getApplicationContext(),
                                        feedback, Toast.LENGTH_LONG).show();
                            } else {
                                // Error Get the error message
                                String errorMsg = jObj.getString("error_msg");
                                Log.d(TAG, "Response: " + errorMsg);
                                Toast.makeText(getApplicationContext(),
                                        errorMsg, Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.d(TAG, "JSONException load data: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFail(VolleyError volleyError) {
                        Log.e(TAG, "VolleyError: " + volleyError.getMessage());
                        Toast.makeText(getApplicationContext(),
                                volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
