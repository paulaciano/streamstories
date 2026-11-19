package com.amillioncodelines.streamstories.catalog.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FranchiseRepository extends JpaRepository<Franchise, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @Query("select f from Franchise f join f.movies m where m.id = :movieId")
    Optional<Franchise> findContaining(UUID movieId);

    @Query("select distinct f from Franchise f join f.movies m where m.id in :movieIds")
    List<Franchise> findContainingAny(Collection<UUID> movieIds);
}
