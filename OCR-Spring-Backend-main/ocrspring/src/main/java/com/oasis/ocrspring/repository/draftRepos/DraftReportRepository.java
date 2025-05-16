package com.oasis.ocrspring.repository.draftRepos;

import com.oasis.ocrspring.model.draftModels.DraftReport;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DraftReportRepository extends MongoRepository<DraftReport, String> {
}
