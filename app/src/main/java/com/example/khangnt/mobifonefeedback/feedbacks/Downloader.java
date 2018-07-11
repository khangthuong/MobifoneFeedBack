package com.example.khangnt.mobifonefeedback.feedbacks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.khangnt.mobifonefeedback.http.QueryDatabase;

import java.util.Map;

public class Downloader extends AsyncTask<Void ,Void , Boolean> {

    private ProgressDialog progressDialog;
    private Context mContext;

    public Downloader(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(mContext);
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... objects) {
        new QueryDatabase(mContext)
                .setURL("URL ")
                .setMethod(Request.Method.POST)
                .readFromURL()
                .onListener(new QueryDatabase.VolleyListener() {
                    @Override
                    public Map<String, String> getParams() {
                        return null;
                    }

                    @Override
                    public void onRecieve(String data) {

                    }

                    @Override
                    public void onFail(VolleyError volleyError) {

                    }
                });
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
    }
}
