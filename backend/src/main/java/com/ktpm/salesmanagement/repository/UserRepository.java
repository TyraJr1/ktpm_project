package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    @Query("""
            select u from User u
            where (:query is null
                or lower(u.username) like lower(concat('%', :query, '%'))
                or lower(u.fullName) like lower(concat('%', :query, '%')))
              and (:role is null or u.role = :role)
              and (:status is null or u.status = :status)
            """)
    Page<User> search(@Param("query") String query,
                      @Param("role") String role,
                      @Param("status") String status,
                      Pageable pageable);
}

