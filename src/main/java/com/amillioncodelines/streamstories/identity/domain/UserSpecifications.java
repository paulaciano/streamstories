package com.amillioncodelines.streamstories.identity.domain;

import org.springframework.data.jpa.domain.Specification;

public final class UserSpecifications {

    private UserSpecifications() {}

    public static Specification<User> filter(AccountStatus status, Role role) {
        return (root, query, cb) -> cb.and(
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status),
                role == null ? cb.conjunction() : cb.equal(root.get("role"), role));
    }
}
