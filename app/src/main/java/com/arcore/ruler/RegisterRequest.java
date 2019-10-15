package com.arcore.ruler;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {
    final static private String URL = "http://13.125.224.69/Register.php";
    private Map<String, String> parameters;

    public RegisterRequest(String userID, String userPassword, String userName, String userMail, Response.Listener<String> listener, Response.ErrorListener errlistener){
        super(Method.POST, URL,listener,errlistener);
        parameters = new HashMap<>();
        parameters.put("userid", userID);
        parameters.put("passwd", userPassword);
        parameters.put("name", userName);
        parameters.put("email", userMail);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters;
    }
}
