package com.adamsnub.upilib;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.adamsnub.upilib.detector.UpiAppDetector;
import com.adamsnub.upilib.exception.AppNotFoundException;
import com.adamsnub.upilib.launcher.PaymentStatusListener;
import com.adamsnub.upilib.models.PaymentRequest;
import com.adamsnub.upilib.ui.PaymentActivity;

public class AdamzUpiPayment {

    private final Activity activity;
    private final PaymentRequest paymentRequest;
    private LifecycleObserver lifecycleObserver;
    private static final String TAG = "AdamzUpiPayment";

    private AdamzUpiPayment(Activity activity, PaymentRequest paymentRequest) {
        this.activity = activity;
        this.paymentRequest = paymentRequest;

        // Register a lifecycle observer to clear the listener when the activity is destroyed
        if (activity instanceof AppCompatActivity) {
            lifecycleObserver = new LifecycleObserver() {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                public void onDestroy() {
                    Log.d(TAG, "Activity destroyed - clearing listener");
                    Singleton.clearListener(); // Clear the listener on destroy
                }
            };
            registerLifecycleObserver((AppCompatActivity) activity);
        }
    }

    public void startPayment() {
        // Check if any UPI apps exist before launching
        UpiAppDetector detector = new UpiAppDetector(activity);

        if (!detector.hasAnyUpiApp()) {
            Log.d(TAG, "No UPI apps found - triggering onAppNotFound");
            if (Singleton.getListener() != null) {
                Singleton.getListener().onAppNotFound();
            }
            return; // Don't launch PaymentActivity if no UPI apps are available
        }

        // UPI apps exist, proceed with the payment flow
        Intent intent = new Intent(activity, PaymentActivity.class);
        intent.putExtra("payment_request", paymentRequest);
        activity.startActivity(intent);
    }

    public void setPaymentStatusListener(PaymentStatusListener listener) {
        Singleton.setListener(listener); // Set the listener
    }

    public void removePaymentStatusListener() {
        Singleton.clearListener(); // Clear the listener
    }

    private void registerLifecycleObserver(LifecycleOwner owner) {
        owner.getLifecycle().addObserver(lifecycleObserver);
    }

    public static class Builder {
        private final Activity activity;
        private String payeeVpa;
        private String payeeName;
        private String amount;
        private String transactionRef;
        private String transactionNote;
        private String currency = "INR";
        private String targetPackage;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder setPayeeVpa(String vpa) {
            this.payeeVpa = vpa;
            return this;
        }

        public Builder setPayeeName(String name) {
            this.payeeName = name;
            return this;
        }

        public Builder setAmount(String amount) {
            this.amount = amount;
            return this;
        }

        public Builder setTransactionRef(String ref) {
            this.transactionRef = ref;
            return this;
        }

        public Builder setTransactionNote(String note) {
            this.transactionNote = note;
            return this;
        }

        public Builder setTargetPackage(String packageName) {
            this.targetPackage = packageName;
            return this;
        }

        @VisibleForTesting
        boolean isPackageInstalled(String packageName) {
            try {
                activity.getPackageManager().getPackageInfo(packageName, 0);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public AdamzUpiPayment build() throws IllegalStateException, AppNotFoundException {
            validate();

            PaymentRequest request = new PaymentRequest.Builder()
                    .setPayeeVpa(payeeVpa)
                    .setPayeeName(payeeName)
                    .setAmount(amount)
                    .setTransactionRef(transactionRef)
                    .setTransactionNote(transactionNote != null ? transactionNote : "")
                    .setCurrency(currency)
                    .build();

            // Validate if the specific UPI app package is installed
            if (targetPackage != null && !targetPackage.isEmpty()) {
                if (!isPackageInstalled(targetPackage)) {
                    throw new AppNotFoundException("App not installed: " + targetPackage);
                }
            }

            return new AdamzUpiPayment(activity, request);
        }

        private void validate() {
            // Validate payeeVpa
            if (payeeVpa == null || payeeVpa.trim().isEmpty()) {
                throw new IllegalStateException("Must call setPayeeVpa() before build()");
            }
            if (!payeeVpa.matches("^[\\w.-]+@[\\w-]+$")) {
                throw new IllegalStateException("Invalid VPA format");
            }

            // Validate payeeName
            if (payeeName == null || payeeName.trim().isEmpty()) {
                throw new IllegalStateException("Must call setPayeeName() before build()");
            }

            // Validate amount
            if (amount == null || amount.trim().isEmpty()) {
                throw new IllegalStateException("Must call setAmount() before build()");
            }
            if (!amount.matches("\\d+(\\.\\d{1,2})?")) {
                throw new IllegalStateException("Amount must be valid decimal (e.g., 100.00)");
            }

            // Validate transactionRef
            if (transactionRef == null || transactionRef.trim().isEmpty()) {
                throw new IllegalStateException("Must call setTransactionRef() before build()");
            }
        }
    }
}