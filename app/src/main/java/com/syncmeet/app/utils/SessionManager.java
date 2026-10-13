package com.syncmeet.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Sessão do usuário.
 *
 * Por segurança NÃO guarda a senha — apenas o e-mail (função "lembrar de mim")
 * e o id do usuário logado, usados nas chamadas da API.
 */
public class SessionManager {

    private static final String PREF_NAME = "SyncMeetSession";
    private static final String KEY_LOGIN = "loginSaved";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_REMEMBER = "remember";

    private final SharedPreferences pref;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Inicia a sessão após o login. O id é sempre guardado (usado pela API);
     * o e-mail só é guardado quando o usuário marca "lembrar de mim".
     */
    public void startSession(int userId, String email, boolean lembrar) {
        SharedPreferences.Editor editor = pref.edit().putInt(KEY_USER_ID, userId);
        if (lembrar) {
            editor.putString(KEY_LOGIN, email).putBoolean(KEY_REMEMBER, true);
        } else {
            editor.remove(KEY_LOGIN).putBoolean(KEY_REMEMBER, false);
        }
        editor.apply();
    }

    /** Apaga todos os dados da sessão (logout completo / exclusão de conta). */
    public void clearSession() {
        pref.edit().clear().apply();
    }

    public boolean isRemembered() {
        return pref.getBoolean(KEY_REMEMBER, false);
    }

    public String getSavedLogin() {
        return pref.getString(KEY_LOGIN, "");
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, 0);
    }
}
