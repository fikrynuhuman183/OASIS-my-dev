package com.oasis.ocrspring.service.draftServices;

import com.oasis.ocrspring.dto.ImageRequestDto;
import com.oasis.ocrspring.dto.UploadDraftImageResponse;
import com.oasis.ocrspring.dto.UploadImageResponse;
import com.oasis.ocrspring.model.draftModels.DraftEntry;
import com.oasis.ocrspring.model.draftModels.DraftImage;
import com.oasis.ocrspring.repository.draftRepos.DraftEntryRepository;
import com.oasis.ocrspring.repository.draftRepos.DraftimageRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
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
public class DraftImageService {
    private final DraftimageRepository draftImageRepo;
    private final DraftEntryRepository draftEntryRepo;

    @Autowired
    public DraftImageService(DraftimageRepository draftImageRepo,
                             DraftEntryRepository draftEntryRepo) {
        this.draftImageRepo = draftImageRepo;
        this.draftEntryRepo = draftEntryRepo;
    }

    @Value("${uploadDir}")
    private String uploadDir;

    public ResponseEntity<UploadDraftImageResponse> uploadImages(ImageRequestDto data,
                                                                 String id,
                                                                 String clinicianId,
                                                                 List<MultipartFile> files)  {
        List<DraftImage> uploadedImages = new ArrayList<>();
        List<String> imageURIs = new ArrayList<>();
        final String errorMessage = "Internal Server Error";
        com.oasis.ocrspring.model.draftModels.DraftEntry draftEntry;
        try {
            draftEntry = draftEntryRepo.findById(new ObjectId(id)).orElse(null);
            if (draftEntry == null || draftEntry.getClinicianId().toString().equals(clinicianId)) {
                return ResponseEntity.status(404).body(new UploadDraftImageResponse(null, "Entry Not Found"));
            }
            if (files.size() >12){
                return ResponseEntity.status(500).body(new UploadDraftImageResponse(null, errorMessage));
            }
            for (MultipartFile file : files) {
                String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
                ResponseEntity<UploadDraftImageResponse> savedImage = saveImage(data, file, fileName, imageURIs, uploadedImages, errorMessage);
                if (savedImage != null) return savedImage;
            }

            ResponseEntity<UploadDraftImageResponse> updatedDraftEntry = updateTeleconEntry(uploadedImages, draftEntry, errorMessage);
            if (updatedDraftEntry != null) return updatedDraftEntry;

            return ResponseEntity.status(200).body(new UploadDraftImageResponse(uploadedImages, "Images Uploaded Successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new UploadDraftImageResponse(null, errorMessage));
        }
    }

    private ResponseEntity<UploadDraftImageResponse> updateTeleconEntry(List<DraftImage> uploadedImages, DraftEntry draftEntry, String errorMessage) {
        try {
            List<ObjectId> imageIds = uploadedImages.stream().map(DraftImage::getId).toList();
            List<ObjectId> existedImageIds = draftEntry.getImages();
            if (existedImageIds.isEmpty()) {
                existedImageIds = new ArrayList<>();
            }

            existedImageIds.addAll(imageIds);
            draftEntry.setImages(existedImageIds);
            draftEntry.setUpdatedAt(LocalDateTime.now());
            draftEntryRepo.save(draftEntry);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new UploadDraftImageResponse(null, errorMessage));
        }
        return null;
    }

    private ResponseEntity<UploadDraftImageResponse> saveImage(ImageRequestDto data, MultipartFile file,
                                                          String fileName, List<String> imageURIs,
                                                          List<DraftImage> uploadedImages, String errorMessage) {
        try {
            extracted(file, fileName, imageURIs);
            DraftImage image = getImage(data);
            draftImageRepo.save(image);
            uploadedImages.add(image);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new UploadDraftImageResponse(null, errorMessage));
        }
        return null;
    }

    private void extracted(MultipartFile file, String fileName, List<String> imageURIs) throws IOException {
        Path path = Paths.get(uploadDir + File.separator + fileName);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        //useful for creating uri to check the report
        String fileDownUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files")
                .path(fileName)
                .toUriString();
        imageURIs.add(fileDownUri);
        //How the Naming convension of the files work
    }

    private static DraftImage getImage(ImageRequestDto data) {
        //create new Image object for each file and copy the image data
        DraftImage image = new DraftImage();
        image.setTeleconEntryId(data.getTeleconId());
        image.setImageName(data.getImageName());
        image.setLocation(data.getLocation());
        image.setClinicalDiagnosis(data.getClinicalDiagnosis());
        image.setLesionsAppear(data.getLesionsAppear());
        image.setAnnotation(data.getAnnotation());
        image.setPredictedCat(data.getPredictedCat());
        image.setCreatedAt(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
        image.setUpdatedAt(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
        return image;
    }

}
