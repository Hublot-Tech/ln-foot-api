package co.hublots.ln_foot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

public abstract class NotchPayDto {
    @Data
    @Builder
    public static class InitiatePaymentRequest {
        private int amount;
        private String currency;
        private Customer customer;
        private String description;
        private String reference;

        @Data
        @Builder
        public static class Customer {
            private String name;
            private String email;
            private String phone;
        }
    }

    @Data
    public class InitiatePaymentResponse {
        private String status;
        private String message;
        private int code;
        private Transaction transaction;

        @JsonProperty("authorization_url")
        private String authorizationUrl;

        @Data
        public static class Transaction {
            private String id;
            private String reference;
            private String trxref;
            private int amount;
            private String currency;
            private String status;
            private String customer;
            @JsonProperty("created_at")
            private String createdAt; // or OffsetDateTime if you prefer
        }
    }

    @Data
    @Builder
    public static class ChargePaymentRequest {
        private String channel; // e.g. "cm.mtn" or "cm.mobile"
        private DataPayload data;

        @Data
        @Builder
        public static class DataPayload {
            private String phone;
        }
    }

    @Data
    public static class ChargePaymentResponse {
        private String status;
        private String message;
        private int code;
        private Transaction transaction;

        @Data
        public static class Transaction {
            private String reference;
            private String trxref;
            private String status;
        }
    }

}
