package com.oasis.ocrspring.service;

import com.oasis.ocrspring.dto.ErrorResponseDto;
import com.oasis.ocrspring.model.Assignment;
import com.oasis.ocrspring.repository.AssignmentRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssignmentService {
    private final AssignmentRepository assignmentRepo;

    @Autowired
    public AssignmentService(AssignmentRepository assignmentRepo) {
        this.assignmentRepo = assignmentRepo;
    }

    public List<Assignment> allAssignmentDetails() {
        return assignmentRepo.findAll();
    }

    public ResponseEntity<?> getUnreviewedEntryCount(String clinicianId){
        ObjectId clinicianObjectId = new ObjectId(clinicianId);
        try {
            long count = assignmentRepo.countByReviewerIdAndReviewedFalse(clinicianObjectId);
            Map<String,Long> response = new HashMap<>();
            response.put("count",count);
            return ResponseEntity.status(200).body(response);
        }catch (Exception e){
            return ResponseEntity.status(500).body(new ErrorResponseDto("Internal Server Error!",e.toString()));
        }
    }
}
