package com.syncmeet.app;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.syncmeet.app.utils.LembreteScheduler;
import com.syncmeet.app.utils.SessionManager;
import com.syncmeet.app.utils.VolleyQueue;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcluirEventoActivity extends AppCompatActivity {

    private ImageView backArrow;
    private Button excluirButton;
    private LinearLayout containerEventos;

    private final List<Integer> idsEventos = new ArrayList<>();
    private final List<CheckBox> checkBoxes = new ArrayList<>();

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.excluir_evento);

        session = new SessionManager(this);

        backArrow = findViewById(R.id.back_arrow);
        excluirButton = findViewById(R.id.excluir_button);
        containerEventos = findViewById(R.id.container_eventos);

        backArrow.setOnClickListener(v -> finish());

        carregarEventosDoBanco();

        excluirButton.setOnClickListener(v -> {
            if (getEventosSelecionados().isEmpty()) {
                Toast.makeText(this, "Nenhum evento selecionado!", Toast.LENGTH_SHORT).show();
            } else {
                showConfirmacaoDialog();
            }
        });
    }

    private void carregarEventosDoBanco() {

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

                        JSONArray array = response.getJSONArray("eventos");

                        containerEventos.removeAllViews();
                        idsEventos.clear();
                        checkBoxes.clear();

                        for (int i = 0; i < array.length(); i++) {

                            JSONObject obj = array.getJSONObject(i);

                            int id = obj.getInt("id");
                            String nome = obj.getString("nome_evento");

                            idsEventos.add(id);

                            CheckBox cb = new CheckBox(this);
                            cb.setText(nome);
                            cb.setTextSize(18);
                            cb.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                            cb.setPadding(20, 25, 20, 25);

                            containerEventos.addView(cb);
                            checkBoxes.add(cb);
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao processar eventos!", Toast.LENGTH_SHORT).show();
                    }
                },

                error -> Toast.makeText(this, "Erro ao conectar ao servidor!", Toast.LENGTH_LONG).show()
        );

        VolleyQueue.add(this, request);
    }

    private List<Integer> getEventosSelecionados() {
        List<Integer> selecionados = new ArrayList<>();

        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isChecked()) {
                selecionados.add(idsEventos.get(i));
            }
        }
        return selecionados;
    }

    private void showConfirmacaoDialog() {

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmacao, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.dialog_button_sim).setOnClickListener(v -> {
            excluirEventosDoBanco();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.dialog_button_cancelar).setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        dialog.show();
    }

    private void excluirEventosDoBanco() {

        boolean algumExcluido = false;

        for (int i = checkBoxes.size() - 1; i >= 0; i--) {

            if (checkBoxes.get(i).isChecked()) {

                algumExcluido = true;
                final int index = i;
                final int idEvento = idsEventos.get(i);
                final View itemView = checkBoxes.get(i);

                // Exclui primeiro no servidor; só remove da tela (e cancela o
                // lembrete) quando o servidor confirmar a exclusão.
                excluirNoBanco(idEvento, () -> {
                    LembreteScheduler.cancelar(this, idEvento);

                    itemView.animate()
                            .translationX(700f)
                            .alpha(0f)
                            .scaleX(0.7f)
                            .scaleY(0.7f)
                            .setDuration(350)
                            .withEndAction(() -> {
                                containerEventos.removeView(itemView);
                                checkBoxes.remove(index);
                                idsEventos.remove(index);
                            })
                            .start();
                });
            }
        }

        if (algumExcluido) {
            Toast.makeText(this, "Excluindo eventos...", Toast.LENGTH_SHORT).show();
        }
    }

    private void excluirNoBanco(int idEvento, Runnable aposSucesso) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                BuildConfig.BASE_URL + "delete_event.php",
                response -> {
                    try {
                        if (new JSONObject(response).optBoolean("success")) {
                            aposSucesso.run();
                        } else {
                            Toast.makeText(this, "Falha ao excluir ID " + idEvento, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Resposta inválida do servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Falha ao excluir ID " + idEvento, Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("id", String.valueOf(idEvento));
                map.put("usuario_id", String.valueOf(session.getUserId()));
                return map;
            }
        };

        VolleyQueue.add(this, request);
    }
}
