package com.oasis.ocrspring.service;

import com.oasis.ocrspring.dto.ReportsRequestDto;
import com.oasis.ocrspring.dto.UploadReportResponse;
import com.oasis.ocrspring.model.Report;
import com.oasis.ocrspring.model.TeleconEntry;
import com.oasis.ocrspring.repository.ReportRepository;
import com.oasis.ocrspring.repository.TeleconEntriesRepository;
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
public class ReportService {private final ReportRepository reportRepo;
    private final TeleconEntriesService teleconServ;
    private final TeleconEntriesRepository teleconRepo;
    private final String reportUploadDir;

    @Autowired
    public ReportService(ReportRepository reportRepo,
                         TeleconEntriesService teleconServ,
                         TeleconEntriesRepository teleconRepo,
                         @Value("${reportUploadDir}") String reportUploadDir) {
        this.reportRepo = reportRepo;
        this.teleconServ = teleconServ;
        this.teleconRepo = teleconRepo;
        this.reportUploadDir = reportUploadDir;
    }

    public List<Report> allReportDetails(){
        return reportRepo.findAll();
    }
    public ResponseEntity<UploadReportResponse> uploadReports(ReportsRequestDto data,
                                                              String id,
                                                              String clinicianId,
                                                              List<MultipartFile> files){
        List<Report> uploadedReports = new ArrayList<>();//Report model list
        List<String> uploadFiles = new ArrayList<>();//List of file uri's
        List<ObjectId> reportIdList = new ArrayList<>();

        TeleconEntry teleconEntry = teleconRepo.findById(new ObjectId(id)).orElse(null);
        if (teleconEntry != null && !teleconEntry.getClinicianId().toString().equals(clinicianId) && (files.size() <= 3)){
            try{
                for(MultipartFile file: files) {
                    String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
                    ResponseEntity<UploadReportResponse> uploadedReport = uploadReport(data, file, fileName, uploadFiles, uploadedReports, reportIdList);
                    if (uploadedReport != null) return uploadedReport;

                }
                //to make sure not to overwritten on the existing IDs
                List<ObjectId> reportIds = uploadedReports.stream().map(Report :: getId).toList();
                List<ObjectId> existedReportIds = teleconEntry.getReports();
                if(existedReportIds.isEmpty()){
                    existedReportIds = new ArrayList<>();
                }
                existedReportIds.addAll(reportIds);
                teleconEntry.setReports(existedReportIds);
                teleconEntry.setUpdatedAt(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
                teleconServ.save(teleconEntry);

                return ResponseEntity.status(200).body(new UploadReportResponse(uploadedReports, "Reports Uploaded Successfully"));
            }catch (Exception e){
                return ResponseEntity.status(500).body(new UploadReportResponse(null
                        ,"Internal Server Error"));
        }

        }else{
                return ResponseEntity.status(404).body(new UploadReportResponse(null
                        ,"Entry Not Found"));
            }
    }

    private ResponseEntity<UploadReportResponse> uploadReport(ReportsRequestDto data, MultipartFile file,
                                                              String fileName, List<String> uploadFiles,
                                                              List<Report> uploadedReports, List<ObjectId> reportIdList) {
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
            Report report = new Report();
            report.setTeleconId(data.getTeleconId());
            report.setReportName(data.getReportName());
            report.setCreatedAt(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            report.setUpdatedAt(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            reportRepo.save(report);
            uploadedReports.add(report); //Report model list
            reportIdList.add(report.getId());

        }
        catch(Exception ex){
            return ResponseEntity.status(500).body(new UploadReportResponse(null, "Internal Server Error"));
        }
        return null;
    }
}
