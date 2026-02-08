package com.oxalio.terne.model;

public enum LocationSource {
  MDM,          // Remontée par SOTI / autre MDM
  DEVICE_AGENT, // Remontée par l'app TERNE
  MANUAL        // Exceptionnel (support)
}
