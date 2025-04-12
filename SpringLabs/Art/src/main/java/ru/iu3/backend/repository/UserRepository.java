package ru.iu3.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.iu3.backend.entity.Artist;
import ru.iu3.backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
