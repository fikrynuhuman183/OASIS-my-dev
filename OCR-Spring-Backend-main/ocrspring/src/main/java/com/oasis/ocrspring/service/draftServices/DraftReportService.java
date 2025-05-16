package com.oasis.ocrspring.service.draftServices;

import com.oasis.ocrspring.dto.ReportsRequestDto;
import com.oasis.ocrspring.dto.UploadDraftReportResponse;
import com.oasis.ocrspring.dto.UploadReportResponse;
import com.oasis.ocrspring.model.Report;
import com.oasis.ocrspring.model.draftModels.DraftEntry;
import com.oasis.ocrspring.model.draftModels.DraftReport;
import com.oasis.ocrspring.repository.draftRepos.DraftEntryRepository;
import com.oasis.ocrspring.repository.draftRepos.DraftReportRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DraftReportService {
    private final DraftReportRepository draftReportRepo;
    private final DraftEntryRepository draftEntryRepository;

    @Autowired
    public DraftReportService(DraftReportRepository draftReportRepo,
                              DraftEntryRepository draftEntryRepository) {
        this.draftReportRepo = draftReportRepo;
        this.draftEntryRepository = draftEntryRepository;
    }
    @Value("${reportUploadDir}")
    private String reportUploadDir;

    public ResponseEntity<UploadDraftReportResponse> uploadReports(ReportsRequestDto data,
                                                                   String id,
                                                                   String clinicianId,
                                                                   List<MultipartFile> files){
        List<DraftReport> uploadedReports = new ArrayList<>();//Report model list
        List<String> uploadFiles = new ArrayList<>();//List of file uri's
        List<ObjectId> reportIdObjectIdList = new ArrayList<>();

        DraftEntry draftEntry = draftEntryRepository.findById(new ObjectId(id)).orElse(null);
        if (draftEntry != null && !draftEntry.getClinicianId().toString().equals(clinicianId) && (files.size() <= 3)){
            try{
                for(MultipartFile file: files) {
                    String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
                    ResponseEntity<UploadDraftReportResponse> uploadedReport = uploadReport(data, file, fileName, uploadFiles, uploadedReports, reportIdObjectIdList);
                    if (uploadedReport != null) return uploadedReport;

                }
                //to make sure not to overwritten on the existing IDs
                List<ObjectId> reportIds = uploadedReports.stream().map(DraftReport :: getId).toList();
                List<ObjectId> existedReportIds = draftEntry.getReports();
                if(existedReportIds.isEmpty()){
                    existedReportIds = new ArrayList<>();
                }
                existedReportIds.addAll(reportIds);
                draftEntry.setReports(existedReportIds);
                draftEntry.setUpdatedAt(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
                draftEntryRepository.save(draftEntry);

                return ResponseEntity.status(200).body(new UploadDraftReportResponse(uploadedReports, "Reports Uploaded Successfully"));
            }catch (Exception e){
                return ResponseEntity.status(500).body(new UploadDraftReportResponse(null
                        ,"Internal Server Error"));
            }

        }else{
            return ResponseEntity.status(404).body(new UploadDraftReportResponse(null
                    ,"Entry Not Found"));
        }
    }

    private ResponseEntity<UploadDraftReportResponse> uploadReport(ReportsRequestDto data, MultipartFile file,
                                                                   String fileName, List<String> uploadFiles,
                                                                   List<DraftReport> uploadedReports, List<ObjectId> reportids) {
        try{
            Path path = Paths.get(reportUploadDir + File.separator+ fileName);
            if(!Files.exists(path)){//if the path doesn't exist create em
                Files.createDirectories(path);
            }
            Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);

            //useful for creating uri to check the report
            String fileDownUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/files")
                    .path(fileName)
                    .toUriString();
            uploadFiles.add(fileDownUri);

            // creating a report instance and saving it on the database
            DraftReport report = new DraftReport();
            report.setTeleconEntryId(data.getTeleconId());
            report.setReportName(data.getReportName());
            report.setCreatedAt(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            report.setUpdatedAt(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            draftReportRepo.save(report);
            uploadedReports.add(report); //Report model list
            reportids.add(report.getId());

        }
        catch(Exception ex){
            return ResponseEntity.status(500).body(new UploadDraftReportResponse(null, "Internal Server Error"));
        }
        return null;
    }
}
