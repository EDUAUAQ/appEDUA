package com.topico1.appedua;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class CreateAccount extends DialogFragment {

    private Spinner spinnerAccountType;
    private EditText etBalance;
    private Button btnCreateAccount;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Usamos LayoutInflater para inflar el layout del diálogo
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_create_account, null);

        // Inicializamos los componentes
        spinnerAccountType = view.findViewById(R.id.spinnerAccountType);
        etBalance = view.findViewById(R.id.etBalance);
        btnCreateAccount = view.findViewById(R.id.btnCreateAccount);

        // Configurar el Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.account_types, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccountType.setPopupBackgroundDrawable(getResources().getDrawable(R.drawable.spinner_dropdown_background));
        spinnerAccountType.setAdapter(adapter);

        // Crear un dialogo de alerta
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        // Acción del botón
        btnCreateAccount.setOnClickListener(v -> {
            String accountType = spinnerAccountType.getSelectedItem().toString();
            String balanceString = etBalance.getText().toString().trim();

            if (accountType.equals("Elija un tipo de cuenta...") || balanceString.isEmpty()) {
                Toast.makeText(getActivity(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                int balance = Integer.parseInt(balanceString);

                // Recuperar el userId y accessToken desde SharedPreferences
                String userId = getUserIdFromSession();
                String accessToken = getAccessTokenFromSession();

                if (userId == null || accessToken == null) {
                    Toast.makeText(getActivity(), "No se pudo recuperar los datos de la sesión", Toast.LENGTH_SHORT).show();
                } else {
                    // Si userId y accessToken están bien, procedemos a crear la cuenta
                    createAccount(userId, accountType, balance, accessToken);
                }
            }
        });

        return builder.create();
    }

    private String getUserIdFromSession() {
        // Recuperar el user_id desde SharedPreferences
        return getActivity().getSharedPreferences("UserSession", getActivity().MODE_PRIVATE)
                .getString("user_id", null);
    }

    private String getAccessTokenFromSession() {
        // Recuperar el access_token desde SharedPreferences
        return getActivity().getSharedPreferences("UserSession", getActivity().MODE_PRIVATE)
                .getString("access_token", null);
    }

    private void createAccount(String userId, String accountType, int balance, String accessToken) {
        String url = "https://1-five-fawn.vercel.app/account/create"; // Cambia por tu URL de API

        // Crear la solicitud POST
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        int code = jsonResponse.getInt("code");
                        String message = jsonResponse.getString("message");

                        if (code == 201) {
                            Toast.makeText(getActivity(), "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show();

                            // Cerrar el DialogFragment
                            dismiss();

                            // Enviar Broadcast para actualizar las cuentas en el index
                            Intent intent = new Intent("com.topico1.UPDATE_ACCOUNTS");
                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                        } else {
                            Toast.makeText(getActivity(), "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMessage = "Error al crear cuenta";
                    if (error.networkResponse != null) {
                        errorMessage += ": " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }

            @Override
            public byte[] getBody() {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("user_id", userId);
                    jsonBody.put("account_type", accountType);
                    jsonBody.put("balance", balance);
                    return jsonBody.toString().getBytes("utf-8");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        // Enviar la solicitud
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(postRequest);
    }
}