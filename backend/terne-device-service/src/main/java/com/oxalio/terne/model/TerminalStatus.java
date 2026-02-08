package com.oxalio.terne.model;

public enum TerminalStatus {
  ENROLLED,    // Enrôlé mais non actif
  ACTIVE,     // Autorisé à émettre des RNE
  SUSPENDED,  // Bloqué (audit, incident)
  RETIRED     // Hors service / décommissionné
}
