// src/main/java/com/oxalio/invoice/controller/dto/InvoiceCreateResponse.java
package com.oxalio.invoice.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvoiceCreateResponse {
    private Long id;
    private String invoiceNumber;
    private String status;
}
