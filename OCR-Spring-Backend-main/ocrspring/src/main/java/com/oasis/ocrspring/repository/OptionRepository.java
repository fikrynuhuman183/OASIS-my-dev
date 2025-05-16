package com.oasis.ocrspring.repository;

import com.oasis.ocrspring.model.Option;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface OptionRepository extends MongoRepository<Option, String> {
    @Query("{ 'name' : { $regex: ?0, $options: 'i' } }")
    Option findByNameRegex(String name);



}
