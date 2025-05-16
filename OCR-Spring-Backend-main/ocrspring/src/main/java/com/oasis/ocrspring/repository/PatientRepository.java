package com.oasis.ocrspring.repository;

import com.oasis.ocrspring.dto.RiskFactorAggregationResult;
import com.oasis.ocrspring.model.Patient;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PatientRepository extends MongoRepository<Patient, String> {
    Optional<Patient> findByPatientIdAndClinicianId(String patientId,
                                                    ObjectId clinicianId);

    Optional<Patient> findByIdAndClinicianId(ObjectId id, ObjectId clinicianId);

    Optional<Patient> findById(ObjectId id);

    @Query("{ 'clinicianId': ?0, $or: [ { 'patientId': { $regex: ?1, $options: 'i' } }, { 'patientName': { $regex: ?1, $options: 'i' } }, { 'gender': { $regex: ?1, $options: 'i' } } ] }")
    List<Patient> findByClinicianIdAndSearch(ObjectId clinicianId, String search, Pageable pageable);
    List<Patient> findByClinicianId(ObjectId clinicianId, Pageable pageable);


}
