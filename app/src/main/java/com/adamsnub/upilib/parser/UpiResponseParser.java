package com.adamsnub.upilib.parser;

import com.adamsnub.upilib.models.TransactionResponse;

import java.util.HashMap;
import java.util.Map;

public class UpiResponseParser {

    public static TransactionResponse parse(String response) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setRawResponse(response);

        if (response == null || response.isEmpty()) {
            transactionResponse.setStatus(TransactionResponse.STATUS_CANCELLED);
            return transactionResponse;
        }

        Map<String, String> responseMap = parseResponseString(response);

        // Extract common fields
        transactionResponse.setTransactionId(responseMap.get("txnId"));
        transactionResponse.setResponseCode(responseMap.get("responseCode"));
        transactionResponse.setApprovalRefNo(responseMap.get("ApprovalRefNo"));
        transactionResponse.setStatus(determineStatus(responseMap));

        return transactionResponse;
    }

    private static Map<String, String> parseResponseString(String response) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = response.split("&");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                map.put(keyValue[0], keyValue[1]);
            }
        }
        return map;
    }

    private static String determineStatus(Map<String, String> responseMap) {
        // Check status from response
        String status = responseMap.get("status");
        if (status != null) {
            if ("success".equalsIgnoreCase(status)) {
                return TransactionResponse.STATUS_SUCCESS;
            } else if ("failure".equalsIgnoreCase(status)) {
                return TransactionResponse.STATUS_FAILURE;
            }
        }

        // Check response code
        String responseCode = responseMap.get("responseCode");
        if (responseCode != null) {
            // UPI success codes typically start with 0
            if (responseCode.startsWith("0")) {
                return TransactionResponse.STATUS_SUCCESS;
            }
        }

        return TransactionResponse.STATUS_FAILURE;
    }
}