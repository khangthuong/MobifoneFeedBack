package com.example.khangnt.mobifonefeedback.loginandregister;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.khangnt.mobifonefeedback.Manifest;
import com.example.khangnt.mobifonefeedback.R;
import com.example.khangnt.mobifonefeedback.feedbacks.FeedbackListActivity;
import com.example.khangnt.mobifonefeedback.helper.AndroidPermissions;
import com.example.khangnt.mobifonefeedback.helper.AppConfig;
import com.example.khangnt.mobifonefeedback.helper.FeedbackProvider;
import com.example.khangnt.mobifonefeedback.helper.SessionManager;
import com.example.khangnt.mobifonefeedback.http.QueryDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private Context mContext;
    private EditText edtInputEmail, edtInputPass;
    private TextView btnLogin;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;
    private Long timeStartlogin;
    private AndroidPermissions mPermissions;
    private static final int ANDROID_PERMISSION_WRITE_EXTERNAL_STORAGE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;
        mPermissions = new AndroidPermissions(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        edtInputEmail = (EditText)findViewById(R.id.txt_input_user_email);
        edtInputPass = (EditText)findViewById(R.id.txt_input_pass);
        btnLogin = (TextView)findViewById(R.id.btn_login);
        sessionManager = new SessionManager(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sessionManager.setLogin(true);
                timeStartlogin = Calendar.getInstance().getTimeInMillis();
                sessionManager.setTimeStartLogin(timeStartlogin);
                OpenListFeedbackActivity();
                // checking login successfully or not
                String email = edtInputEmail.getText().toString().trim();
                String pwd = edtInputPass.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !pwd.isEmpty()) {
                    // login user
                    checkLogin(email, pwd);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });


        // Check if user is already logged in or not
        if (sessionManager != null && sessionManager.isLoggedIn() &&
                Calendar.getInstance().getTimeInMillis() - sessionManager.getTimeStartLogin() < 3600000) {
            OpenListFeedbackActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.i(getClass().getSimpleName(), "onRequestPermissionsResult");

        if (requestCode == ANDROID_PERMISSION_WRITE_EXTERNAL_STORAGE &&
                mPermissions.areAllRequiredPermissionsGranted(permissions, grantResults)) {
            OpenListFeedbackActivity();
        } else {
            Toast.makeText(this, "Please grant permissions to be able to select files", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkLogin(final String email, final String pwd) {
        progressDialog.setMessage(getResources().getString(R.string.Logging_in));
        showDialog();
        new QueryDatabase(this)
                .setURL(AppConfig.URL_LOGIN)
                .setMethod(Request.Method.POST)
                .readFromURL()
                .onListener(new QueryDatabase.VolleyListener() {
                    @Override
                    public Map<String, String> getParams() {
                        // Posting parameters to login url
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("email", email);
                        params.put("pwd", pwd);

                        return params;
                    }

                    @Override
                    public void onRecieve(String data) {
                        Log.d(TAG, "Login Response: " + data.toString());
                        hideDialog();

                        try {
                            JSONObject jObj = new JSONObject(data);
                            boolean error = jObj.getBoolean("error");

                            // Check for error node in json
                            if (!error) {
                                // user successfully logged in
                                // Create login session
                                sessionManager.setLogin(true);
                                timeStartlogin = Calendar.getInstance().getTimeInMillis();
                                sessionManager.setTimeStartLogin(timeStartlogin);

                                // Now store the user in SQLite
                                String uid = jObj.getString("uid");
                                JSONObject user = jObj.getJSONObject("user");
                                String email = user.getString("email");
                                String permission = user.getString("permission");
                                String deviceId = user.getString("deviceID");

                                // Inserting row in users table
                                FeedbackProvider.storeNewUserToSQLite(mContext, uid, email, permission, deviceId);

                                Toast.makeText(getApplicationContext(),
                                        "Login successfully", Toast.LENGTH_LONG).show();
                                // Launch main activity
                               OpenListFeedbackActivity();
                            } else {
                                // Error in login. Get the error message
                                String errorMsg = jObj.getString("error_msg");
                                Toast.makeText(getApplicationContext(),
                                        errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onFail(VolleyError volleyError) {
                        Log.e(TAG, "Login Error: " + volleyError.getMessage());
                        Toast.makeText(getApplicationContext(),
                                volleyError.getMessage(), Toast.LENGTH_LONG).show();
                        hideDialog();
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

    private void OpenListFeedbackActivity() {
        if (mPermissions.checkPermissions()) {
            Intent intent = new Intent(LoginActivity.this,
                    FeedbackListActivity.class);
            startActivity(intent);
            finish();
        } else {
            mPermissions.requestPermissions(ANDROID_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }

    }
}
