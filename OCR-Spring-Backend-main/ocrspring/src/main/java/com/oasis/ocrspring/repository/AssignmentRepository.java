package com.oasis.ocrspring.repository;

import com.oasis.ocrspring.model.Assignment;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AssignmentRepository extends MongoRepository<Assignment, String> {
    long countByReviewerIdAndReviewedFalse(ObjectId clinicianId);
    void deleteByTeleconEntryAndReviewerId(ObjectId teleconEntry, ObjectId reviewerId);
    void deleteByTeleconEntry(ObjectId teleconId);
    Page<Assignment> findByReviewerIdAndReviewed(ObjectId reviewerId, boolean reviewed, Pageable pageable);
    Page<Assignment> findByReviewerId(ObjectId reviewerId,Pageable pageable);
    Optional<Assignment> findById(ObjectId id);
    Optional<Assignment> findByIdAndReviewerId(ObjectId id, ObjectId reviwerId);
    Optional<Assignment> findByReviewerIdAndTeleconEntry(ObjectId reviewerId, ObjectId teleconId);
    void deleteByIdAndReviewerId(ObjectId id, ObjectId reviewerId);
}
