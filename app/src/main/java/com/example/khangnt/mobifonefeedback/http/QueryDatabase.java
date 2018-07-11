package com.example.khangnt.mobifonefeedback.http;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class QueryDatabase {

    private Context mContext;
    private String mUrl;
    private int mMethod;
    private VolleyListener mVolleyListener;

    public QueryDatabase(Context context) {
        mContext = context;
    }

    public QueryDatabase setURL(String url) {
        mUrl = url;
        return this;
    }

    public QueryDatabase setMethod(int method) {
        mMethod = method;
        return this;
    }

    public QueryDatabase readFromURL() {
        StringRequest stringRequest = new StringRequest(mMethod, mUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                mVolleyListener.onRecieve(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mVolleyListener.onFail(volleyError);
            }
        }) {
            @Override
            protected Map<String, String> getParams()
            {
                return mVolleyListener.getParams();
            }
        };

        MySingleton.getInstance(mContext).addToRequestQueue(stringRequest);
        return this;
    }

    public QueryDatabase onListener(VolleyListener volleyListener) {
        mVolleyListener = volleyListener;
        return this;
    }

    public interface VolleyListener {
        Map<String, String> getParams();
        void onRecieve(String data);
        void onFail(VolleyError volleyError);
    }
}
