package com.oasis.ocrspring.repository;

import com.oasis.ocrspring.model.RefreshToken;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshtokenRepsitory extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(ObjectId user);
}
