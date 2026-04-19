package com.example.integration_with_bakong_khqr.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BakongPaymentResponse {

    private int responseCode;   // 0 = success
    private String responseMessage;
    private Data data;

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private String hash;           // transaction hash
        private String fromAccountId;  // payer account
        private String toAccountId;    // your account
        private Double amount;
        private String currency;
        private String createdDateMs;
    }
}