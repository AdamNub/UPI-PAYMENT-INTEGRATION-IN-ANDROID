package com.adamsnub.upilib.utils;

import android.content.Intent;
import android.net.Uri;

import com.adamsnub.upilib.models.PaymentRequest;

public class UpiIntentBuilder {
    
    public Uri buildUpiUri(PaymentRequest request) {
        return buildUpiUri(request, null);
    }
    
    public Uri buildUpiUri(PaymentRequest request, String merchantCode) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", request.getPayeeVpa())
                .appendQueryParameter("pn", request.getPayeeName())
                .appendQueryParameter("am", request.getAmount())
                .appendQueryParameter("tr", request.getTransactionRef())
                .appendQueryParameter("cu", request.getCurrency())
                .appendQueryParameter("mode", "05");
        
        // Add merchant code if provided
        if (merchantCode != null && !merchantCode.isEmpty()) {
            builder.appendQueryParameter("mc", merchantCode);
        }
        
        if (request.getTransactionNote() != null && !request.getTransactionNote().isEmpty()) {
            builder.appendQueryParameter("tn", request.getTransactionNote());
        }
        
        return builder.build();
    }
    
    public String getUpiStringForQr(PaymentRequest request) {
        return buildUpiUri(request).toString();
    }
    
    public Intent createIntent(Uri upiUri, String targetPackage) {
        Intent intent = new Intent(Intent.ACTION_VIEW, upiUri);
        if (targetPackage != null && !targetPackage.isEmpty()) {
            intent.setPackage(targetPackage);
        }
        return intent;
    }
    
    public Intent createChooserIntent(Uri upiUri, String title) {
        Intent intent = new Intent(Intent.ACTION_VIEW, upiUri);
        return Intent.createChooser(intent, title);
    }
    
    public boolean isValidAmount(String amount) {
        if (amount == null || amount.isEmpty()) return false;
        return amount.matches("^\\d+(\\.\\d{1,2})?$");
    }
    
    public boolean isValidVpa(String vpa) {
        if (vpa == null || vpa.isEmpty()) return false;
        return vpa.matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$");
    }
}