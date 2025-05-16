package com.oasis.ocrspring.service;

import com.oasis.ocrspring.dto.RoleDto;
import com.oasis.ocrspring.model.Role;
import com.oasis.ocrspring.repository.RoleRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest
{
    @Mock
    private RoleRepository roleRepo;

    @Mock
    private RoleDto roleDetails;

    @InjectMocks
    private RoleService roleService;

    @BeforeEach
    void setUp()
    {
        // Set up mock behavior
        when(roleDetails.getRole()).thenReturn("role");
        List<Integer> permissions = List.of(1, 2, 3);
        when(roleDetails.getPermissions()).thenReturn(permissions);

        // Create Role object
        Role role = new Role();
        role.setRole("oldRole");
        role.setPermissions(List.of(4, 5, 6));

        // Mock repository response
        when(roleRepo.findById(new ObjectId("507f1f77bcf86cd799439011"))).thenReturn(
                Optional.of(role));
    }

    @Test
    void updateRoleTest()
    {
        // Invoke service method
        roleService.updateRole("507f1f77bcf86cd799439011", roleDetails);

        // Verify interactions
        verify(roleRepo).save(any(Role.class));
        assertEquals("role",
                roleRepo.findById(new ObjectId("507f1f77bcf86cd799439011")).get()
                        .getRole());
        assertEquals(List.of(1, 2, 3),
                roleRepo.findById(new ObjectId("507f1f77bcf86cd799439011")).get()
                        .getPermissions());
    }
}
