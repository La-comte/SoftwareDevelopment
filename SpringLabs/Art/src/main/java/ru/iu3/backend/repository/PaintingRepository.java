package ru.iu3.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.iu3.backend.entity.Museum;
import ru.iu3.backend.entity.Painting;

@Repository
public interface PaintingRepository extends JpaRepository<Painting, Long> {

}
