package com.oxalio.terne.model;

public enum CommandStatus {
  QUEUED,
  ACKED,
  RUNNING,
  SUCCEEDED,
  FAILED,
  CANCELED
}
