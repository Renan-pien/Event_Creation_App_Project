package com.syncmeet.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.syncmeet.app.utils.VolleyQueue;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CriarContaActivity extends AppCompatActivity {

    // Componentes do layout
    private TextInputLayout emailLayout, usuarioLayout, senhaLayout, confirmarSenhaLayout;
    private TextInputEditText emailEditText, usuarioEditText, senhaEditText, confirmarSenhaEditText;
    private MaterialButton registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.criar_conta);

        // Botão de voltar (Toolbar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Vincula os componentes
        emailLayout = findViewById(R.id.email_layout_create);
        usuarioLayout = findViewById(R.id.usuario_layout_create);
        senhaLayout = findViewById(R.id.password_layout_create);
        confirmarSenhaLayout = findViewById(R.id.confirm_password_layout_create);

        emailEditText = findViewById(R.id.email_edittext_create);
        usuarioEditText = findViewById(R.id.usuario_edittext_create);
        senhaEditText = findViewById(R.id.password_edittext_create);
        confirmarSenhaEditText = findViewById(R.id.confirm_password_edittext_create);

        registerButton = findViewById(R.id.register_button);

        // Clique no botão CADASTRAR
        registerButton.setOnClickListener(v -> validarCampos());
    }

    private void validarCampos() {
        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
        String usuario = Objects.requireNonNull(usuarioEditText.getText()).toString().trim();
        String senha = Objects.requireNonNull(senhaEditText.getText()).toString().trim();
        String confirmarSenha = Objects.requireNonNull(confirmarSenhaEditText.getText()).toString().trim();

        boolean isValid = true;

        // Validação do e-mail
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Este campo é obrigatório");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        // Validação do usuário
        if (TextUtils.isEmpty(usuario)) {
            usuarioLayout.setError("Este campo é obrigatório");
            isValid = false;
        } else {
            usuarioLayout.setError(null);
        }

        // Validação da senha
        if (TextUtils.isEmpty(senha)) {
            senhaLayout.setError("Este campo é obrigatório");
            isValid = false;
        } else {
            senhaLayout.setError(null);
        }

        // Validação da confirmação
        if (TextUtils.isEmpty(confirmarSenha)) {
            confirmarSenhaLayout.setError("Este campo é obrigatório");
            isValid = false;
        } else if (!senha.equals(confirmarSenha)) {
            confirmarSenhaLayout.setError("As senhas não coincidem");
            isValid = false;
        } else {
            confirmarSenhaLayout.setError(null);
        }

        // Se válido → envia para o backend
        if (isValid) {
            cadastrarUsuario(email, usuario, senha);
        }
    }

    private void cadastrarUsuario(String email, String usuario, String senha) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                BuildConfig.BASE_URL + "register.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            if (json.optBoolean("success")) {
                                Toast.makeText(CriarContaActivity.this,
                                        "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show();
                                finish(); // Fecha tela após sucesso
                            } else {
                                Toast.makeText(CriarContaActivity.this,
                                        json.optString("message", "Não foi possível concluir o cadastro."),
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(CriarContaActivity.this,
                                    "Resposta inválida do servidor.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CriarContaActivity.this,
                                "Falha de conexão. Tente novamente.", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("usuario", usuario);
                params.put("senha", senha);
                return params;
            }
        };

        // Envia requisição
        VolleyQueue.add(this, request);
    }

    // Botão voltar (Toolbar ← )
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
