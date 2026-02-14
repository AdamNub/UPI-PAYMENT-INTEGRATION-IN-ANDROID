package com.adamsnub.upilib;

import com.adamsnub.upilib.launcher.PaymentStatusListener;

public class Singleton {
    private static PaymentStatusListener listener;
    
    public static void setListener(PaymentStatusListener l) {
        listener = l;
    }
    
    public static PaymentStatusListener getListener() {
        return listener;
    }
    
    public static void clearListener() {
        listener = null;
    }
}