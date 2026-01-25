package com.oxalio.invoice.dto;

import lombok.Data;
import java.util.List;

@Data
public class RefundRequest {
    private String reason;
    private List<RefundItem> items;

    @Data
    public static class RefundItem {
        private Long id;
        private Integer quantity;
    }
}