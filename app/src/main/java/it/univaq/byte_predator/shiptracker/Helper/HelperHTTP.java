package it.univaq.byte_predator.shiptracker.Helper;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by byte-predator on 21/02/18.
 */

public class HelperHTTP {
    static private HelperHTTP instance;
    private RequestQueue queue;

    static public HelperHTTP getInstance(Context context){
        if(instance == null)
            instance = new HelperHTTP(context);
        return instance;
    }

    public HelperHTTP(Context context){
        queue = Volley.newRequestQueue(context);
    }

    public void Request(String url, String method, Response.Listener<String> listener, Response.ErrorListener errorListener){
        Request(url, new HashMap<String, String>(), method, listener, errorListener);
    }
    public void Request(String url, final HashMap<String, String> params, String method, Response.Listener<String> listener, Response.ErrorListener errorListener){
        int m = 0;
        switch (method){
            case "POST":
                m = StringRequest.Method.POST;
                break;
            case "GET":
                m = StringRequest.Method.GET;
                break;
        }

        StringRequest request = new StringRequest(m, url, listener, errorListener) {
            protected Map<String, String> getParams(){
                return params;
            }
        };
        queue.add(request);
    }

    public void RequestJSONObject(String url, String method, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener){
        RequestJSONObject( url, new JSONObject(), method, listener, errorListener);
    }
    public void RequestJSONObject(String url, HashMap<String, String> params, String method, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        RequestJSONObject( url, new JSONObject(params), method, listener, errorListener);
    }

    public void RequestJSONObject(String url, JSONObject params, String method, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener){
        int m = 0;
        switch (method){
            case "POST":
                m = StringRequest.Method.POST;
                break;
            case "GET":
                m = StringRequest.Method.GET;
                break;
        }

        JsonObjectRequest request = new JsonObjectRequest(m, url, params, listener, errorListener);
        queue.add(request);
    }

    public void RequestJSONArray(String url, String method, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener){
        int m = 0;
        switch (method){
            case "POST":
                m = StringRequest.Method.POST;
                break;
            case "GET":
                m = StringRequest.Method.GET;
                break;
        }

        JsonArrayRequest request = new JsonArrayRequest(m, url, null, listener, errorListener);
        queue.add(request);
    }
}
