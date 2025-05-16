package com.oasis.ocrspring.repository;

import com.oasis.ocrspring.model.Review;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByTeleconEntryId(ObjectId id);
}
