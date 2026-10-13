package com.example.syncmeet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.syncmeet.utils.SessionManager;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private SwitchMaterial lembrarSwitch;
    private SessionManager session;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        lembrarSwitch = findViewById(R.id.remember_me_switch);
        Button loginButton = findViewById(R.id.login_button);
        TextView criarConta = findViewById(R.id.criar_nova_conta);

        session = new SessionManager(this);

        // AUTO-PREENCHIMENTO
        if (session.isRemembered()) {
            emailEditText.setText(session.getSavedLogin());
            passwordEditText.setText(session.getSavedPassword());
            lembrarSwitch.setChecked(true);
        }

        criarConta.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, CriarContaActivity.class);
            startActivity(i);
        });

        loginButton.setOnClickListener(v -> fazerLogin());
    }

    private void fazerLogin() {
        String email = emailEditText.getText().toString().trim();
        String senha = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://10.0.2.2/syncmeet/login.php");
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String dados = "email=" + URLEncoder.encode(email, "UTF-8")
                        + "&senha=" + URLEncoder.encode(senha, "UTF-8");


                try (OutputStream os = conn.getOutputStream()) {
                    os.write(dados.getBytes("UTF-8"));
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                InputStream is;
                if (responseCode >= 200 && responseCode < 400) {
                    is = conn.getInputStream();
                } else {
                    is = conn.getErrorStream();
                }

                StringBuilder sb = new StringBuilder();
                if (is != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                        String linha;
                        while ((linha = br.readLine()) != null) {
                            sb.append(linha);
                        }
                    }
                }

                String responseBody = sb.toString();
                Log.d("DEBUG_PHP", "Resposta crua do servidor: " + responseBody);
                Log.d(TAG, "Response Body: " + responseBody);

                if (responseBody.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "O servidor retornou uma resposta vazia.", Toast.LENGTH_SHORT).show());
                    return;
                }

                JSONObject json = new JSONObject(responseBody);
                runOnUiThread(() -> tratarRespostaLogin(json, email, senha));

            } catch (Exception e) {
                Log.e(TAG, "Erro na conexão", e);
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Erro na conexão: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    private void tratarRespostaLogin(JSONObject json, String email, String senha) {
        try {
            if (json.getBoolean("success")) {

                if (lembrarSwitch.isChecked()) {
                    session.saveLogin(email, senha);
                } else {
                    session.clearSession();
                }

                Intent intent = new Intent(MainActivity.this, TelaPrincipalActivity.class);
                intent.putExtra("USERNAME", json.getJSONObject("user").getString("usuario"));
                intent.putExtra("EMAIL", json.getJSONObject("user").getString("email"));
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao tratar resposta do login", e);
            Toast.makeText(this, "Erro ao processar a resposta do servidor.", Toast.LENGTH_SHORT).show();
        }
    }
}
