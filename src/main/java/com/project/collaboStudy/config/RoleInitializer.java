package com.project.collaboStudy.config;

import com.project.collaboStudy.model.Role;
import com.project.collaboStudy.model.UserRole;
import com.project.collaboStudy.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if ROLE_MEMBER exists
        if (roleRepository.findByName(UserRole.ROLE_MEMBER).isEmpty()) {
            Role memberRole = new Role();
            memberRole.setName(UserRole.ROLE_MEMBER);
            roleRepository.save(memberRole);
            System.out.println("Created ROLE_MEMBER.");
        }

        // Check if ROLE_ADMIN exists
        if (roleRepository.findByName(UserRole.ROLE_ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(UserRole.ROLE_ADMIN);
            roleRepository.save(adminRole);
            System.out.println("Created ROLE_ADMIN.");
        }
    }
}