package com.topico1.appedua;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AccountDetail extends AppCompatActivity {

    private String userId;
    private String accountId;
    private String accessToken;
    private TextView tvAccountId, tvAccountId2, tvAccountUser, tvAccountType, tvAccountCreation, tvBalance;
    private Button btnTransfer, btnHistory;
    private Handler handler;
    private Runnable runnable;
    private static final int UPDATE_INTERVAL = 2000; // 5 segundos

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);

        // Referencias a los elementos de la interfaz
        tvAccountId = findViewById(R.id.tv_accountid);
        tvAccountId2 = findViewById(R.id.tv_accountid2);
        tvAccountUser = findViewById(R.id.tv_account_user);
        tvAccountType = findViewById(R.id.tv_account_type);
        tvAccountCreation = findViewById(R.id.tv_account_creation);
        tvBalance = findViewById(R.id.tv_balance);
        btnTransfer = findViewById(R.id.btn_transfer);
        btnHistory = findViewById(R.id.btn_history);

        // Recuperar datos desde SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = preferences.getString("user_id", "");
        accessToken = preferences.getString("access_token", "");

        // Recuperar el accountId desde el intent
        accountId = getIntent().getStringExtra("account_id");
        Log.d("AccountDetails", "Account ID recibido: " + accountId);

        if (!accountId.isEmpty() && !accessToken.isEmpty()) {
            fetchAccountDetails(accountId, accessToken); // Consultar detalles de la cuenta
            fetchUserName(userId, accessToken);          // Consultar el nombre del usuario
        } else {
            Toast.makeText(this, "Datos incompletos para obtener detalles", Toast.LENGTH_SHORT).show();
        }

        // Configurar el botón de retroceso
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Configurar el botón de transferir
        btnTransfer.setOnClickListener(v -> {
            showTransferDialog(accountId);

            // Actualizar los detalles después de la transferencia
            fetchAccountDetails(accountId, accessToken);
        });

        //Boton para mostrar el historial
        btnHistory.setOnClickListener(v -> {
            // Crear el fragmento History
            History historyFragment = new History();

            // Crear un Bundle para pasar datos
            Bundle args = new Bundle();
            args.putString("account_id", accountId); // Pasar el account_id

            // Establecer los argumentos al fragmento
            historyFragment.setArguments(args);

            // Mostrar el fragmento en el DialogFragment
            historyFragment.show(getSupportFragmentManager(), "HistoryDialog");
        });


        // Configurar el Handler para actualizaciones periódicas
        handler = new Handler();
        runnable = () -> {
            fetchAccountDetails(accountId, accessToken); // Actualizar detalles
            handler.postDelayed(runnable, UPDATE_INTERVAL); // Repetir después de 5 segundos
        };
        handler.post(runnable); // Iniciar las actualizaciones periódicas
    }

    private void fetchAccountDetails(String accountId, String accessToken) {
        String url = "https://1-five-fawn.vercel.app/account/details/" + accountId;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getInt("code") == 200) {
                            JSONObject data = jsonResponse.getJSONObject("data");
                            tvAccountId.setText(data.getString("account_id"));
                            tvAccountId2.setText(data.getString("account_id"));
                            tvAccountType.setText(data.getString("account_type"));
                            tvAccountCreation.setText(data.getString("created_at"));
                            tvBalance.setText("$ " + String.format("%.2f", data.getDouble("balance")));
                        } else {
                            Toast.makeText(this, "Error al obtener los detalles de la cuenta", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void showTransferDialog(String accountId) {
        Transfer transferDialog = new Transfer();
        Bundle args = new Bundle();
        args.putString("account_id", accountId);
        transferDialog.setArguments(args);

        transferDialog.show(getSupportFragmentManager(), "TransferDialog");
    }

    private void fetchUserName(String userId, String accessToken) {
        String url = "https://1-five-fawn.vercel.app/user/profile/" + userId;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getInt("code") == 200) {
                            JSONObject data = jsonResponse.getJSONObject("data");
                            String fullName = data.getString("first_name") + " " + data.getString("last_name");
                            tvAccountUser.setText(fullName);
                        } else {
                            Toast.makeText(this, "Error al obtener el nombre del usuario", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(runnable); // Detener actualizaciones
        }
    }
}