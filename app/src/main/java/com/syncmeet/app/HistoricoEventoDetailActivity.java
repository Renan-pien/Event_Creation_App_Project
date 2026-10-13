package com.syncmeet.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.syncmeet.app.utils.SessionManager;
import com.syncmeet.app.utils.VolleyQueue;

import org.json.JSONException;
import org.json.JSONObject;

public class HistoricoEventoDetailActivity extends AppCompatActivity {

    private ImageView backArrow;
    private TextView nomeEvento, local, data, horarioInicio, horarioTermino;

    private SessionManager session;

    private int idEvento = -1;

    private static final String TAG = "HistoricoDetail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historico_evento_detail);

        session = new SessionManager(this);

        backArrow = findViewById(R.id.back_arrow);
        nomeEvento = findViewById(R.id.detalhe_nome_evento);
        local = findViewById(R.id.detalhe_local);
        data = findViewById(R.id.detalhe_data);
        horarioInicio = findViewById(R.id.detalhe_horario_inicio);
        horarioTermino = findViewById(R.id.detalhe_horario_termino);

        backArrow.setOnClickListener(v -> finish());

        // Recebe ID real do evento
        idEvento = getIntent().getIntExtra("id", -1);

        if (idEvento == -1) {
            Toast.makeText(this, "Erro: Evento inválido!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        carregarDetalhes();
    }

    private void carregarDetalhes() {

        String url = BuildConfig.BASE_URL + "get_event.php?id=" + idEvento
                + "&usuario_id=" + session.getUserId();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {

                        if (!response.getBoolean("success")) {
                            Toast.makeText(this, "Evento não encontrado!", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        JSONObject evento = response.getJSONObject("evento");

                        nomeEvento.setText(evento.getString("nome_evento"));
                        local.setText(evento.getString("local_evento"));

                        // converter YYYY-MM-DD → DD/MM/YYYY
                        String dataMysql = evento.getString("data_evento");
                        String[] partes = dataMysql.split("-");
                        data.setText(partes[2] + "/" + partes[1] + "/" + partes[0]);

                        horarioInicio.setText(evento.getString("hora_evento"));

                        // Como só existe 1 horário no banco, repito:
                        horarioTermino.setText("--:--");

                    } catch (JSONException e) {
                        Log.e(TAG, "Erro ao processar JSON", e);
                        Toast.makeText(this, "Erro ao ler dados!", Toast.LENGTH_SHORT).show();
                    }
                },

                error -> {
                    Toast.makeText(this, "Erro ao conectar ao servidor!", Toast.LENGTH_SHORT).show();
                }
        );

        VolleyQueue.add(this, request);
    }
}
