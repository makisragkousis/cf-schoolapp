package gr.aueb.cf.schoolapp.repository;

import gr.aueb.cf.schoolapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"role", "role.capabilities"})
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = {"role", "role.capabilities"})
    Optional<User> findByUuidAndDeletedFalse(UUID uuid);

    @EntityGraph(attributePaths = {"role", "capabilities"})
    Page<User> findAllByDeletedFalse(Pageable pageable);
}
