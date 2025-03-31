package com.pedestrianassistant.Util.Specification;

import com.pedestrianassistant.Model.Core.Incident;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class IncidentSpecifications {

    public static Specification<Incident> hasTitleLike(String title) {
        return (root, query, cb) -> {
            if (title == null || title.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("title")), "%" + title.trim() + "%");
        };
    }

    public static Specification<Incident> hasDescriptionLike(String description) {
        return (root, query, cb) -> {
            if (description == null || description.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("description")), "%" + description.trim() + "%");
        };
    }

    public static Specification<Incident> hasUserId(Long userId) {
        return (root, query, cb) -> userId == null ? cb.conjunction() : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Incident> hasLocation(Long locationId, String address) {
        return (root, query, cb) -> {
            if (locationId != null) {
                return cb.equal(root.get("location").get("id"), locationId);
            } else if (address != null && !address.trim().isEmpty()) {
                return cb.like(cb.lower(root.get("location").get("address")), "%" + address.trim() + "%");
            }
            return cb.conjunction();
        };
    }

    public static Specification<Incident> hasIncidentTypeId(Long incidentTypeId) {
        return (root, query, cb) -> incidentTypeId == null ? cb.conjunction() : cb.equal(root.get("incidentType").get("id"), incidentTypeId);
    }

    public static Specification<Incident> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (startDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }
            return predicate;
        };
    }
}
