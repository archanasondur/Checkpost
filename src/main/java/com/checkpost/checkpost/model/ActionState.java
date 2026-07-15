package com.checkpost.checkpost.model;

public enum ActionState {
    SUBMITTED,
    POLICY_EVALUATING,
    PENDING_APPROVAL,
    APPROVED,
    EXECUTING,
    SUCCEEDED,
    FAILED,
    KILLED,
    TIMED_OUT,
    DENIED
}