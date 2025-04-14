package org.example.auth.repositorys;

import org.example.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;

import java.util.Optional;

@RepositoryRestController
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
