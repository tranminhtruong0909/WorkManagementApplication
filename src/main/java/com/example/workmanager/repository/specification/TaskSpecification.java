package com.example.workmanager.repository.specification;

import com.example.workmanager.model.Task;
import com.example.workmanager.model.TaskStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {

    public static Specification<Task> searchTasks(
            String name,
            TaskStatus status,
            Integer groupId,
            Integer assigneeId,
            LocalDate searchDate,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                ));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (groupId != null) {
                predicates.add(criteriaBuilder.equal(root.get("group").get("id"), groupId));
            }

            if (assigneeId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("assignees", JoinType.INNER).get("id"),
                        assigneeId
                ));
            }

            if (searchDate != null) {
                Predicate timelinePredicate = criteriaBuilder.and(
                        criteriaBuilder.lessThanOrEqualTo(root.get("timelineStart"), searchDate),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("timelineEnd"), searchDate)
                );

                Predicate dueDatePredicate = criteriaBuilder.and(
                        criteriaBuilder.isNotNull(root.get("dueDate")),
                        criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), searchDate),
                        criteriaBuilder.notEqual(root.get("status"), TaskStatus.DONE)
                );

                Predicate noTimelinePredicate = criteriaBuilder.and(
                        criteriaBuilder.isNull(root.get("timelineStart")),
                        criteriaBuilder.isNull(root.get("timelineEnd")),
                        criteriaBuilder.or(
                                criteriaBuilder.isNull(root.get("dueDate")),
                                criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), searchDate)
                        ),
                        criteriaBuilder.notEqual(root.get("status"), TaskStatus.DONE)
                );

                predicates.add(criteriaBuilder.or(timelinePredicate, dueDatePredicate, noTimelinePredicate));
            }

            if (startDate != null && endDate != null) {
                Predicate timelineRangePredicate = criteriaBuilder.and(
                        criteriaBuilder.isNotNull(root.get("timelineStart")),
                        criteriaBuilder.lessThanOrEqualTo(root.get("timelineStart"), endDate),
                        criteriaBuilder.or(
                                criteriaBuilder.isNull(root.get("timelineEnd")),
                                criteriaBuilder.greaterThanOrEqualTo(root.get("timelineEnd"), startDate)
                        )
                );

                Predicate dueDateRangePredicate = criteriaBuilder.and(
                        criteriaBuilder.isNotNull(root.get("dueDate")),
                        criteriaBuilder.between(root.get("dueDate"), startDate, endDate)
                );

                predicates.add(criteriaBuilder.or(timelineRangePredicate, dueDateRangePredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
