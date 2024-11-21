package com.topico1.appedua;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Transfer extends DialogFragment {

    private String accountId;
    private String userId;
    private String accessToken;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfer, container, false);

        // Obtener el account_id desde los argumentos
        if (getArguments() != null) {
            accountId = getArguments().getString("account_id");
        }

        // Recuperar datos desde SharedPreferences
        SharedPreferences preferences = requireActivity().getSharedPreferences("UserSession", requireActivity().MODE_PRIVATE);
        userId = preferences.getString("user_id", "");
        accessToken = preferences.getString("access_token", "");

        // Configurar el campo de remitente con el account_id
        EditText remitenteCuenta = view.findViewById(R.id.remitenteCuenta);
        remitenteCuenta.setText(accountId);

        // Configurar los botones (Cancelar y Transferir)
        view.findViewById(R.id.cancelarButton).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.transferirButton).setOnClickListener(v -> {
            // Aquí puedes agregar la lógica para realizar la transferencia
            // usando accountId, userId y accessToken
            performTransfer();
        });

        return view;
    }

    private void performTransfer() {
        // Obtener los campos del formulario
        EditText destinatarioCuentaField = requireView().findViewById(R.id.destinatarioCuenta);
        EditText montoTransferenciaField = requireView().findViewById(R.id.montoTransferencia);

        String toAccountId = destinatarioCuentaField.getText().toString().trim();
        String amountString = montoTransferenciaField.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (toAccountId.isEmpty() || amountString.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountString);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Ingrese un monto válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear el cuerpo de la solicitud
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("from_account_id", accountId); // accountId ya se obtiene en el Fragment
            requestBody.put("to_account_id", toAccountId);
            requestBody.put("amount", amount);
            requestBody.put("description", "Transferencia desde la app móvil");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Realizar la solicitud POST a la API
        String url = "https://1-five-fawn.vercel.app/transfer/create"; // Cambia por tu URL
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    // Manejar la respuesta exitosa
                    try {
                        String message = response.getString("message");
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dismiss(); // Cierra el diálogo
                },
                error -> {
                    // Manejar errores
                    if (error.networkResponse != null) {
                        String errorMessage = new String(error.networkResponse.data);
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken); // Autenticación
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Agregar la solicitud a la cola de Volley
        Volley.newRequestQueue(requireContext()).add(request);
    }

}