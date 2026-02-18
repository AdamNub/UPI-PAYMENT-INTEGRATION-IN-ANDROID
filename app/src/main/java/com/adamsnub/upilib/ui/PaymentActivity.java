package com.adamsnub.upilib.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.adamsnub.upilib.R;
import com.adamsnub.upilib.Singleton;
import com.adamsnub.upilib.detector.UpiAppDetector;
import com.adamsnub.upilib.launcher.PaymentStatusListener;
import com.adamsnub.upilib.models.PaymentRequest;
import com.adamsnub.upilib.models.UpiApp;
import com.adamsnub.upilib.parser.UpiResponseParser;
import com.adamsnub.upilib.utils.UpiIntentBuilder;

import java.util.List;

public class PaymentActivity extends AppCompatActivity {

    private PaymentRequest paymentRequest;
    private UpiIntentBuilder intentBuilder;
    private TextView tvResult;
    private ProgressBar progressBar;
    private static final int UPI_PAYMENT_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        tvResult = findViewById(R.id.tvResult);
        progressBar = findViewById(R.id.progressBar);

        paymentRequest = (PaymentRequest) getIntent().getSerializableExtra("payment_request");
        intentBuilder = new UpiIntentBuilder();

        if (paymentRequest == null) {
            finish();
            return;
        }

        launchUpiAppDirectly();
    }

    private void launchUpiAppDirectly() {
        UpiAppDetector detector = new UpiAppDetector(this);
        List<UpiApp> apps = detector.getInstalledUpiApps();

        if (apps.isEmpty()) {
            PaymentStatusListener listener = Singleton.getListener();
            if (listener != null) {
                listener.onAppNotFound();
            }
            finish();
            return;
        }

        try {
            Uri uri = intentBuilder.buildUpiUri(paymentRequest);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            
            // Show app chooser for better UX
            // Sometimes shows error " payment failed"
            startActivityForResult(Intent.createChooser(intent, "Pay with UPI app"), UPI_PAYMENT_REQUEST);
            
            tvResult.setText("Launching UPI app...");
            
        } catch (Exception e) {
            e.printStackTrace();
            tvResult.setText("Error: " + e.getMessage());
            progressBar.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPI_PAYMENT_REQUEST) {
            PaymentStatusListener listener = Singleton.getListener();

            if (resultCode == Activity.RESULT_OK && data != null) {
                String response = data.getStringExtra("response");
                if (listener != null) {
                    listener.onTransactionCompleted(
                            UpiResponseParser.parse(response)
                    );
                }
                tvResult.setText("Payment completed!");
            } else {
                if (listener != null) {
                    listener.onTransactionCancelled();
                }
                tvResult.setText("Payment cancelled");
            }
            progressBar.setVisibility(android.view.View.GONE);
            
            // Close after 1 second
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1000);
        }
    }
}