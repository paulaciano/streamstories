package com.amillioncodelines.streamstories.identity.api;

import jakarta.validation.constraints.NotBlank;

record CredentialsRequest(@NotBlank String email, @NotBlank String password) {}
