package com.syncmeet.app.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.syncmeet.app.EventoLembreteReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Agenda e cancela o lembrete do evento (40 minutos antes) via AlarmManager.
 *
 * Compatibilidade:
 * - Android 12+ (API 31): alarme EXATO exige a permissão SCHEDULE_EXACT_ALARM
 *   (acesso especial, concedido pelo usuário nas configurações). Quando não
 *   concedido, o agendamento cai automaticamente para setAndAllowWhileIdle
 *   (pode atrasar alguns minutos — aceitável para lembrete de evento).
 * - Android 13+ (API 33): a notificação em si exige POST_NOTIFICATIONS
 *   solicitada em runtime; sem ela o alarme dispara mas nada é exibido.
 * - Android 12 e inferiores: funciona sem nenhuma ação extra do usuário.
 */
public final class LembreteScheduler {

    public static final int MINUTOS_ANTECEDENCIA = 40;
    private static final int REQ_NOTIFICACAO = 1001;
    private static final String TAG = "LembreteScheduler";

    private LembreteScheduler() {
    }

    /** Agenda o lembrete de um evento. dataStr = dd/MM/yyyy, horaStr = HH:mm. */
    public static void agendar(Context context, int idEvento, String nome, String dataStr, String horaStr) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(dataStr));

            String[] h = horaStr.split(":");
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(h[0]));
            cal.set(Calendar.MINUTE, Integer.parseInt(h[1]));
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            long disparo = cal.getTimeInMillis() - MINUTOS_ANTECEDENCIA * 60_000L;
            if (disparo <= System.currentTimeMillis()) {
                return; // evento iminente ou já passado: nada a agendar
            }

            Intent intent = new Intent(context, EventoLembreteReceiver.class);
            intent.putExtra(EventoLembreteReceiver.EXTRA_ID, idEvento);
            intent.putExtra(EventoLembreteReceiver.EXTRA_NOME, nome);
            intent.putExtra(EventoLembreteReceiver.EXTRA_DATA, dataStr);
            intent.putExtra(EventoLembreteReceiver.EXTRA_HORA, horaStr);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    idEvento,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparo, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparo, pendingIntent);
            }

        } catch (Exception e) {
            Log.e(TAG, "Falha ao agendar lembrete do evento " + idEvento, e);
        }
    }

    /** Cancela o lembrete agendado para um evento. */
    public static void cancelar(Context context, int idEvento) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                idEvento,
                new Intent(context, EventoLembreteReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    /** POST_NOTIFICATIONS concedida? (sempre true abaixo do Android 13) */
    public static boolean temPermissaoNotificacao(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Solicita POST_NOTIFICATIONS em runtime (necessário a partir do Android 13).
     * Chamar em momento de uso — ao criar/editar um evento com lembrete.
     */
    public static void pedirPermissaoNotificacao(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && !temPermissaoNotificacao(activity)) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQ_NOTIFICACAO
            );
        }
    }
}
