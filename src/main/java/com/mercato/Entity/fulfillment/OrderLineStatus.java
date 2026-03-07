package com.mercato.Entity.fulfillment;

public enum OrderLineStatus {
    CREATED,
    CONFIRMED,
    PROCESSING,
    PARTIALLY_PROCESSED,
    FULFILLED,
    PARTIALLY_FULFILLED,
    CANCELLED;

    public boolean isTerminal() {
        return this == FULFILLED
                || this == PARTIALLY_FULFILLED
                || this == CANCELLED;
    }

    public boolean isDerived() {
        return this == PARTIALLY_PROCESSED
                || this == FULFILLED
                || this == PARTIALLY_FULFILLED
                || this == CANCELLED;
    }
}
