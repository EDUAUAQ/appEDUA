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

    public void irLogin(View view) {
        Intent intent = new Intent(register.this, login.class);
        startActivity(intent);
        finish(); // Finalizar la actividad actual para evitar regresar
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
                                Toast.makeText(getApplicationContext(), "Registro exitoso", Toast.LENGTH_LONG).show();

                                // Redirigir al Login Activity y pasar los datos
                                Intent intent = new Intent(register.this, login.class);
                                intent.putExtra("user_mail", mail);  // Pasar correo
                                intent.putExtra("user_password", password);  // Pasar contraseña
                                startActivity(intent);
                                finish();
                            } else {
                                // Mostrar mensaje de error si no fue exitoso
                                String message = jsonResponse.getString("message");
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error en la respuesta del servidor: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Error al realizar la petición
                        Toast.makeText(getApplicationContext(), "Error en el registro: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    // Crear el objeto JSON con los datos
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("user_name", username);
                    jsonBody.put("user_mail", mail);
                    jsonBody.put("user_password", password);
                    jsonBody.put("first_name", firstName);
                    jsonBody.put("last_name", lastName);

                    // Convertir el objeto JSON a bytes con la codificación UTF-8
                    return jsonBody.toString().getBytes("UTF-8");
                } catch (JSONException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error al crear el cuerpo de la solicitud: " + e.getMessage(), Toast.LENGTH_LONG).show();
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