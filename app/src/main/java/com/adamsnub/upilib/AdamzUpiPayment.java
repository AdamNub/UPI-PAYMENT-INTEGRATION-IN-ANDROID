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

        if (activity instanceof AppCompatActivity) {
            lifecycleObserver = new LifecycleObserver() {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                public void onDestroy() {
                    Log.d(TAG, "Activity destroyed - clearing listener");
                    Singleton.clearListener();
                }
            };
            ((AppCompatActivity) activity).getLifecycle().addObserver(lifecycleObserver);
        }
    }

    public void startPayment() {
        UpiAppDetector detector = new UpiAppDetector(activity);

        if (!detector.hasAnyUpiApp()) {
            Log.d(TAG, "No UPI apps found - triggering onAppNotFound");
            if (Singleton.getListener() != null) {
                Singleton.getListener().onAppNotFound();
            }
            return;
        }

        Intent intent = new Intent(activity, PaymentActivity.class);
        intent.putExtra("payment_request", paymentRequest);
        activity.startActivity(intent);
    }

    public void setPaymentStatusListener(PaymentStatusListener listener) {
        Singleton.setListener(listener);
    }

    public void removePaymentStatusListener() {
        Singleton.clearListener();
    }

    public static class Builder {
        private final Activity activity;
        private String payeeVpa;
        private String payeeName;
        private String amount;
        private String transactionRef;
        private String transactionNote;
        private String currency = "INR";
        private String merchantCode;  // ← ADD THIS
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
        
        public Builder setMerchantCode(String merchantCode) {  // ← ADD THIS
            this.merchantCode = merchantCode;
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

            PaymentRequest.Builder requestBuilder = new PaymentRequest.Builder()
                    .setPayeeVpa(payeeVpa)
                    .setPayeeName(payeeName)
                    .setAmount(amount)
                    .setTransactionRef(transactionRef)
                    .setTransactionNote(transactionNote != null ? transactionNote : "")
                    .setCurrency(currency);
            
            if (merchantCode != null) {  // ← ADD THIS
                requestBuilder.setMerchantCode(merchantCode);
            }
            
            PaymentRequest request = requestBuilder.build();

            if (targetPackage != null && !targetPackage.isEmpty()) {
                if (!isPackageInstalled(targetPackage)) {
                    throw new AppNotFoundException("App not installed: " + targetPackage);
                }
            }

            return new AdamzUpiPayment(activity, request);
        }

        private void validate() {
            if (payeeVpa == null || payeeVpa.trim().isEmpty()) {
                throw new IllegalStateException("Must call setPayeeVpa() before build()");
            }
            if (!payeeVpa.matches("^[\\w.-]+@[\\w-]+$")) {
                throw new IllegalStateException("Invalid VPA format");
            }

            if (payeeName == null || payeeName.trim().isEmpty()) {
                throw new IllegalStateException("Must call setPayeeName() before build()");
            }

            if (amount == null || amount.trim().isEmpty()) {
                throw new IllegalStateException("Must call setAmount() before build()");
            }
            if (!amount.matches("\\d+(\\.\\d{1,2})?")) {
                throw new IllegalStateException("Amount must be valid decimal (e.g., 100.00)");
            }

            if (transactionRef == null || transactionRef.trim().isEmpty()) {
                throw new IllegalStateException("Must call setTransactionRef() before build()");
            }
        }
    }
}