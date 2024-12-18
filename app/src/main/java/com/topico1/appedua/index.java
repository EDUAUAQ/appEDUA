package com.topico1.appedua;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class index extends AppCompatActivity {

    private BroadcastReceiver accountUpdateReceiver;

    private TextView usernameTextView;
    private Button logoutButton;
    private Button btnCreateAccount;
    private SharedPreferences sharedPrefs;
    private String userId;
    private String accessToken;
    private LinearLayout cardsContainer;
    private EditText noAccountsEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        // Registra el BroadcastReceiver para actualizar cuentas al recibir el broadcast
        accountUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Cuando se detecta una actualización, se refresca la lista de cuentas
                getAccounts(userId, accessToken);
            }
        };

        // Registrar el receptor con el filtro de la acción personalizada
        LocalBroadcastManager.getInstance(this).registerReceiver(accountUpdateReceiver, new IntentFilter("com.topico1.UPDATE_ACCOUNTS"));

        // Inicialización de SharedPreferences y recuperación de datos
        sharedPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = sharedPrefs.getString("user_id", null);
        accessToken = sharedPrefs.getString("access_token", null);

        // Referencias a los elementos de la vista
        usernameTextView = findViewById(R.id.username);
        logoutButton = findViewById(R.id.btn_logout);
        btnCreateAccount = findViewById(R.id.btn_create_account);
        cardsContainer = findViewById(R.id.cards_container);
        noAccountsEditText = findViewById(R.id.no_accounts_edittext);

        // Establecer el evento del botón para crear cuenta
        btnCreateAccount.setOnClickListener(v -> {
            CreateAccount dialogFragment = new CreateAccount();
            dialogFragment.show(getSupportFragmentManager(), "CreateAccountDialog");
        });

        // Verificar sesión activa y obtener perfil y cuentas
        if (userId != null && accessToken != null) {
            getProfile(userId, accessToken);
            getAccounts(userId, accessToken); // Aquí llamamos para obtener las cuentas
        } else {
            Toast.makeText(this, "No se encontró sesión activa", Toast.LENGTH_SHORT).show();
            // Redirigir a login si no hay sesión activa
            startActivity(new Intent(index.this, login.class));
            finish();
        }

        // Manejar clic en el botón de cerrar sesión
        logoutButton.setOnClickListener(v -> {
            // Limpia los datos de la sesión en SharedPreferences
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.clear();
            editor.apply();

            // Desregistrar el BroadcastReceiver para evitar fugas de memoria
            if (accountUpdateReceiver != null) {
                LocalBroadcastManager.getInstance(index.this).unregisterReceiver(accountUpdateReceiver);
            }

            // Redirigir a la pantalla de login
            Intent intent = new Intent(index.this, login.class);
            startActivity(intent);

            // Finalizar la actividad actual para que no se pueda volver a ella
            finish();
        });
    }

    // Método para obtener el perfil del usuario
    private void getProfile(String userId, String accessToken) {
        String url = "https://1-five-fawn.vercel.app/user/profile/" + userId;

        StringRequest profileRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getInt("code") == 200) {
                            JSONObject userData = jsonResponse.getJSONObject("data");
                            String username = userData.getString("username");
                            usernameTextView.setText(username);
                        } else {
                            Toast.makeText(getApplicationContext(), "No se pudo obtener el perfil", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(getApplicationContext(), "Error al obtener perfil", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();  // Para depuración
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(profileRequest);
    }

    private void getAccounts(String userId, String accessToken) {
        String url = "https://1-five-fawn.vercel.app/account/" + userId; // Endpoint para las cuentas

        StringRequest accountsRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        Log.d("API Response", "Response: " + jsonResponse.toString()); // Log de la respuesta

                        // Verificar el código de la respuesta (si es 200)
                        int responseCode = jsonResponse.getInt("code");
                        Log.d("API Response", "Response Code: " + responseCode); // Log del código de respuesta

                        if (responseCode == 200) {
                            // Si el código es 200, revisar si hay cuentas
                            JSONArray accounts = jsonResponse.getJSONArray("data");

                            if (accounts.length() > 0) {
                                // Si hay cuentas, procesarlas
                                cardsContainer.removeAllViews(); // Limpiar antes de mostrar nuevas cuentas

                                for (int i = 0; i < accounts.length(); i++) {
                                    JSONObject account = accounts.getJSONObject(i);
                                    String accountId = account.getString("account_id");
                                    String accountType = account.getString("account_type");
                                    // Inflar la vista para cada cuenta
                                    View accountView = getLayoutInflater().inflate(R.layout.card_item, null);

                                    TextView accountNumberTextView = accountView.findViewById(R.id.tv_account_number);
                                    accountNumberTextView.setText(accountId);

                                    TextView accountTypeTextView = accountView.findViewById(R.id.tv_account_type);
                                    accountTypeTextView.setText(accountType);

                                    // Aplicar margen dinámico
                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                    );
                                    layoutParams.setMargins(0, 16, 0, 16); // margen superior e inferior
                                    accountView.setLayoutParams(layoutParams);

                                    // Establecer el evento click en el botón
                                    Button btnViewDetail = accountView.findViewById(R.id.btn_view_details); // Asegúrate de que el ID es correcto
                                    btnViewDetail.setOnClickListener(v -> {
                                        try {
                                            // Crear un Intent para iniciar la actividad AccountDetailsActivity
                                            Intent intent = new Intent(index.this, AccountDetail.class);

                                            // Pasar el accountId como extra
                                            intent.putExtra("account_id", accountId);

                                            // Iniciar la actividad
                                            startActivity(intent);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(this, "Error al abrir los detalles de la cuenta", Toast.LENGTH_SHORT).show();
                                        }
                                    });


                                    // Agregar la vista de la cuenta al contenedor
                                    cardsContainer.addView(accountView);
                                }
                            } else {
                                // Si no hay cuentas, mostrar el mensaje de la API
                                String message = jsonResponse.optString("message", "No se encontraron cuentas.");
                                showNoAccountsMessage(message);
                            }
                        } else {
                            // Si el código es diferente de 200, mostrar el mensaje de la API
                            String message = jsonResponse.optString("message", "No se pudieron obtener las cuentas");
                            showNoAccountsMessage(message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error al procesar las cuentas", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Error en la conexión a la API
                    Toast.makeText(getApplicationContext(), "Error al obtener cuentas. Verifique la conexión", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();  // Para depuración
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(accountsRequest);
    }


    // Mostrar mensaje si no hay cuentas
    private void showNoAccountsMessage(String message) {
        Log.d("No Accounts Message", "Message: " + message); // Log del mensaje que se va a mostrar
        noAccountsEditText.setVisibility(View.VISIBLE);
        noAccountsEditText.setText(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpia el receptor solo si ha sido registrado
        if (accountUpdateReceiver != null) {
            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(accountUpdateReceiver);
            } catch (IllegalArgumentException e) {
                // Ignorar el error si el receptor no estaba registrado
                e.printStackTrace();
            }
        }
    }
}