package com.adamsnub.upilib.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adamsnub.upilib.R;
import com.adamsnub.upilib.Singleton;
import com.adamsnub.upilib.detector.UpiAppDetector;
import com.adamsnub.upilib.launcher.PaymentStatusListener;
import com.adamsnub.upilib.models.PaymentRequest;
import com.adamsnub.upilib.models.UpiApp;
import com.adamsnub.upilib.utils.UpiIntentBuilder;
import com.adamsnub.upilib.utils.UpiResponseParser;

import java.util.List;

public class PaymentActivity extends AppCompatActivity {

    private PaymentRequest paymentRequest;
    private UpiIntentBuilder intentBuilder;
    private List<UpiApp> upiApps;
    private static final int UPI_PAYMENT_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        paymentRequest = (PaymentRequest) getIntent().getSerializableExtra("payment_request");
        intentBuilder = new UpiIntentBuilder();
        
        detectUpiAppsAndShowList();
    }

    private void detectUpiAppsAndShowList() {
        UpiAppDetector detector = new UpiAppDetector(this);
        upiApps = detector.getInstalledUpiApps();

        if (upiApps.isEmpty()) {
            PaymentStatusListener listener = Singleton.getListener();
            if (listener != null) {
                listener.onAppNotFound();
            }
            finish();
            return;
        }

        // Show list of UPI apps (you can create a RecyclerView)
        showUpiAppList();
    }

    private void showUpiAppList() {
        // For simplicity, just launch with first app or show chooser
        // In a real implementation, show a list for user to choose
        
        if (upiApps.size() == 1) {
            // Only one app, launch directly
            launchUpiApp(upiApps.get(0).getPackageName());
        } else {
            // Show chooser
            launchUpiApp(null);
        }
    }

    private void launchUpiApp(String targetPackage) {
        Uri uri = intentBuilder.buildUpiUri(paymentRequest);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        
        if (targetPackage != null) {
            intent.setPackage(targetPackage);
        }
        
        startActivityForResult(intent, UPI_PAYMENT_REQUEST);
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
            } else {
                if (listener != null) {
                    listener.onTransactionCancelled();
                }
            }
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't clear listener here - let the main activity handle it
    }
}