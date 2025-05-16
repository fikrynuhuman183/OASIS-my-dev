package com.oasis.ocrspring.repository;

import com.oasis.ocrspring.model.Image;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ImageRepository extends MongoRepository<Image, String> {
    Image findById(ObjectId imageId);
    void deleteByTeleconEntryId(ObjectId teleconId);
}
