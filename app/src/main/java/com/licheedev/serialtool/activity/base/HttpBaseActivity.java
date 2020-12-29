package com.licheedev.serialtool.activity.base;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.licheedev.serialtool.R;
import com.licheedev.serialtool.activity.ApiMethods;
import com.licheedev.serialtool.activity.OnSuccessListener;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import co.sspp.library.SweetAlertDialog;

public class HttpBaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    private SweetAlertDialog mDialog;
    private SweetAlertDialog mSuccessDialog;
    private SweetAlertDialog mErrorDialog;
    protected OnSuccessListener onSuccessListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);


    }

    final public void makeHttpRequest(final ApiMethods apiMethod, String urlString, Map<String, String> parameters) {

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, urlString, new JSONObject(parameters), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                handleResponse(response, apiMethod);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleResponse(new JSONObject(), apiMethod);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Username", "api@Engineers");
                params.put("Password", "API@Engineers_123654");
                params.put("Authorization", "Basic YXBpQEVuZ2luZWVyczpBUElARW5naW5lZXJzXzEyMzY1NA==");

                return params;
            }
        };
        queue.add(jsonRequest);
    }

    final public void makeHttpRequest(final ApiMethods apiMethods, String urlString, final Map<String, String> parameters, boolean ok) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlString, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                handleResponse(response, apiMethods);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Username", "api@Engineers");
                //params.put("Password", "API@Engineers_123654");
                params.put("Content-Type", "application/json");

                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return parameters;
            }


        };


        queue.add(stringRequest);
    }

    protected void handleResponse(JSONObject response, ApiMethods method) { }

    protected void handleResponse(String response, ApiMethods method) { }

    protected void setOnSuccessListener(OnSuccessListener listener) {
        this.onSuccessListener = listener;
    }

    protected final void showActivityIndicator(String msg) {
        mDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        mDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        mDialog.setTitleText(msg);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    protected final void hideActivityIndicator() {
        if(mDialog.isShowing())
            mDialog.hide();
    }

    protected final void doError(String title, String content) {
        mErrorDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        mErrorDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        mErrorDialog.setTitleText(title);
        mErrorDialog.setContentText(content);
        mErrorDialog.setCancelable(false);
        mErrorDialog.show();
    }

    protected  final void doSuccess(String title, String content) {
        mSuccessDialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
        mSuccessDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        mSuccessDialog.setTitleText(title);
        mSuccessDialog.setContentText(content);
        mSuccessDialog.setCancelable(false);
        mSuccessDialog.show();

        mSuccessDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

                if(onSuccessListener != null) {
                    //onSuccessListener.onRegisterSuccess();
                }
            }
        });


    }
}
