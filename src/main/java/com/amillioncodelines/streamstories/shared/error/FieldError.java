package com.amillioncodelines.streamstories.shared.error;

/** A single validation failure, exposed in the {@code errors} property of a validation problem. */
public record FieldError(String field, String message) {}
