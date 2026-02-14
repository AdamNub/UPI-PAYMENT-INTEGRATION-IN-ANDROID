package com.adamsnub.upilib.launcher;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.net.Uri;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.adamsnub.upilib.builder.UpiIntentBuilder;
import com.adamsnub.upilib.detector.UpiAppDetector;
import com.adamsnub.upilib.models.PaymentRequest;
import com.adamsnub.upilib.models.TransactionResponse;
import com.adamsnub.upilib.models.UpiApp;
import com.adamsnub.upilib.parser.UpiResponseParser;

import java.util.List;

public class UpiPaymentLauncher {
    private static final String TAG = "UpiPaymentLauncher";
    private final Object caller;
    private final PaymentStatusListener listener;
    private final UpiIntentBuilder intentBuilder;
    private ActivityResultLauncher<Intent> launcher;
    private PaymentRequest currentRequest;
    private String targetPackage;
    private UpiAppDetector appDetector;
    private android.content.Context context;

    public UpiPaymentLauncher(Object caller, PaymentStatusListener listener) {
        this.caller = caller;
        this.listener = listener;
        this.intentBuilder = new UpiIntentBuilder();
        this.context = getContext();
        this.appDetector = new UpiAppDetector(context);
        registerLauncher();
    }

    private void registerLauncher() {
        ActivityResultContracts.StartActivityForResult contract = 
            new ActivityResultContracts.StartActivityForResult();
        
        ActivityResultCallback<ActivityResult> callback = result -> {
            handleActivityResult(result);
        };

        if (caller instanceof AppCompatActivity) {
            launcher = ((AppCompatActivity) caller).registerForActivityResult(contract, callback);
        } else if (caller instanceof Fragment) {
            launcher = ((Fragment) caller).registerForActivityResult(contract, callback);
        } else {
            throw new IllegalArgumentException("Caller must be Activity or Fragment");
        }
    }

    private void handleActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            String response = result.getData().getStringExtra("response");
            TransactionResponse transactionResponse = UpiResponseParser.parse(response);
            
            if (currentRequest != null) {
                transactionResponse.setTransactionRef(currentRequest.getTransactionRef());
            }
            
            if (listener != null) {
                listener.onTransactionCompleted(transactionResponse);
                
                if (transactionResponse.isSuccess()) {
                    listener.onTransactionSuccess();
                } else if (transactionResponse.isFailure()) {
                    listener.onTransactionFailed();
                } else if (transactionResponse.isCancelled()) {
                    listener.onTransactionCancelled();
                }
            }
        } else {
            if (listener != null) {
                listener.onTransactionCancelled();
            }
        }
    }

    /**
     * Get UPI string for QR code generation
     */
    public String getUpiStringForQr(PaymentRequest request) {
        return intentBuilder.getUpiStringForQr(request);
    }

    /**
     * Launch payment with app chooser
     */
    public void startPayment(PaymentRequest request) {
        startPayment(request, null);
    }

    /**
     * Launch payment with specific UPI app
     */
    public void startPayment(PaymentRequest request, String targetPackage) {
        this.currentRequest = request;
        this.targetPackage = targetPackage;

        // Check if ANY UPI apps exist
        List<UpiApp> installedApps = appDetector.getInstalledUpiApps();
        Log.d(TAG, "Found " + installedApps.size() + " UPI apps installed");
        
        if (installedApps.isEmpty()) {
            Log.d(TAG, "NO UPI APPS FOUND - triggering onAppNotFound");
            if (listener != null) {
                listener.onAppNotFound();
            }
            return;
        }

        // Check if target app exists (if specified)
        if (targetPackage != null && !targetPackage.isEmpty()) {
            boolean appFound = false;
            for (UpiApp app : installedApps) {
                if (app.getPackageName().equals(targetPackage)) {
                    appFound = true;
                    break;
                }
            }
            if (!appFound) {
                Log.d(TAG, "Target app not found: " + targetPackage);
                if (listener != null) {
                    listener.onAppNotFound();
                }
                return;
            }
        }

        // Only proceed if we have UPI apps
        try {
            Uri upiUri = intentBuilder.buildUpiUri(request);
            Intent intent = intentBuilder.createIntent(upiUri, targetPackage);
            launcher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error launching payment", e);
            if (listener != null) {
                listener.onTransactionFailed();
            }
        }
    }

    /**
     * Get list of installed UPI apps
     */
    public List<UpiApp> getInstalledUpiApps() {
        return appDetector.getInstalledUpiApps();
    }

    private android.content.Context getContext() {
        if (caller instanceof AppCompatActivity) {
            return ((AppCompatActivity) caller).getApplicationContext();
        } else if (caller instanceof Fragment) {
            return ((Fragment) caller).requireContext().getApplicationContext();
        }
        return null;
    }

    public void detachListener() {
        // Clean up if needed
    }
}