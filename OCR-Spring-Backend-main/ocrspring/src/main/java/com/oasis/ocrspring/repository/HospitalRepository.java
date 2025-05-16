package com.oasis.ocrspring.repository;

import com.oasis.ocrspring.model.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface HospitalRepository extends MongoRepository<Hospital, String> {
    Optional<Hospital> findByName(String name);
}
