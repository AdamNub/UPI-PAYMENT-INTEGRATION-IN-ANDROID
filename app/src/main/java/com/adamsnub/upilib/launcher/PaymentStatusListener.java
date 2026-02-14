package com.adamsnub.upilib.launcher;

import com.adamsnub.upilib.models.TransactionResponse;

public interface PaymentStatusListener {
    void onTransactionCompleted(TransactionResponse transactionResponse);
    void onTransactionSuccess();
    void onTransactionFailed();
    void onTransactionCancelled();
    void onAppNotFound();
}