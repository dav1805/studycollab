package com.project.collaboStudy.service;

import com.project.collaboStudy.dto.ProjectUpdateDTO;
import com.project.collaboStudy.model.*;
import com.project.collaboStudy.repository.JoinRequestRepository;
import com.project.collaboStudy.repository.ProjectRepository;
import com.project.collaboStudy.repository.TaskRepository;
import com.project.collaboStudy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final JoinRequestRepository joinRequestRepository;


    @Transactional
    public Project createProject(Project project) {
        // Find the authenticated user to set as the project owner
        String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User owner = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        // Assign the owner and initialize the members set with the owner
        project.setOwner(owner);
        project.getMembers().add(owner);

        // Here you can add logic to check if the owner has a valid course and goals before saving
        if (owner.getCourse() == null || owner.getGoals() == null) {
            throw new IllegalArgumentException("Owner must have a defined course and goals to create a project.");
        }

        return projectRepository.save(project);
    }

    public List<Project> findAllProjects() {
        return projectRepository.findAll();
    }

    public Project findProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found."));
    }

    @Transactional
    public Project addMemberToProject(Long projectId, Long userId) {
        Project project = findProjectById(projectId);
        User newMember = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // **Collaboration Logic Check:**
        // A user can only be added if their course and goals match the project owner's.
        if (!project.getOwner().getCourse().equals(newMember.getCourse()) ||
                !project.getOwner().getGoals().equals(newMember.getGoals())) {
            throw new IllegalArgumentException("User's course or goals do not match the project owner's.");
        }

        project.getMembers().add(newMember);
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found."));

        // **Authorization check:** Only the project owner can delete the project.
        String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        if (!project.getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You do not have permission to delete this project.");
        }

        projectRepository.delete(project);
    }

    @Transactional
    public Project updateProject(Long projectId, ProjectUpdateDTO projectDTO) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found."));

        // **Authorization check:** Only the project owner can update the project.
        String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        if (!project.getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You do not have permission to update this project.");
        }

        // Map the DTO fields to the entity
        if (projectDTO.getName() != null) {
            project.setName(projectDTO.getName());
        }
        if (projectDTO.getDescription() != null) {
            project.setDescription(projectDTO.getDescription());
        }

        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProjectStatus(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found."));

        boolean allTasksCompleted = taskRepository.findByProjectId(projectId)
                .stream()
                .allMatch(task -> task.isCompleted());

        if (allTasksCompleted) {
            project.setStatus(ProjectStatus.COMPLETED);
        } else {
            project.setStatus(ProjectStatus.ACTIVE);
        }

        return projectRepository.save(project);
    }

    @Transactional
    public void archiveProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found."));

        // **Authorization check:** Only the project owner can archive the project.
        String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        if (!project.getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You do not have permission to archive this project.");
        }

        // Change the status to ARCHIVED instead of deleting
        project.setStatus(ProjectStatus.ARCHIVED);
        projectRepository.save(project);
    }


    @Transactional
    public void requestToJoinProject(Long projectId) {
        String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found."));

        // Prevent duplicate or self-requests
        if (project.getMembers().contains(user)) {
            throw new IllegalArgumentException("You are already a member of this project.");
        }
        if (joinRequestRepository.findByProjectAndUser(project, user).isPresent()) {
            throw new IllegalArgumentException("A join request has already been sent.");
        }

        // Check if user's course and goals match the owner's
        if (!user.getCourse().equals(project.getOwner().getCourse()) || !user.getGoals().equals(project.getOwner().getGoals())) {
            throw new IllegalArgumentException("Your course or goals do not match the project owner's.");
        }

        JoinRequest request = new JoinRequest();
        request.setProject(project);
        request.setUser(user);
        joinRequestRepository.save(request);
    }

    @Transactional
    public void approveJoinRequest(Long requestId) {
        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Join request not found."));

        String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        if (!request.getProject().getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You do not have permission to approve this request.");
        }

        request.setStatus(RequestStatus.APPROVED);
        joinRequestRepository.save(request);

        // Add the user to the project's members
        request.getProject().getMembers().add(request.getUser());
        projectRepository.save(request.getProject());
    }

    @Transactional
    public void rejectJoinRequest(Long requestId) {
        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Join request not found."));

        String currentUsername = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        if (!request.getProject().getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You do not have permission to reject this request.");
        }

        request.setStatus(RequestStatus.REJECTED);
        joinRequestRepository.save(request);
    }
}