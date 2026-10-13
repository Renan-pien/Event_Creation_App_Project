package com.syncmeet.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.syncmeet.app.utils.LembreteScheduler;

public class EventoLembreteReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "syncmeet_lembretes";
    public static final String EXTRA_ID = "id_evento";
    public static final String EXTRA_NOME = "nome_evento";
    public static final String EXTRA_DATA = "data_evento";
    public static final String EXTRA_HORA = "hora_evento";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null) return;

        int idEvento = intent.getIntExtra(EXTRA_ID, -1);
        String nome = intent.getStringExtra(EXTRA_NOME);
        String data = intent.getStringExtra(EXTRA_DATA);
        String hora = intent.getStringExtra(EXTRA_HORA);

        if (idEvento == -1 || nome == null || data == null || hora == null) {
            return; // evita crash
        }

        // Android 13+ (API 33): sem POST_NOTIFICATIONS concedida pelo usuário,
        // a notificação não pode ser exibida — encerra sem erro.
        if (!LembreteScheduler.temPermissaoNotificacao(context)) {
            return;
        }

        criarCanal(context);

        // Intent ao tocar na notificação (abrir tela de detalhes)
        Intent abrirIntent = new Intent(context, EditarEventoDetailActivity.class);
        abrirIntent.putExtra("id", idEvento);
        abrirIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                idEvento, // mesmo ID = substitui intent antiga
                abrirIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String titulo = "Reunião em 40 minutos";
        String texto = nome + " às " + hora + " (" + data + ")";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        "Seu evento \"" + nome + "\" começa em 40 minutos, às "
                                + hora + " do dia " + data + "."
                ))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(idEvento, builder.build()); // usa id do evento como id da notificação
    }

    private void criarCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Lembretes de eventos";
            String description = "Notificações para reuniões do SyncMeet";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
