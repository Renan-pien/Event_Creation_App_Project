package com.syncmeet.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.navigation.NavigationView;
import com.syncmeet.app.utils.SessionManager;
import com.syncmeet.app.utils.VolleyQueue;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TelaPrincipalActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;
    private TextView navFooterSair;
    private TextView navFooterExcluirConta;
    private Button botaoCriarEvento;
    private Button botaoEditarEvento;
    private Button botaoExcluirEvento;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_principal);

        session = new SessionManager(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuIcon = findViewById(R.id.menu_icon);
        navFooterSair = findViewById(R.id.nav_footer_sair);
        navFooterExcluirConta = findViewById(R.id.nav_footer_excluir_conta);
        botaoCriarEvento = findViewById(R.id.botao_criar_evento);
        botaoEditarEvento = findViewById(R.id.botao_edit_evento);
        botaoExcluirEvento = findViewById(R.id.botao_exclui_evento);

        // --- Início da atualização do Header ---

        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_header_username);
        TextView navEmail = headerView.findViewById(R.id.nav_header_email);

        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");
        String email = intent.getStringExtra("EMAIL");

        if (username != null && !username.isEmpty()) {
            navUsername.setText(username);
        }
        if (email != null && !email.isEmpty()) {
            navEmail.setText(email);
        }

        // --- Fim da atualização do Header ---

        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_eventos_agendados) {
                Intent agendaIntent = new Intent(TelaPrincipalActivity.this, AgendaEventoActivity.class);
                startActivity(agendaIntent);
            } else if (id == R.id.nav_historico) {
                Intent historicoIntent = new Intent(TelaPrincipalActivity.this, HistoricoEventoActivity.class);
                startActivity(historicoIntent);
            }
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });

        // --- Botão de sair no Drawer ---
        navFooterSair.setOnClickListener(v -> {
            session.clearSession();
            Intent logoutIntent = new Intent(TelaPrincipalActivity.this, MainActivity.class);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutIntent);
            finish();
        });

        // --- Exclusão de conta (exigência da Google Play para apps com login) ---
        navFooterExcluirConta.setOnClickListener(v -> confirmarExclusaoDeConta());

        // --- Abrir a tela de Criar Evento ---
        botaoCriarEvento.setOnClickListener(v ->
                startActivity(new Intent(TelaPrincipalActivity.this, CriarEventoActivity.class)));

        // --- Abrir a tela de Editar Evento ---
        botaoEditarEvento.setOnClickListener(v ->
                startActivity(new Intent(TelaPrincipalActivity.this, EditarEventoActivity.class)));

        // --- Abrir a tela de Excluir Evento ---
        botaoExcluirEvento.setOnClickListener(v ->
                startActivity(new Intent(TelaPrincipalActivity.this, ExcluirEventoActivity.class)));
    }

    private void confirmarExclusaoDeConta() {

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmacao, null);

        TextView mensagem = dialogView.findViewById(R.id.dialog_message);
        mensagem.setText("Deseja realmente excluir sua conta? Todos os seus eventos " +
                "serão apagados e esta ação não pode ser desfeita.");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.dialog_button_sim).setOnClickListener(v -> {
            dialog.dismiss();
            excluirContaNoServidor();
        });

        dialogView.findViewById(R.id.dialog_button_cancelar).setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        dialog.show();
    }

    private void excluirContaNoServidor() {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                BuildConfig.BASE_URL + "delete_account.php",
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);

                        if (obj.optBoolean("success")) {
                            // Apaga a sessão local e volta ao login
                            session.clearSession();
                            Toast.makeText(this, "Conta excluída.", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(TelaPrincipalActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this,
                                    obj.optString("message", "Não foi possível excluir a conta."),
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Resposta inválida do servidor.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Falha de conexão. Tente novamente.", Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("usuario_id", String.valueOf(session.getUserId()));
                return params;
            }
        };

        VolleyQueue.add(this, request);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }
}
