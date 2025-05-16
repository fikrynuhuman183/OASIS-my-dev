package com.oasis.ocrspring.service;

import com.oasis.ocrspring.dto.*;
import com.oasis.ocrspring.model.*;
import com.oasis.ocrspring.repository.*;
import com.oasis.ocrspring.service.ResponseMessages.ErrorMessage;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TeleconEntriesService {
    private final TeleconEntriesRepository teleconEntriesRepo;
    private final PatientService patientService;
    private final PatientRepository patientRepo;
    private final UserRepository userRepo;
    private final AssignmentRepository assignmentRepo;
    private final ImageRepository imageRepo;
    private final ReportRepository reportRepo;
    private final MongoTemplate mongoTemplate;
    private final ReviewRepository reviewRepo;

    @Autowired
    public TeleconEntriesService(TeleconEntriesRepository teleconEntriesRepo,
                                 PatientService patientService,
                                 PatientRepository patientRepo,
                                 UserRepository userRepo,
                                 AssignmentRepository assignmentRepo,
                                 ImageRepository imageRepo,
                                 ReportRepository reportRepo,
                                 MongoTemplate mongoTemplate,
                                 ReviewRepository reviewRepo) {
        this.teleconEntriesRepo = teleconEntriesRepo;
        this.patientService = patientService;
        this.patientRepo = patientRepo;
        this.userRepo = userRepo;
        this.assignmentRepo = assignmentRepo;
        this.imageRepo = imageRepo;
        this.reportRepo = reportRepo;
        this.mongoTemplate = mongoTemplate;
        this.reviewRepo = reviewRepo;
    }

    static final String INTERNAL_SERVER_ERROR = "Internal Server Error!";
    static final String REVIEWERS = "reviewers";
    static final String ENTRY_NOT_FOUND = "Entry Not Found!";
    static final  String REVIEWED = "reviewed";

    public void save(TeleconEntry teleconEntry){
        teleconEntriesRepo.save(teleconEntry);
    }
    public ResponseEntity<?> patientTeleconEntry(String patientId,
                                                 String clinicianId,
                                                 PatientTeleconRequest newPatient ){ //path patient id

        try{
            Patient patient = patientService.findPatient(patientId,clinicianId);//patient_id, String clinician_id
            if(patient != null){
                TeleconEntry newEntry = new TeleconEntry();
                createTeleconEntry(newPatient, newEntry, patient);

                return saveTeleconEntry(newEntry);
            }
            else{
                return ResponseEntity.status(404).body(new MessageDto("Patient is not registered" ));
            }
        }
        catch(Exception e){
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
        }

    }

    private ResponseEntity<?> saveTeleconEntry(TeleconEntry newEntry) {
        try {
            teleconEntriesRepo.save(newEntry);
            return ResponseEntity.status(200).body(new PatientTeleconResponse(newEntry));
        }
        catch(Exception ex){
            return ResponseEntity.status(500).body(new MessageDto("Tele consultation entry failed"));
        }
    }

    private static void createTeleconEntry(PatientTeleconRequest newPatient, TeleconEntry newEntry, Patient patient) {
        newEntry.setPatient(patient.getId());
        newEntry.setClinicianId(patient.getClinicianId());
        newEntry.setStartTime(LocalDateTime.parse(newPatient.getStartTime()));
        newEntry.setEndTime(LocalDateTime.parse(newPatient.getEndTime()));
        newEntry.setComplaint(newPatient.getComplaint());
        newEntry.setFindings(newPatient.getFindings());
        newEntry.setCurrentHabits(newPatient.getCurrentHabits());
        newEntry.setReviewers(new ArrayList<>());
        newEntry.setReviews(new ArrayList<>());
        newEntry.setImages(new ArrayList<>());
        newEntry.setReports(new ArrayList<>());
        newEntry.setCreatedAt(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        newEntry.setUpdatedAt(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
    }

    public ResponseEntity<?> getAllUserEntries(String id, Integer page, String filter,Integer pageSize)
    {
        Pageable pageable = PageRequest.of(page-1,pageSize,Sort.by(Sort.Direction.DESC,getSortField(filter)));//page-1 cuz Page numbers in Spring Data are zero-based
        //pageSize is the number of items you want to retrieve per page
        ObjectId clinicianObjectId = new ObjectId(id);
        Page<TeleconEntry> entryPage;
        List<TeleconEntry> entryPageList ;
        try {
            switch (filter) {
                case "Assigned":
                    entryPage = teleconEntriesRepo.findByClinicianIdAndReviewersNotEmpty(clinicianObjectId, pageable);
                    break;
                case "Unassigned":
                    entryPage = teleconEntriesRepo.findByClinicianIdAndReviewersIsEmpty(clinicianObjectId, pageable);
                    break;
                case "Reviewed":
                    entryPage = teleconEntriesRepo.findByClinicianIdAndReviewsIsNotEmpty(clinicianObjectId, pageable);
                    break;
                case "Unreviewed":
                    entryPage = teleconEntriesRepo.findByClinicianIdAndReviewsIsEmpty(clinicianObjectId, pageable);
                    break;
                case "Newly Reviewed":
                    entryPage = teleconEntriesRepo.findByClinicianIdAndUpdatedTrue(clinicianObjectId, pageable);
                    break;
                default:
                    entryPage = teleconEntriesRepo.findByClinicianId(clinicianObjectId, pageable);
                    break;
            }
            entryPageList = entryPage.getContent();
            List<TeleconEntryDto> response = new ArrayList<>();

            //loop through the list to access getters and setters
            for(TeleconEntry entry: entryPageList){
                Patient patient = (patientRepo.findById(entry.getPatient()).orElse(null));
                PatientDetailsDto patientDetails =new PatientDetailsDto(patient);
                List<ObjectId> reviewerList = entry.getReviewers() ;
                List<ReviewerDetailsDto> reviewerObjectList = new ArrayList<>();
                for(ObjectId Reviewer : reviewerList){
                    User user = userRepo.findById(Reviewer).orElse(null);
                    ReviewerDetailsDto reviewerDetails = new ReviewerDetailsDto(user);
                    reviewerObjectList.add(reviewerDetails);
                }
                response.add(new TeleconEntryDto(entry,patientDetails,reviewerObjectList));
            }
            return ResponseEntity.status(200).body(response);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
        }

    }
    public ResponseEntity<?> getUserEntryById(String clinicianId,String patient,
                                              Integer page, String filter, Integer pageSize){
        Pageable pageable = PageRequest.of(page-1,pageSize,Sort.by(Sort.Direction.DESC,getSortField(filter)));
        ObjectId patientId = new ObjectId(patient);
        ObjectId clinicianObjectId = new ObjectId(clinicianId);
        Page<TeleconEntry> entryPage ;
        List<TeleconEntry> entryPageList;
        List<TeleconEntryDto> response = new ArrayList<>();
        try{
            entryPage = teleconEntriesRepo.findByPatientAndClinicianId(patientId,clinicianObjectId,pageable);
            entryPageList = entryPage.getContent();
            for(TeleconEntry entry:entryPageList){
                Patient patientProfile = patientRepo.findById(entry.getPatient()).orElse(null);
                PatientDetailsDto patientDetails = new PatientDetailsDto(patientProfile);
                List<ObjectId> reviewerIdList = entry.getReviewers();
                List<ReviewerDetailsDto> reviewerList = new ArrayList<>();
                for (ObjectId reviewer: reviewerIdList){
                    User reviewerObject = userRepo.findById(reviewer).orElse(null);
                    reviewerList.add(new ReviewerDetailsDto(reviewerObject));
                }
                response.add(new TeleconEntryDto(entry,patientDetails,reviewerList));
            }
            return  ResponseEntity.status(200).body(response);
        } catch(Exception e){
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
        }
    }

    public ResponseEntity<?> getSharedPatient(String clinicianId, String patient,String filter,Integer pageSize,Integer page){

        Pageable pageable = PageRequest.of(page-1, pageSize, Sort.by( Sort.Direction.DESC, getSortField(filter)));
        ObjectId patientId = new ObjectId(patient);
        ObjectId clinicianObjectId = new ObjectId(clinicianId);
        List<TeleconEntry> entryList = new ArrayList<>();
        List<TeleconEntryDto> results = new ArrayList<>();

        try {
            Optional<TeleconEntry> entry = teleconEntriesRepo.findByPatientAndReviewersIn(patientId, clinicianObjectId); //List Can be used as well
            if (entry.isEmpty()) {
                return ResponseEntity.status(404).body(new ErrorMessage("Entries Not Found"));
            }
            Page<TeleconEntry> entries = teleconEntriesRepo.findByPatient(patientId, pageable);
            entryList = entries.getContent();
            for(TeleconEntry element: entryList){
                Patient newPatient = patientRepo.findById(element.getPatient()).orElse(null);
                PatientDetailsDto patientDetails = new PatientDetailsDto(newPatient);
                List<ObjectId> reviewerList = element.getReviewers();
                List<ReviewerDetailsDto> reviewerDetails = new ArrayList<>();
                setReviewerDetails(reviewerList, reviewerDetails);
                results.add(new TeleconEntryDto(element,patientDetails,reviewerDetails));
            }
            return ResponseEntity.status(200).body(results);
        }catch(NullPointerException er){
            return ResponseEntity.status(404).body(new ErrorMessage("Entries Not Found"));
        } catch (Exception e ){
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
        }


    }
    public ResponseEntity<?> getEntryDetails(String clinicianId,String id){
        ObjectId clinicinObjectId = new ObjectId(clinicianId);
        ObjectId teleconId = new ObjectId(id);
        try{
            Optional<TeleconEntry> entry = teleconEntriesRepo.findByIdAndClinicianId(teleconId,clinicinObjectId);
            if (entry.isPresent()) {
                TeleconEntry entryDetails = entry.get();
                Patient patient = patientRepo.findById(entryDetails.getPatient()).orElse(null);
                PatientDetailsDto patientDetails = new PatientDetailsDto(patient);

                List<ObjectId> reviewers = entryDetails.getReviewers();
                List<ReviewerDetailsDto> reviewerDetailList = new ArrayList<>();
                List<Image> imageList = new ArrayList<>();
                List<Report> reportList = new ArrayList<>();

                setReviewerDetails(reviewers, reviewerDetailList);
                setImageDetails(entryDetails.getImages(),imageList);
                setReportDetails(entryDetails.getReports(),reportList);

                PopulatedResultDto teleconDetails = new PopulatedResultDto(entryDetails, patientDetails, reviewerDetailList,imageList,reportList);
                return ResponseEntity.status(200).body(teleconDetails);
            }else {
                return ResponseEntity.status(404).body(new ErrorMessage("Entry not found"));
            }
        }catch(Exception e){
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
        }
    }
    public ResponseEntity<?> countNewReviews(String clinicianId){
        ObjectId clinicianObjectId = new ObjectId(clinicianId);
        try{
            long count = teleconEntriesRepo.countByClinicianIdAndUpdatedTrue(clinicianObjectId);
            Map<String,Long> response = new HashMap<>();
            response.put("count",count);
            return ResponseEntity.status(200).body(response);
        }catch (Exception e){
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
        }

    }

    public ResponseEntity<?> addReviewer(String clinicianId, String id, String reviewerId){

        ObjectId clinicianObjectId = new ObjectId(clinicianId);
        ObjectId teleconId = new ObjectId(id);
        ObjectId reviewerObjectId = new ObjectId(reviewerId);

        Optional<TeleconEntry> entry;
        TeleconEntry entryElement;
        List<ObjectId> reviewers;
        try {
            entry = teleconEntriesRepo.findByIdAndClinicianId(teleconId, clinicianObjectId);
            if(entry.isEmpty()){
                return ResponseEntity.status(404).body(new ErrorMessage("Entry not found"));
            }
            entryElement = entry.get();
            reviewers = entryElement.getReviewers();
            if (entryElement.getReviewers().contains(reviewerObjectId)){

                TeleconEntry teleconEntry = teleconEntriesRepo.findByIdAndClinicianId(teleconId,clinicianObjectId).orElse(null);
                if (teleconEntry == null) {
                    throw new NullPointerException("TeleconEntry is null");
                }
                List<ObjectId> reviewersIdList = teleconEntry.getReviewers();
                List<ObjectId> imageIdList = teleconEntry.getImages();
                List<ObjectId> reportIdList = teleconEntry.getReports();

                Patient patient = patientRepo.findById(teleconEntry.getPatient()).orElse(null);
                PatientDetailsDto patientDetails = new PatientDetailsDto(patient);

                List<ReviewerDetailsDto> reviewerDetails = new ArrayList<>();
                List<Image> imageList = new ArrayList<>();
                List<Report> reportList = new ArrayList<>();

                setReviewerDetails(reviewersIdList, reviewerDetails);
                setImageDetails(imageIdList, imageList);
                setReportDetails(reportIdList, reportList);

                PopulatedResultDto updatedEntry = new PopulatedResultDto(teleconEntry, patientDetails, reviewerDetails, imageList, reportList);
                return ResponseEntity.status(200).body(updatedEntry);
            }
            createAssignment(teleconId, reviewerObjectId);

            reviewers.add(reviewerObjectId);
            entryElement.setReviewers(reviewers);
            teleconEntriesRepo.save(entryElement);
            return ResponseEntity.status(200).body(new MessageDto("Reviewer is added"));

        } catch (NullPointerException e) {
            return ResponseEntity.status(500).body(new ErrorResponseDto("TeleconEntry is null", e.toString()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR, e.toString()));
        }
    }

    public ResponseEntity<?> removeReviewer(String clinicianId,String id, String reviewerId){

        ObjectId clinicianObjectId = new ObjectId(clinicianId);
        ObjectId teleconId = new ObjectId(id);
        ObjectId reviewerObjectId = new ObjectId(reviewerId);

        Optional<TeleconEntry> entry;
        TeleconEntry entryElement;
        try{
            entry = teleconEntriesRepo.findByIdAndClinicianId(teleconId,clinicianObjectId);
            if (entry.isEmpty()){
                return ResponseEntity.status(404).body(new MessageDto("Entry is not found"));
            }
            entryElement = entry.get();
            assignmentRepo.deleteByTeleconEntryAndReviewerId(teleconId,reviewerObjectId);
            pullReviewerFromEntry(teleconId,reviewerObjectId);
            return ResponseEntity.status(200).body(new MessageDto("Reviewer is removed"));
        }catch(Exception err){
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,err.toString()));
        }
    }
    public ResponseEntity<?> deleteEntry(String clinicianId,String id){
        ObjectId clinicianObjectId = new ObjectId(clinicianId);
        ObjectId teleconObjectId = new ObjectId(id);
        try{
            Optional<TeleconEntry> entry = teleconEntriesRepo.findByIdAndClinicianId(teleconObjectId,clinicianObjectId);

            if (entry.isPresent()){
                TeleconEntry entryObject = entry.get();
                final LocalDateTime now = LocalDateTime.now();
                final LocalDateTime createdAt = entryObject.getCreatedAt();
                final Duration duration = Duration.between(now,createdAt) ;
                final float hours = duration.toMinutes()/60f;
                if(hours >= 24){
                    return ResponseEntity.status(401).body(new ErrorMessage("Unauthorized access"));
                }
                assignmentRepo.deleteByTeleconEntry(teleconObjectId);
                imageRepo.deleteByTeleconEntryId(teleconObjectId);
                reportRepo.deleteByTeleconId(teleconObjectId);
                teleconEntriesRepo.deleteById(teleconObjectId);
                return ResponseEntity.status(200).body(new MessageDto("Entry is deleted successfully"));
            }
            else {
                return ResponseEntity.status(404).body(new MessageDto(ENTRY_NOT_FOUND));
            }
        }catch (Exception err){
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,err.toString()));
        }
    }

    public ResponseEntity<?> getAllSharedEntries(int page, int pageSize, String clinicianId,String filter) {
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("reviewer_id", clinicianId);
        if (filter != null && !filter.equals("All")) {
            filterMap.put(REVIEWED, filter.equals("Reviewed"));
        }
        try {
            Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Assignment> assignments;
            if (filterMap.containsKey(REVIEWED)) {
                assignments = assignmentRepo.findByReviewerIdAndReviewed((ObjectId) filterMap.get("reviewer_id"),
                        (Boolean) filterMap.get(REVIEWED), pageable);
            } else {
                assignments = assignmentRepo.findByReviewerId(new ObjectId(clinicianId), pageable);
            }

            List<SharedEntriesDto> results = new ArrayList<>();

            if (!assignments.isEmpty()) {
                for (Assignment assignment : assignments) {
                    ObjectId teleconId = assignment.getTeleconEntry();
                    TeleconEntry teleconEntry = teleconEntriesRepo.findById(teleconId).orElse(null);
                    if (teleconEntry == null) {
                        results.add(new SharedEntriesDto(null, null, null, assignment));
                    }
                    else {
                        ObjectId patientId = teleconEntry.getPatient();
                        ObjectId clicianId = teleconEntry.getClinicianId();
                        Patient patient = patientRepo.findById(patientId).orElse(null);
                        User clinician = userRepo.findById(clicianId).orElse(null);
                        SharedEntriesDto result = new SharedEntriesDto(teleconEntry, patient, clinician, assignment);
                        results.add(result);

                    }
                }

            }
            return ResponseEntity.status(200).body(results);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
        }
    }

    

    public ResponseEntity<?> getSharedEntry(String id, String clinicianId){
        ObjectId teleconId = new ObjectId(id);
        ObjectId clinicianObjectId = new ObjectId(clinicianId);
        try{
            TeleconEntry entry = teleconEntriesRepo.findByIdAndReviewersContaining(teleconId, clinicianObjectId).orElse(null);
            if (entry == null){
                return ResponseEntity.status(404).body(new MessageDto(ENTRY_NOT_FOUND));
            }
            Patient patient = patientRepo.findById(entry.getPatient()).orElse(null);
            List<ObjectId> reviewerList = entry.getReviewers();
            List<ObjectId> imageIdList = entry.getImages();
            List<ObjectId> reportIdList = entry.getReports();

            List<ReviewerDetailsDto_> reviewerDetails = new ArrayList<>();
            List<Image> imageDetailsList = new ArrayList<>();
            List<Report> reportDetailsList = new ArrayList<>();

            setReviewers(reviewerList, reviewerDetails);
            setImageDetails( imageIdList, imageDetailsList);
            setReportDetails(reportIdList, reportDetailsList);
            PatientDetailsDto patientDetails = new PatientDetailsDto(patient);

            PopulatedEntryDto result = new PopulatedEntryDto(entry,patientDetails,reviewerDetails,imageDetailsList,reportDetailsList);
            return ResponseEntity.status(200).body(result);
        }catch(Exception e){
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
        }

    }

    public ResponseEntity<?> getAssignedEntryDetails(String id){
        ObjectId assignmentId = new ObjectId(id);
        try{
            Assignment assignment = assignmentRepo.findById(assignmentId).orElse(null);
            AssignedEntryDetailsDto result = new AssignedEntryDetailsDto();
            if (assignment != null){
                TeleconEntry entry = teleconEntriesRepo.findById(assignment.getTeleconEntry()).orElse(null);
                if(entry != null){
                    List<ObjectId> imageIdList = entry.getImages();
                    List<ObjectId> reportIdList = entry.getReports();

                    List<Image> imageDetailsList = new ArrayList<>();
                    List<Report> reportDetailsList = new ArrayList<>();

                    setImageDetails( imageIdList, imageDetailsList);
                    setReportDetails(reportIdList, reportDetailsList);
                    Patient patient = patientRepo.findById(entry.getPatient()).orElse(null);
                    User clinician = userRepo.findById(entry.getClinicianId()).orElse(null);
                    PatientDetailsDto patientDetails = new PatientDetailsDto(patient);
                    ClinicianDetailsDto clinicianDetails = new ClinicianDetailsDto(clinician);

                    result = new AssignedEntryDetailsDto(entry, patientDetails, clinicianDetails,
                            imageDetailsList, reportDetailsList);

                    result.setAssignedAt(assignment.getCreatedAt());
                    result.setReviewed(assignment.getReviewed());
                    result.setChecked(assignment.getChecked());
                }
                return ResponseEntity.status(200).body(result);
            }else {
                return ResponseEntity.status(404).body(new MessageDto(ENTRY_NOT_FOUND));
            }

        }catch(Exception e){
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
        }
    }

    public ResponseEntity<?> getEntryReviews(String id){
        ObjectId teleconId = new ObjectId(id);
        try{
            List<Review> reviews = reviewRepo.findByTeleconEntryId(teleconId);
            List<ReviewDetailsDto> reviewDetails = new ArrayList<>();
            if (!reviews.isEmpty()){
                for(Review review: reviews){
                    User reviewer = userRepo.findById(review.getReviewerId()).orElse(null);
                    reviewDetails.add(new ReviewDetailsDto(review,reviewer));
                }
                return ResponseEntity.status(200).body(reviewDetails);
            }else {
                return ResponseEntity.status(404).body(new MessageDto(ENTRY_NOT_FOUND));
            }
        }catch(Exception e){
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
        }
    }

    public ResponseEntity<?> changeReviewer(String id, String clinicianId,String reviewerId){
        ObjectId assignmentId = new ObjectId(id);
        ObjectId clinicianObjectId = new ObjectId(clinicianId);
        ObjectId reviewerObjectId = new ObjectId(reviewerId);
        Assignment assignment = assignmentRepo.findByIdAndReviewerId(assignmentId, clinicianObjectId).orElse(null);
        if(assignment != null){
            try {
                Assignment exists = assignmentRepo.findByReviewerIdAndTeleconEntry(reviewerObjectId,assignment.getTeleconEntry()).orElse(null);
                if (exists == null){
                    Assignment newAssignment = new Assignment();
                    newAssignment.setTeleconEntry(assignment.getTeleconEntry());
                    newAssignment.setReviewerId(reviewerObjectId);
                    newAssignment.setReviewed(false);
                    newAssignment.setChecked(false);

                    assignmentRepo.save(newAssignment);
                    //updating teleconEntry
                    pushReviewerToEntry(assignment.getTeleconEntry(), reviewerObjectId);
                }
                assignmentRepo.deleteByIdAndReviewerId(assignmentId,clinicianObjectId);
                pullReviewerFromEntry(assignment.getTeleconEntry(), clinicianObjectId);

                return ResponseEntity.status(200).body(new MessageDto("Reviewer assigned successfully"));
            }catch(Exception e){
                return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
            }
        }else{
            return ResponseEntity.status(404).body(new MessageDto(ENTRY_NOT_FOUND));
        }
    }

    public ResponseEntity<?> addReview(String clinicianId, String teleconId,
                                       ReviewRequestDto reviewDetails){
        ObjectId clinicianObjectId = new ObjectId(clinicianId);
        ObjectId teleconObjectId = new ObjectId(teleconId);

        try{
            TeleconEntry teleconEntry = teleconEntriesRepo.findById(teleconObjectId).orElse(null);
            if (teleconEntry != null && teleconEntry.getReviewers().contains(clinicianObjectId)){
                Review newReview = new Review();
                newReview.setTeleconEntryId(teleconObjectId);
                newReview.setReviewerId(clinicianObjectId);
                newReview.setProvisionalDiagnosis(reviewDetails.getProvisionalDiagnosis());
                newReview.setManagementSuggestions(reviewDetails.getManagementSuggestions());
                newReview.setReferralSuggestions(reviewDetails.getReferralSuggestions());
                newReview.setOtherComments(reviewDetails.getOtherComments());

                Assignment assignment = assignmentRepo.findByReviewerIdAndTeleconEntry(clinicianObjectId, teleconObjectId).orElse(null);
                if (assignment == null) {
                    return ResponseEntity.status(404).body(new MessageDto("Assignment not found"));
                }
                assignment.setReviewed(true);
                assignmentRepo.save(assignment);
                teleconEntry.setUpdated(true);
                teleconEntriesRepo.save(teleconEntry);

                reviewRepo.save(newReview);
                teleconEntry.getReviews().add(String.valueOf(newReview.getId()));
                teleconEntriesRepo.save(teleconEntry);

                return ResponseEntity.status(200).body(Map.of( "message", "Review added successfully","docs", newReview));

            } else {
                return ResponseEntity.status(404).body(new MessageDto(ENTRY_NOT_FOUND));
            }
        } catch (NullPointerException e) {
            return ResponseEntity.status(500).body(new ErrorResponseDto("Null Pointer Exception", e.toString()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR, e.toString()));
        }
    }

    public ResponseEntity<?> markAsRead(String id ){
        ObjectId assignmentObjectId = new ObjectId(id);
        Assignment assignment = assignmentRepo.findById(assignmentObjectId).orElse(null);
        try{
            if (assignment == null) {
                return ResponseEntity.status(404).body(new ErrorMessage("Assignment not found"));
            }
            assignment.setChecked(true);
            assignmentRepo.save(assignment);
            return ResponseEntity.status(200).body(new MessageDto("marked as read"));
        } catch(Exception e){
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR,e.toString()));
        }
    }

    public ResponseEntity<?> markAsOpen(String id ){
        ObjectId teleconObjectId = new ObjectId(id);
        TeleconEntry teleconEntry = teleconEntriesRepo.findById(teleconObjectId).orElse(null);
        try {
            if (teleconEntry == null) {
                return ResponseEntity.status(404).body(new MessageDto("TeleconEntry not found"));
            }
            teleconEntry.setUpdated(false);
            teleconEntriesRepo.save(teleconEntry);
            return ResponseEntity.status(200).body(new MessageDto("marked as read"));
        } catch (NullPointerException e) {
            return ResponseEntity.status(500).body(new ErrorResponseDto("Null Pointer Exception", e.toString()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponseDto(INTERNAL_SERVER_ERROR, e.toString()));
        }
    }

    public void pullReviewFromEntry(ObjectId teleconId, List<ObjectId> reviewers){
        Query query = new Query().addCriteria(Criteria.where("_id").is(teleconId)) ;
        Update update = new Update().pullAll(REVIEWERS, reviewers.toArray());
        mongoTemplate.updateFirst(query,update,TeleconEntry.class);
    }

    public void pullReviewerFromEntry(ObjectId teleconId, ObjectId reviewer){
        Query query = new Query().addCriteria(Criteria.where("_id").is(teleconId)) ;
        Update update = new Update().pull(REVIEWERS,reviewer);
        mongoTemplate.updateFirst(query,update,TeleconEntry.class);
    }

    public void pushReviewerToEntry(ObjectId teleconId, ObjectId reviewer){
        Query query = new Query().addCriteria(Criteria.where("_id").is(teleconId)) ;
        Update update = new Update().push(REVIEWERS,reviewer);
        mongoTemplate.updateFirst(query,update,TeleconEntry.class);
    }

    private void createAssignment(ObjectId teleconId, ObjectId clinicianObjectId) {
        Assignment newAssignement = new Assignment();
        newAssignement.setTeleconEntry(teleconId);
        newAssignement.setReviewerId(clinicianObjectId);
        newAssignement.setChecked(Boolean.FALSE);
        newAssignement.setReviewed(Boolean.FALSE);
        assignmentRepo.save(newAssignement);
    }

    private void setReportDetails(List<ObjectId> reportIdList, List<Report> reportList) {
        if(!reportIdList.isEmpty()){
            for (ObjectId report: reportIdList){
                Report reportObject = reportRepo.findById(report);
                reportList.add(reportObject);
            }
        }
    }

    private void setImageDetails(List<ObjectId> imageIdList, List<Image> imageList) {
        if(!imageIdList.isEmpty()){
            for (ObjectId imageId: imageIdList){
                Image imageObject = imageRepo.findById(imageId);
                imageList.add(imageObject);
            }
        }
    }

    private void setReviewerDetails(List<ObjectId> reviewersIdList, List<ReviewerDetailsDto> reviewerDetails) {
        if(!reviewersIdList.isEmpty()){
            for (ObjectId reviewer: reviewersIdList){
                User reviewerObject = userRepo.findById(reviewer).orElse(null);
                reviewerDetails.add(new ReviewerDetailsDto(reviewerObject));
            }
        }
    }
    private void setReviewers(List<ObjectId> reviwerIdList,List<ReviewerDetailsDto_> reviewerDetails){
        if (!reviwerIdList.isEmpty()){
            for(ObjectId reviewer: reviwerIdList){
                User reviewerObject = userRepo.findById(reviewer).orElse(null);
                reviewerDetails.add(new ReviewerDetailsDto_(reviewerObject));
            }
        }
    }

    private String getSortField(String filter){
        if(filter.equals("Updated At")){
            return "updatedAt";
        }
        return "createdAt";
    }
}

