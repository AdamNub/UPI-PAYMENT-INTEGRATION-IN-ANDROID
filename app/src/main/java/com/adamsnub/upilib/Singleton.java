package com.adamsnub.upilib;

import com.adamsnub.upilib.launcher.PaymentStatusListener;
import java.util.concurrent.atomic.AtomicReference;

public class Singleton {

    private static final AtomicReference<PaymentStatusListener> listener = new AtomicReference<>();

    public static void setListener(PaymentStatusListener l) {
        listener.set(l);
    }

    public static PaymentStatusListener getListener() {
        return listener.get();
    }

    public static void clearListener() {
        listener.set(null);
    }
}