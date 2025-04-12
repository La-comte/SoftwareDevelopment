package ru.iu3.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.iu3.backend.entity.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
}