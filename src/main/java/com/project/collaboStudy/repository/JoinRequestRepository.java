package com.project.collaboStudy.repository;

import com.project.collaboStudy.model.JoinRequest;
import com.project.collaboStudy.model.Project;
import com.project.collaboStudy.model.RequestStatus;
import com.project.collaboStudy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    Optional<JoinRequest> findByProjectAndUser(Project project, User user);
    List<JoinRequest> findByProjectAndStatus(Project project, RequestStatus status);
}