package com.topico1.appedua;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class login extends AppCompatActivity {

    private Button btnIngresar;
    private EditText textMail, textPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnIngresar = findViewById(R.id.btnIngresar);
        textMail = findViewById(R.id.textMail);
        textPassword = findViewById(R.id.textPassword);

        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textMail.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Ingrese el correo", Toast.LENGTH_LONG).show();
                } else if (textPassword.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Ingrese la contraseña", Toast.LENGTH_LONG).show();
                } else {
                    login(textMail.getText().toString(), textPassword.getText().toString());
                }
            }
        });
    }

    private void login(String email, String password) {
        String url = "https://1-five-fawn.vercel.app/user/login"; // Asegúrate de usar la URL correcta

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            int code = jsonResponse.getInt("code");
                            String message = jsonResponse.getString("message");

                            if (code == 200) {
                                String accessToken = jsonResponse.getString("token");
                                String userId = jsonResponse.getString("userId");

                                saveToken(accessToken);

                                // Guardar solo el userId
                                SharedPreferences sharedPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPrefs.edit();
                                editor.putString("user_id", userId);
                                editor.apply();

                                // Redirigir al Index Activity
                                Intent intent = new Intent(login.this, index.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error en la respuesta del servidor", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = "Error al iniciar sesión";
                        if (error.networkResponse != null) {
                            errorMessage += ": " + error.networkResponse.statusCode;
                        }
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("user_mail", email);
                    jsonBody.put("user_password", password);
                    return jsonBody.toString().getBytes("utf-8");
                } catch (JSONException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(postRequest);
    }

    private void saveToken(String accessToken) {
        SharedPreferences sharedPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("access_token", accessToken);
        editor.apply();
    }

    public void irRegister(View view) {
        Intent miIntent = new Intent(this, register.class);
        startActivity(miIntent);
    }
}