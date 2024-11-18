package com.topico1.appedua;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class register extends AppCompatActivity {

    private Button btnRegistrar;
    private EditText textUsername, textMail, textPassword, textFirstName, textLastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnRegistrar = findViewById(R.id.btnRegistrar);
        textUsername = findViewById(R.id.textUsername);
        textMail = findViewById(R.id.textMail);
        textPassword = findViewById(R.id.textPassword);
        textFirstName = findViewById(R.id.textFirstName);
        textLastName = findViewById(R.id.textLastName);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textUsername.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), "Ingrese el nombre de usuario", Toast.LENGTH_LONG).show();
                else if (textMail.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Ingrese el correo", Toast.LENGTH_LONG).show();
                } else if (textPassword.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Ingrese la contraseña", Toast.LENGTH_LONG).show();
                } else if (textFirstName.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Ingrese su(s) nombre(s)", Toast.LENGTH_LONG).show();
                } else if (textLastName.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Ingrese sus apellidos", Toast.LENGTH_LONG).show();
                } else {
                    // Registrar al usuario
                    registerUser(textUsername.getText().toString(), textMail.getText().toString(),
                            textPassword.getText().toString(), textFirstName.getText().toString(), textLastName.getText().toString());
                }
            }
        });
    }

    private void registerUser(String username, String mail, String password, String firstName, String lastName) {
        String url = "https://1-five-fawn.vercel.app/user/signup"; // Asegúrate de que esta URL sea correcta

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            int code = jsonResponse.getInt("code");

                            if (code == 201) {
                                // Registro exitoso
                                // Obtener el access token y userId de la respuesta
                                String accessToken = jsonResponse.getString("token");  // Suponiendo que el backend devuelve un token
                                String userId = jsonResponse.getString("userId"); // O el ID de usuario si lo devuelve

                                // Guardar el token y el userId en SharedPreferences
                                SharedPreferences sharedPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPrefs.edit();
                                editor.putString("access_token", accessToken);  // Guardamos el token
                                editor.putString("user_id", userId);            // Guardamos el userId
                                editor.apply();

                                Toast.makeText(getApplicationContext(), "Registro exitoso", Toast.LENGTH_LONG).show();

                                // Redirigir al Index Activity
                                Intent intent = new Intent(register.this, index.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Mostrar mensaje de error si no fue exitoso
                                String message = jsonResponse.getString("message");
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
                        // Error al realizar la petición
                        Toast.makeText(getApplicationContext(), "Error en el registro", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    // Crear el objeto JSON con los datos
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("user_name", username); // Cambié el nombre de la clave a 'user_name'
                    jsonBody.put("user_mail", mail); // Cambié el nombre de la clave a 'user_mail'
                    jsonBody.put("user_password", password); // Cambié el nombre de la clave a 'user_password'
                    jsonBody.put("first_name", firstName);
                    jsonBody.put("last_name", lastName);

                    // Convertir el objeto JSON a bytes con la codificación UTF-8
                    return jsonBody.toString().getBytes("UTF-8");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8"; // Especificar tipo de contenido
            }
        };

        // Crear la cola de peticiones Volley y agregar la petición
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(postRequest);
    }
}
