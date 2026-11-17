package com.amillioncodelines.streamstories.catalog.domain;

/** Whether content is offered. Retired content is kept with all its activity instead of being deleted (ADR-0009). */
public enum Availability {
    AVAILABLE,
    RETIRED
}
