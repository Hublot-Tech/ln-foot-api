package co.hublots.ln_foot.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class NotchPayDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitiatePaymentRequest {
        private Double amount;
        private String currency;
        private Customer customer;
        private String description;
        private String reference;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Customer {
            private String name;
            private String email;
            private String phone;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitiatePaymentResponse {
        private String status;
        private String message;
        private Integer code;
        private Transaction transaction;

        @JsonProperty("authorization_url")
        private String authorizationUrl;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Transaction {
            private String id;
            private String reference;
            private String trxref;
            private Integer amount;
            private String currency;
            private String status;
            private String customer;

            @JsonProperty("created_at")
            private OffsetDateTime createdAt;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChargePaymentRequest {
        private String channel;

        @JsonProperty("data")
        private ChargeData data;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ChargeData {
            private String phone;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChargePaymentResponse {
        private String status;
        private String message;
        private Integer code;
        private Transaction transaction;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Transaction {
            private String reference;
            private String trxref;
            private String status;
        }
    }
}
