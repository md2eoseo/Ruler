package com.arcore.ruler;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class ListRequest extends StringRequest {
    final static private String URL = "http://13.125.224.69/Listload.php";
    private Map<String, String> parameters;

    public ListRequest(Response.Listener<String> listener, Response.ErrorListener errlistner){
        super(Method.POST,URL,listener,errlistner);
        parameters = new HashMap<>();

    }
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters;
    }
}
