package com.adamsnub.upilib.builder;

import android.content.Intent;
import android.net.Uri;

import com.adamsnub.upilib.models.PaymentRequest;

public class UpiIntentBuilder {
    
    /**
     * Build UPI URI from payment request
     */
    public Uri buildUpiUri(PaymentRequest request) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", request.getPayeeVpa())
                .appendQueryParameter("pn", request.getPayeeName())
                .appendQueryParameter("am", request.getAmount())
                .appendQueryParameter("tr", request.getTransactionRef())
                .appendQueryParameter("cu", request.getCurrency());
        
        // Add transaction note if present
        if (request.getTransactionNote() != null && !request.getTransactionNote().isEmpty()) {
            builder.appendQueryParameter("tn", request.getTransactionNote());
        }
        
        return builder.build();
    }
    
    /**
     * Get UPI string for QR code (same as URI but as string)
     */
    public String getUpiStringForQr(PaymentRequest request) {
        return buildUpiUri(request).toString();
    }
    
    /**
     * Create intent for specific UPI app
     */
    public Intent createIntent(Uri upiUri, String targetPackage) {
        Intent intent = new Intent(Intent.ACTION_VIEW, upiUri);
        if (targetPackage != null && !targetPackage.isEmpty()) {
            intent.setPackage(targetPackage);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
    
    /**
     * Create intent that shows app chooser
     */
    public Intent createChooserIntent(Uri upiUri, String title) {
        Intent intent = new Intent(Intent.ACTION_VIEW, upiUri);
        return Intent.createChooser(intent, title);
    }
    
    /**
     * Validate if a string is a valid UPI amount format
     */
    public boolean isValidAmount(String amount) {
        if (amount == null || amount.isEmpty()) return false;
        return amount.matches("^\\d+(\\.\\d{1,2})?$");
    }
    
    /**
     * Validate UPI VPA format (simple validation)
     */
    public boolean isValidVpa(String vpa) {
        if (vpa == null || vpa.isEmpty()) return false;
        return vpa.matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$");
    }
}