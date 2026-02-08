package com.oxalio.invoice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oxalio.invoice.dto.InvoiceRequest;
import com.oxalio.invoice.dto.InvoiceResponse;
import com.oxalio.invoice.service.InvoiceService;
import com.oxalio.invoice.dto.SellerDTO;
import com.oxalio.invoice.dto.BuyerDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * ✅ Tests unitaires pour InvoiceController avec les DTOs.
 */
@WebMvcTest(InvoiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InvoiceService invoiceService;

    @Test
    void shouldCreateInvoiceSuccessfully() throws Exception {
        // Given
        InvoiceRequest request = buildValidInvoiceRequest();
        InvoiceResponse response = buildInvoiceResponse();

        when(invoiceService.createInvoice(any(InvoiceRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invoiceNumber").value("INV-2025-000001"))
                .andExpect(jsonPath("$.stickerId").value("STKR-ABC12345"))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.totals.totalAmount").value(23600));
    }

    @Test
    void shouldRejectInvalidInvoiceRequest() throws Exception {
        // Given
        InvoiceRequest invalidRequest = InvoiceRequest.builder().build();

        // When & Then
        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidTaxIdFormat() throws Exception {
        // Given
        InvoiceRequest request = buildValidInvoiceRequest();
        request.getSeller().setTaxId("INVALID_TAX_ID");

        // When & Then
        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void shouldRejectInvalidCurrency() throws Exception {
        // Given
        InvoiceRequest request = buildValidInvoiceRequest();
        request.setCurrency("JPY");

        // When & Then
        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectEmptyInvoiceLines() throws Exception {
        // Given
        InvoiceRequest request = buildValidInvoiceRequest();
        request.setLines(List.of());

        // When & Then
        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAllInvoices() throws Exception {
        // Given
        List<InvoiceResponse> responses = List.of(buildInvoiceResponse());
        when(invoiceService.getAllInvoices()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].invoiceNumber").value("INV-2025-000001"));
    }

    @Test
    void shouldGetInvoiceById() throws Exception {
        // Given
        InvoiceResponse response = buildInvoiceResponse();
        when(invoiceService.getInvoiceById(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/invoices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.invoiceNumber").value("INV-2025-000001"));
    }

    // -----------------------
    // Helpers pour les objets
    // -----------------------

    private InvoiceRequest buildValidInvoiceRequest() {
        return InvoiceRequest.builder()
                .invoiceType("STANDARD")
                .template("B2C") 
                .currency("XOF")
                .seller(com.oxalio.invoice.dto.SellerDTO.builder()
                        .taxId("2505842N") // ✅ Doit respecter le Pattern ^CI[0-9]{7,10}$
                        .companyName("Oxalio SARL")
                        .address("Abidjan, Plateau, Immeuble Alpha") // ✅ AJOUTER CECI
                        .pointOfSaleName("TERMINAL-01") 
                        .build())
                .buyer(com.oxalio.invoice.dto.BuyerDTO.builder()
                        .taxId("7654321N")
                        .name("Client Démo")
                        .address("Abidjan, Cocody, Rue des Jardins") // ✅ AJOUTER CECI
                        .build())
                .lines(List.of(
                        InvoiceRequest.InvoiceLineDTO.builder()
                                .description("Produit A")
                                .quantity(BigDecimal.valueOf(2))
                                .unitPrice(BigDecimal.valueOf(10000))
                                .vatRate(BigDecimal.valueOf(18))
                                .vatAmount(BigDecimal.valueOf(3600))
                                .discount(BigDecimal.ZERO)
                                .build()
                ))
                .totals(InvoiceRequest.TotalsDTO.builder()
                        .subtotal(BigDecimal.valueOf(20000))
                        .totalVat(BigDecimal.valueOf(3600))
                        .totalAmount(BigDecimal.valueOf(23600))
                        .otherTaxes(BigDecimal.valueOf(100))
                        .totalToPay(BigDecimal.valueOf(23700))
                        .build())
                .paymentMode("CASH")
                .build();
        }

    private InvoiceResponse buildInvoiceResponse() {
        return InvoiceResponse.builder()
                .id(1L)
                .invoiceNumber("INV-2025-000001")
                .invoiceType("STANDARD")
                .currency("XOF")
                .issueDate(Instant.now())
                .seller(InvoiceResponse.SellerDTO.builder()
                        .taxId("2505842N")
                        .companyName("Oxalio SARL")
                        .address("Abidjan, Plateau")
                        .email("contact@oxalio.com")
                        .phone("+2250701020304")
                        .build())
                .buyer(InvoiceResponse.BuyerDTO.builder()
                        .taxId("7654321N")
                        .name("Client Démo")
                        .address("Cocody, Riviera")
                        .email("client@demo.com")
                        .phone("+2250705060708")
                        .build())
                .lines(List.of(
                        InvoiceResponse.InvoiceLineDTO.builder()
                                .description("Produit A")
                                .quantity(BigDecimal.valueOf(2))
                                .unitPrice(BigDecimal.valueOf(10000))
                                .vatRate(BigDecimal.valueOf(18))
                                .vatAmount(BigDecimal.valueOf(3600))
                                .discount(BigDecimal.ZERO)
                                .productCode("PROD-A001")
                                .lineTotal(BigDecimal.valueOf(23600))
                                .build()
                ))
                .totals(InvoiceResponse.TotalsDTO.builder()
                        .subtotal(BigDecimal.valueOf(20000))
                        .totalVat(BigDecimal.valueOf(3600))
                        .totalAmount(BigDecimal.valueOf(23600))
                        .totalDiscount(BigDecimal.ZERO)
                        .build())
                .paymentMode("TRANSFER")
                .stickerId("STKR-ABC12345")
                .qrBase64("iVBORw0KGgoAAAANSUhEUg...")
                .status("RECEIVED")
                .notes("Facture de test")
                .issueDate(Instant.now())
                .issueDate(Instant.now())
                .build();
    }

    @Test
        void shouldCreateBapaSuccessfully() throws Exception {
        InvoiceRequest request = buildValidBapaRequest();
        InvoiceResponse response = buildBapaResponse();
        
        when(invoiceService.createInvoice(any(InvoiceRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invoiceType").value("purchase"));
        }

        @Test
        void shouldAcceptEmptyBuyerTaxIdForBapa() throws Exception {
        InvoiceRequest request = buildValidBapaRequest();
        request.getBuyer().setTaxId(""); // Producteur sans NCC
        
        InvoiceResponse response = buildBapaResponse();
        when(invoiceService.createInvoice(any(InvoiceRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        }

        @Test
        void shouldAcceptMobileMoneyPayment() throws Exception {
        InvoiceRequest request = buildValidInvoiceRequest();
        request.setPaymentMode("MOBILE_MONEY");
        
        InvoiceResponse response = buildInvoiceResponse();
        when(invoiceService.createInvoice(any(InvoiceRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/v1/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        }

        private InvoiceRequest buildValidBapaRequest() {
        return InvoiceRequest.builder()
                .invoiceType("purchase")
                .template("purchase")
                .currency("XOF")
                .seller(SellerDTO.builder()
                .taxId("2505842N")
                .companyName("COOPÉRATIVE AGRICOLE")
                .address("Bouaké")
                .build())
                .buyer(BuyerDTO.builder()
                .taxId("")
                .name("KOUASSI Jean")
                .phone("0708123456")
                .build())
                .lines(List.of(
                InvoiceRequest.InvoiceLineDTO.builder()
                        .description("Cacao")
                        .quantity(BigDecimal.valueOf(500))
                        .unitPrice(BigDecimal.valueOf(750))
                        .vatRate(BigDecimal.ZERO)
                        .vatAmount(BigDecimal.ZERO)
                        .discount(BigDecimal.ZERO)
                        .build()
                ))
                .totals(InvoiceRequest.TotalsDTO.builder()
                .subtotal(BigDecimal.valueOf(375000))
                .totalVat(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(375000))
                .totalToPay(BigDecimal.valueOf(375000))
                .build())
                .paymentMode("CASH")
                .build();
        }

        private InvoiceResponse buildBapaResponse() {
        return InvoiceResponse.builder()
                .id(1L)
                .invoiceNumber("INV-2026-000001")
                .invoiceType("purchase")
                .template("purchase")
                .currency("XOF")
                .status("RECEIVED")
                .build();
        }
}
