package com.oasis.ocrspring.service;

import com.oasis.ocrspring.model.Role;
import com.oasis.ocrspring.model.User;
import com.oasis.ocrspring.repository.RoleRepository;
import com.oasis.ocrspring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ReviewService {

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;

    @Autowired
    public ReviewService( RoleRepository roleRepo, UserRepository userRepo) {

        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
    }

    public List<User> getAllReviewers() {
        List<String> roles = roleRepo.findByPermissionsIn(Collections.singletonList(200))
                .stream()
                .map(Role::getRole)
                .toList();
        return userRepo.findByRoleInAndAvailabilityTrue(roles);
    }
}
