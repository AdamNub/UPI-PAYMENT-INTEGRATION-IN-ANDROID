package com.adamsnub.upilib.models;

public class TransactionResponse {
    private String transactionId;
    private String responseCode;
    private String approvalRefNo;
    private String status;
    private String transactionRef;
    private String rawResponse;

    // Status constants
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public TransactionResponse() {
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getApprovalRefNo() {
        return approvalRefNo;
    }

    public void setApprovalRefNo(String approvalRefNo) {
        this.approvalRefNo = approvalRefNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public boolean isSuccess() {
        return STATUS_SUCCESS.equals(status);
    }

    public boolean isFailure() {
        return STATUS_FAILURE.equals(status);
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }
}