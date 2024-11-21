package com.topico1.appedua;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.AuthFailureError;

import java.util.HashMap;
import java.util.Map;

public class History extends DialogFragment {

    private String accountId;
    private String userId;
    private String accessToken;
    private LinearLayout containerHistory; // Cambié a LinearLayout para agregar elementos verticalmente

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Inicializa el contenedor de transferencias
        containerHistory = view.findViewById(R.id.container_history);

        // Recuperar el accountId de los argumentos
        if (getArguments() != null) {
            accountId = getArguments().getString("account_id");
        }

        // Recuperar el userId y accessToken desde SharedPreferences
        SharedPreferences preferences = getActivity().getSharedPreferences("UserSession", getContext().MODE_PRIVATE);
        userId = preferences.getString("user_id", "");
        accessToken = preferences.getString("access_token", "");

        // Llamada a la API para obtener las transferencias
        getTransfers(accountId, accessToken);

        return view;
    }

    private void getTransfers(String accountId, String accessToken) {
        String url = "https://1-five-fawn.vercel.app/transfer/" + accountId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int statusCode = response.getInt("code");

                            if (statusCode == 200) {
                                JSONArray transfers = response.getJSONArray("data");

                                if (transfers.length() > 0) {
                                    for (int i = 0; i < transfers.length(); i++) {
                                        JSONObject transfer = transfers.getJSONObject(i);
                                        String sender = transfer.getString("from_account_id");
                                        String recipient = transfer.getString("to_account_id");
                                        String amount = transfer.getString("amount");
                                        String date = transfer.getString("transfer_date");

                                        // Crear un item para cada transferencia
                                        View transferView = createTransferView(sender, recipient, amount, date);
                                        containerHistory.addView(transferView);
                                    }
                                } else {
                                    TextView noTransfersMessage = new TextView(getContext());
                                    noTransfersMessage.setText("No hay transferencias.");
                                    noTransfersMessage.setTextSize(16);
                                    containerHistory.addView(noTransfersMessage);
                                }
                            } else if (statusCode == 404) {
                                TextView noTransfersMessage = new TextView(getContext());
                                noTransfersMessage.setText("No se encontraron transferencias para esta cuenta.");
                                noTransfersMessage.setTextSize(16);
                                containerHistory.addView(noTransfersMessage);
                            } else {
                                TextView errorMessage = new TextView(getContext());
                                errorMessage.setText("Error al obtener las transferencias.");
                                errorMessage.setTextSize(16);
                                containerHistory.addView(errorMessage);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error al cargar las transferencias", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "No hay transferencias", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        Volley.newRequestQueue(getContext()).add(request);
    }

    private View createTransferView(String sender, String recipient, String amount, String date) {
        // Inflar el item de transferencia (item_transfer.xml)
        View transferView = getLayoutInflater().inflate(R.layout.item_transfer, null);

        // Rellenar los TextViews con los datos de la transferencia
        TextView transferSender = transferView.findViewById(R.id.transfer_sender);
        TextView transferRecipient = transferView.findViewById(R.id.transfer_recipient);
        TextView transferAmount = transferView.findViewById(R.id.transfer_amount);
        TextView transferDate = transferView.findViewById(R.id.transfer_date);

        transferSender.setText(sender);
        transferRecipient.setText(recipient);
        transferAmount.setText(amount);
        transferDate.setText(date);

        return transferView;
    }
}