
package ru.iu3.Art.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.iu3.Art.models.Country;

@Repository
public interface CountryRepository  extends JpaRepository<Country, Long>
{

}