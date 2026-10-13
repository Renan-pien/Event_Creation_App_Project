package com.syncmeet.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.button.MaterialButton;
import com.syncmeet.app.utils.SessionManager;
import com.syncmeet.app.utils.VolleyQueue;

import org.json.JSONArray;
import org.json.JSONObject;

public class HistoricoEventoActivity extends AppCompatActivity {

    private ImageView backArrow;
    private LinearLayout container;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historico_evento);

        session = new SessionManager(this);

        backArrow = findViewById(R.id.back_arrow);
        container = findViewById(R.id.container_historico);

        backArrow.setOnClickListener(v -> finish());

        carregarEventos();
    }

    private void carregarEventos() {

        String url = BuildConfig.BASE_URL + "list_events.php?usuario_id=" + session.getUserId();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (!response.getBoolean("success")) {
                            Toast.makeText(this, "Erro ao carregar eventos", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray eventos = response.getJSONArray("eventos");
                        container.removeAllViews();

                        if (eventos.length() == 0) {
                            Toast.makeText(this, "Nenhum evento encontrado!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        for (int i = 0; i < eventos.length(); i++) {
                            JSONObject ev = eventos.getJSONObject(i);

                            int id = ev.getInt("id");
                            String nome = ev.getString("nome_evento");
                            String data = ev.getString("data_evento");
                            String hora = ev.getString("hora_evento");

                            adicionarBotaoEvento(id, nome, data, hora);
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao ler dados", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Falha ao carregar eventos", Toast.LENGTH_LONG).show()
        );

        VolleyQueue.add(this, request);
    }

    private void adicionarBotaoEvento(int id, String nome, String data, String hora) {

        MaterialButton botao = new MaterialButton(
                this,
                null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle
        );

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, 0, 0, 25);
        botao.setLayoutParams(lp);

        botao.setText(nome + " - " + data + " " + hora);
        botao.setAllCaps(false);
        botao.setCornerRadius(18);
        botao.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        botao.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.purple_syncmeet));

        botao.setOnClickListener(v -> {
            Intent intent = new Intent(HistoricoEventoActivity.this, HistoricoEventoDetailActivity.class);
            intent.putExtra("id", id);  // Só o ID, o resto será buscado no servidor
            startActivity(intent);
        });

        container.addView(botao);
    }
}
