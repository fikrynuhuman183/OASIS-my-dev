package com.oasis.ocrspring.repository.draftRepos;

import com.oasis.ocrspring.model.draftModels.DraftEntry;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DraftEntryRepository extends MongoRepository<DraftEntry, String> {
    Page<DraftEntry> findByClinicianId(ObjectId clinicianId, Pageable pageable);
    Optional<DraftEntry> findByClinicianIdAndId(ObjectId clinicianId, ObjectId id);

    Optional<DraftEntry> findById(ObjectId id);
}
