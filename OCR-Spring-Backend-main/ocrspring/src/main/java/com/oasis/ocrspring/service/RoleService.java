package com.oasis.ocrspring.service;

import com.oasis.ocrspring.dto.RoleDto;
import com.oasis.ocrspring.dto.RoleReqDto;
import com.oasis.ocrspring.model.Role;
import com.oasis.ocrspring.repository.RoleRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roleRepo;

    @Autowired
    public RoleService(RoleRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    public List<Role> allRoleDetails() {
        return roleRepo.findAll();
    }

    public Role createRole(Role role) {
        return roleRepo.save(role);
    }

    public Optional<Role> getRoleByrole(String role) {
        return roleRepo.findByRole(role);
    }
    public Optional<Role> getRoleById(String id) {
        return roleRepo.findById( new ObjectId(id));
    }
    public boolean addRole(RoleReqDto role)  {
        Optional<Role> existingRole = roleRepo.findByRoleIgnoreCase(role.getRole());
        if (existingRole.isPresent()) {
            return false;
        }

        roleRepo.save(new Role(role.getRole(), role.getPermissions()));
        return true;
    }
    public void updateRole(String id, RoleDto roleDetails) {
        Role role = roleRepo.findById(new ObjectId(id)).orElseThrow(() -> new RuntimeException("Role not found"));
        role.setRole(roleDetails.getRole());
        role.setPermissions(roleDetails.getPermissions());
        roleRepo.save(role);
    }

}

