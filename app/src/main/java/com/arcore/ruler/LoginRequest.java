package com.arcore.ruler;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LoginRequest extends StringRequest {
    final static private String URL = "http://13.125.224.69/Login.php";
    private Map<String, String> parameters;

    public LoginRequest(String userID, String userPassword, Response.Listener<String> listener, Response.ErrorListener errlistner){
        super(Method.POST,URL,listener,errlistner);

        parameters = new HashMap<>();

        parameters.put("userid", userID);
        parameters.put("passwd", userPassword);

    }
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters;
    }

}
