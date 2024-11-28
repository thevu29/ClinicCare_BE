package com.example.cliniccare.repository;

import com.example.cliniccare.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    default Page<User> findByDeleteAtIsNullAndSearchParamsAndRoleParams(
            List<String> searchParams,
            List<String> roleParams,
            String search,
            UUID role,
            Pageable pageable
    ) {
        return findAll(
                (root, query, criteriaBuilder) -> {
                    Predicate predicate = criteriaBuilder.isNull(root.get("deleteAt"));

                    if (!searchParams.isEmpty()) {
                        Predicate searchPredicate = criteriaBuilder.or(
                                searchParams.stream()
                                        .map(param ->
                                                criteriaBuilder.like(root.get(param), "%" + search + "%"))
                                        .toArray(Predicate[]::new)
                        );
                        predicate = criteriaBuilder.and(predicate, searchPredicate);
                    }

                    if (!roleParams.isEmpty()) {
                        predicate = criteriaBuilder.and(
                                predicate,
                                criteriaBuilder.equal(root.get("role").get("roleId"), role));
                    }

                    return predicate;
                },
                pageable
        );
    }
    List<User> findAllByDeleteAtIsNullAndRole_Name(String roleName);
    Optional<User> findByUserIdAndDeleteAtIsNull(UUID userId);
    Optional<User> findByEmailAndDeleteAtIsNull(String email);
    Boolean existsByEmailAndDeleteAtIsNull(String email);
    List<User> findByRoleNameAndDeleteAtIsNull(String role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = 'User' AND u.deleteAt IS NULL AND u.doctorProfile IS NULL " +
            "AND MONTH(u.createAt) = :month AND YEAR(u.createAt) = :year")
    Long countUsersWithUserRoleOnlyByMonth(@Param("month") int month, @Param("year") int year);
}
