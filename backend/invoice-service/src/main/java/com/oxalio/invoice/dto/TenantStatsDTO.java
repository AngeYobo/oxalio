package com.oxalio.invoice.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantStatsDTO {
    
    private Long totalTenants;
    private Long activeTenants;
    private Long trialTenants;
    
    private Long starterPlanCount;
    private Long professionalPlanCount;
    private Long enterprisePlanCount;
    
    private Long totalInvoicesThisMonth;
    private Long totalRevenue; // En FCFA
}