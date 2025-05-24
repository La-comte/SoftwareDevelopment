package ru.iu3.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.iu3.backend.entity.Country;
import ru.iu3.backend.entity.Museum;

import java.util.Optional;

@Repository
public interface MuseumRepository extends JpaRepository<Museum, Long> {
    Optional<Object> findByName(String name);
}
