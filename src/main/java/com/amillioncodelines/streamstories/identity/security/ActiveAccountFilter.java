package com.amillioncodelines.streamstories.identity.security;

import com.amillioncodelines.streamstories.identity.application.AccountInactiveException;
import com.amillioncodelines.streamstories.identity.domain.AccountSnapshot;
import com.amillioncodelines.streamstories.identity.domain.ReactivationRequestRepository;
import com.amillioncodelines.streamstories.identity.domain.ReactivationStatus;
import com.amillioncodelines.streamstories.identity.domain.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Applies account changes immediately, without waiting for the access token to expire.
 *
 * <p>After the JWT is validated, the account is looked up: an inactive account is rejected with
 * {@code 403 account-inactive}, a deleted one with {@code 401}, and the authorities are taken from the current role in
 * the database, so a demotion also takes effect at once.
 */
class ActiveAccountFilter extends OncePerRequestFilter {

    private final UserRepository users;
    private final ReactivationRequestRepository reactivationRequests;
    private final HandlerExceptionResolver exceptionResolver;

    ActiveAccountFilter(
            UserRepository users,
            ReactivationRequestRepository reactivationRequests,
            HandlerExceptionResolver exceptionResolver) {
        this.users = users;
        this.reactivationRequests = reactivationRequests;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken token)) {
            chain.doFilter(request, response);
            return;
        }

        Optional<AccountSnapshot> account = parseUserId(token.getName()).flatMap(users::findSnapshotById);
        if (account.isEmpty()) {
            reject(request, response, new InvalidBearerTokenException("Unknown subject"));
            return;
        }
        if (!account.get().isActive()) {
            boolean pending =
                    reactivationRequests.existsByUserIdAndStatus(account.get().id(), ReactivationStatus.PENDING);
            reject(request, response, new AccountInactiveException(pending));
            return;
        }

        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + account.get().role().name()));
        var refreshed = new JwtAuthenticationToken(token.getToken(), authorities, token.getName());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(refreshed);
        SecurityContextHolder.setContext(context);
        chain.doFilter(request, response);
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        SecurityContextHolder.clearContext();
        exceptionResolver.resolveException(request, response, null, ex);
    }

    private static Optional<UUID> parseUserId(String subject) {
        try {
            return Optional.of(UUID.fromString(subject));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
