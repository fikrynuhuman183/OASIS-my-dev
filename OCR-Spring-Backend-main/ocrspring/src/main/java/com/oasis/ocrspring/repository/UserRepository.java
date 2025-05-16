package com.oasis.ocrspring.repository;

import com.oasis.ocrspring.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByRegNo(String regNo);

    Optional<User> findByEmail(String email);

    Optional<User> findById(ObjectId id);
    List<User> findByRoleInAndAvailabilityTrue(List<String> roles);
    List<User> findByRole(String role);
    Optional<User> findByUsername(String userName);


}
