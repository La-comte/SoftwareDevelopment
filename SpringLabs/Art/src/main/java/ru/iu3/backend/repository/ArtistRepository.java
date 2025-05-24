package ru.iu3.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.iu3.backend.entity.Artist;
import ru.iu3.backend.entity.Country;

import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Optional<Object> findByName(String name);
}
