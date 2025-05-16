package com.oasis.ocrspring.repository;

import com.oasis.ocrspring.model.Report;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportRepository extends MongoRepository<Report, String> {
    Report findById(ObjectId reportId);
    void deleteByTeleconId(ObjectId teleconId);
}
