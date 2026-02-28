package com.adamsnub.upilib.models;

import java.io.Serializable;

public class PaymentRequest implements Serializable {
    private String payeeVpa;
    private String payeeName;
    private String amount;
    private String transactionRef;
    private String transactionNote;
    private String currency;
    private String merchantCode;
    
    private PaymentRequest(Builder builder) {
        this.payeeVpa = builder.payeeVpa;
        this.payeeName = builder.payeeName;
        this.amount = builder.amount;
        this.transactionRef = builder.transactionRef;
        this.transactionNote = builder.transactionNote;
        this.currency = builder.currency;
        this.merchantCode = builder.merchantCode;
    }
    
    // Getters
    public String getPayeeVpa() { return payeeVpa; }
    public String getPayeeName() { return payeeName; }
    public String getAmount() { return amount; }
    public String getTransactionRef() { return transactionRef; }
    public String getTransactionNote() { return transactionNote; }
    public String getCurrency() { return currency; }
    public String getMerchantCode() { return merchantCode; }
    
    public static class Builder {
        private String payeeVpa;
        private String payeeName;
        private String amount;
        private String transactionRef;
        private String transactionNote = "";
        private String currency = "INR";
        private String merchantCode;
        
        public Builder setPayeeVpa(String payeeVpa) {
            this.payeeVpa = payeeVpa;
            return this;
        }
        
        public Builder setPayeeName(String payeeName) {
            this.payeeName = payeeName;
            return this;
        }
        
        public Builder setAmount(String amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder setTransactionRef(String transactionRef) {
            this.transactionRef = transactionRef;
            return this;
        }
        
        public Builder setTransactionNote(String transactionNote) {
            this.transactionNote = transactionNote;
            return this;
        }
        
        public Builder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }
        
        public Builder setMerchantCode(String merchantCode) {
            this.merchantCode = merchantCode;
            return this;
        }
        
        public PaymentRequest build() {
            if (payeeVpa == null || payeeVpa.isEmpty()) {
                throw new IllegalStateException("Payee VPA is required");
            }
            if (payeeName == null || payeeName.isEmpty()) {
                throw new IllegalStateException("Payee name is required");
            }
            if (amount == null || amount.isEmpty()) {
                throw new IllegalStateException("Amount is required");
            }
            if (transactionRef == null || transactionRef.isEmpty()) {
                throw new IllegalStateException("Transaction reference is required");
            }
            
            return new PaymentRequest(this);
        }
    }
}