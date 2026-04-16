package com.example.integration_with_bakong_khqr.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckPaymentRequest {
    private String qrMd5;
}