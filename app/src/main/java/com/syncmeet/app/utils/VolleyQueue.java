package com.syncmeet.app.utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Fila única de requisições Volley para todo o app (antes cada Activity
 * criava a própria fila, multiplicando threads desnecessariamente).
 */
public final class VolleyQueue {

    private static RequestQueue fila;

    private VolleyQueue() {
    }

    public static synchronized RequestQueue getInstance(Context context) {
        if (fila == null) {
            fila = Volley.newRequestQueue(context.getApplicationContext());
        }
        return fila;
    }

    public static <T> void add(Context context, Request<T> request) {
        getInstance(context).add(request);
    }
}
