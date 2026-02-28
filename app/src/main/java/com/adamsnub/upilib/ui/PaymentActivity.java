package com.adamsnub.upilib.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.adamsnub.upilib.R;
import com.adamsnub.upilib.Singleton;
import com.adamsnub.upilib.detector.UpiAppDetector;
import com.adamsnub.upilib.launcher.PaymentStatusListener;
import com.adamsnub.upilib.models.PaymentRequest;
import com.adamsnub.upilib.models.TransactionResponse;
import com.adamsnub.upilib.models.UpiApp;
import com.adamsnub.upilib.parser.UpiResponseParser;
import com.adamsnub.upilib.utils.UpiIntentBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.List;

public class PaymentActivity extends AppCompatActivity {

    private PaymentRequest paymentRequest;
    private UpiIntentBuilder intentBuilder;
    private TextView tvResult, tvQrInstruction;
    private ProgressBar progressBar;
    private ImageView ivQrCode;
    private LinearLayout qrLayout;
    private Button btnRetryIntent;
    private static final int UPI_PAYMENT_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        tvResult = findViewById(R.id.tvResult);
        progressBar = findViewById(R.id.progressBar);
        ivQrCode = findViewById(R.id.ivQrCode);
        qrLayout = findViewById(R.id.qrLayout);
        tvQrInstruction = findViewById(R.id.tvQrInstruction);
        btnRetryIntent = findViewById(R.id.btnRetryIntent);

        paymentRequest = (PaymentRequest) getIntent().getSerializableExtra("payment_request");
        intentBuilder = new UpiIntentBuilder();

        if (paymentRequest == null) {
            finish();
            return;
        }

        // Try intent payment first
        launchUpiAppDirectly();

        btnRetryIntent.setOnClickListener(v -> {
            qrLayout.setVisibility(android.view.View.GONE);
            progressBar.setVisibility(android.view.View.VISIBLE);
            tvResult.setText("Retrying intent payment...");
            launchUpiAppDirectly();
        });
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
            Uri uri;
            // Pass merchant code if available
            if (paymentRequest.getMerchantCode() != null && !paymentRequest.getMerchantCode().isEmpty()) {
                uri = intentBuilder.buildUpiUri(paymentRequest, paymentRequest.getMerchantCode());
            } else {
                uri = intentBuilder.buildUpiUri(paymentRequest);
            }
            
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            
            startActivityForResult(Intent.createChooser(intent, "Pay with UPI app"), UPI_PAYMENT_REQUEST);
            tvResult.setText("Launching UPI app...");
            
        } catch (Exception e) {
            e.printStackTrace();
            // Intent failed - show QR fallback
            showQrFallback();
        }
    }

    private void showQrFallback() {
        try {
            String upiString = intentBuilder.buildUpiUri(paymentRequest).toString();
            Bitmap qrCode = generateQrCode(upiString, 500, 500);
            
            ivQrCode.setImageBitmap(qrCode);
            tvQrInstruction.setText("Scan this QR code with any UPI app");
            qrLayout.setVisibility(android.view.View.VISIBLE);
            progressBar.setVisibility(android.view.View.GONE);
            tvResult.setText("Intent payment failed. Use QR instead.");
            
        } catch (Exception e) {
            e.printStackTrace();
            tvResult.setText("Error: " + e.getMessage());
        }
    }

    private Bitmap generateQrCode(String content, int width, int height) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
            
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPI_PAYMENT_REQUEST) {
            PaymentStatusListener listener = Singleton.getListener();
            
            // Hide progress bar
            progressBar.setVisibility(android.view.View.GONE);

            if (resultCode == Activity.RESULT_OK && data != null) {
                String response = data.getStringExtra("response");
                TransactionResponse transactionResponse = UpiResponseParser.parse(response);
                
                if (listener != null) {
                    if (transactionResponse.isSuccess()) {
                        listener.onTransactionCompleted(transactionResponse);
                        tvResult.setText("Payment completed!");
                        qrLayout.setVisibility(android.view.View.GONE);
                    } else if (transactionResponse.isFailure()) {
                        listener.onTransactionCancelled();
                        tvResult.setText("Payment failed. Please check your PIN.");
                    } else if (transactionResponse.isSubmitted()) {
                        listener.onTransactionCancelled();
                        tvResult.setText("Payment pending. Check transaction status.");
                    }
                }
                
                // Close after 1 second
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1000);
                
            } else {
                // Payment failed or was cancelled
                if (listener != null) {
                    listener.onTransactionCancelled();
                }
                tvResult.setText("Payment failed or cancelled");
                
                // Show QR as fallback option
                qrLayout.setVisibility(android.view.View.VISIBLE);
                tvQrInstruction.setText("Try scanning QR code instead");
            }
        }
    }
}