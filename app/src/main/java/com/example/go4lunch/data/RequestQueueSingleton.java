package com.example.go4lunch.data;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RequestQueueSingleton {
    private final RequestQueue requestQueue;
    private static RequestQueueSingleton INSTANCE;

    public RequestQueueSingleton(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public static RequestQueueSingleton getInstance(Context context){
        if (INSTANCE == null)
            INSTANCE = new RequestQueueSingleton(context);

        return INSTANCE;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }
}
