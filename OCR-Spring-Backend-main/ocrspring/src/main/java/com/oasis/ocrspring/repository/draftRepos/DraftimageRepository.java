package com.oasis.ocrspring.repository.draftRepos;

import com.oasis.ocrspring.model.draftModels.DraftImage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DraftimageRepository extends MongoRepository<DraftImage, String> {
}
