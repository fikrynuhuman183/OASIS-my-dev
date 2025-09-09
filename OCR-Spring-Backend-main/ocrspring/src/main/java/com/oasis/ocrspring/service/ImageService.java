package com.oasis.ocrspring.service;

import com.oasis.ocrspring.dto.ImageNotFoundException;
import com.oasis.ocrspring.dto.ImageRequestDto;
import com.oasis.ocrspring.dto.UpdateImageRequestDto;
import com.oasis.ocrspring.dto.UploadImageResponse;
import com.oasis.ocrspring.model.Image;
import com.oasis.ocrspring.model.TeleconEntry;
import com.oasis.ocrspring.repository.ImageRepository;
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
import java.util.Optional;

@Service
public class ImageService {
    private final ImageRepository imageRepo;
    private final TeleconEntriesService teleconServices;
    private final TeleconEntriesRepository teleconRepo;

    @Value("${uploadDir}")
    private String uploadDir;

    @Autowired
    public ImageService(ImageRepository imageRepo, TeleconEntriesService teleconServices, TeleconEntriesRepository teleconRepo) {
        this.imageRepo = imageRepo;
        this.teleconServices = teleconServices;
        this.teleconRepo = teleconRepo;
    }

    public List<Image> allImageDetails() {
        return imageRepo.findAll();
    }


    public ResponseEntity<UploadImageResponse> uploadImages(ImageRequestDto data,
                                                            String id,
                                                            String clinicianId,
                                                            List<MultipartFile> files)  {
        List<Image> uploadedImages = new ArrayList<>();
        List<String> imageURIs = new ArrayList<>();
        final String errorMessage = "Internal Server Error";
        TeleconEntry teleconEntry;
        try {
            teleconEntry = teleconRepo.findById(new ObjectId(id)).orElse(null);
            if (teleconEntry == null ) {
                return ResponseEntity.status(404).body(new UploadImageResponse(null, "Entry Not Found"));
            }
            if (files.size() >12){
                return ResponseEntity.status(500).body(new UploadImageResponse(null, errorMessage));
            }

            for (MultipartFile file : files) {
                String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
                ResponseEntity<UploadImageResponse> savedImage = saveImage(data, file, fileName, imageURIs, uploadedImages, errorMessage);
                if (savedImage != null) return savedImage;
            }

            ResponseEntity<UploadImageResponse> updatedEntry = updateEntry(uploadedImages, teleconEntry, errorMessage);
            if (updatedEntry != null) return updatedEntry;

            return ResponseEntity.status(200).body(new UploadImageResponse(uploadedImages, "Images Uploaded Successfully"));
        }catch (Exception e) {
            return ResponseEntity.status(500).body(new UploadImageResponse(null, errorMessage));
        }
    }

    private ResponseEntity<UploadImageResponse> updateEntry(List<Image> uploadedImages, TeleconEntry teleconEntry, String errorMessage) {
        try {
            List<ObjectId> imageIds = uploadedImages.stream().map(Image::getId).toList();
            List<ObjectId> existedImageIds = teleconEntry.getImages();
            if (existedImageIds.isEmpty()) {
                existedImageIds = new ArrayList<>();
            }

            existedImageIds.addAll(imageIds);
            teleconEntry.setImages(existedImageIds);
            teleconEntry.setUpdatedAt(LocalDateTime.now());
            teleconServices.save(teleconEntry);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new UploadImageResponse(null, errorMessage));
        }
        return null;
    }

    private ResponseEntity<UploadImageResponse> saveImage(ImageRequestDto data, MultipartFile file, String fileName, List<String> imageURIs, List<Image> uploadedImages, String errorMessage) {
        try {
            creatingPathAndURI(file, fileName, imageURIs);
            Image image = getImage(data);
            // Store the file URI separately from user's location field
            if (!imageURIs.isEmpty()) {
                image.setFileUri(imageURIs.get(imageURIs.size() - 1)); // Store file path for serving
            }
            
            // Set file size and content type from the multipart file
            image.setFileSize(file.getSize());
            image.setContentType(file.getContentType());
            
            imageRepo.save(image);
            // Debug: print the saved image details for tracing
            System.out.println("[ImageService] Saved Image record: imageName=" + image.getImageName() + ", fileUri=" + image.getFileUri());
            uploadedImages.add(image);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new UploadImageResponse(null, errorMessage));
        }
        return null;
    }


    private void creatingPathAndURI(MultipartFile file, String fileName, List<String> imageURIs) throws IOException {
    // Resolve uploadDir to absolute path and create directories if missing
    Path dirPath = Paths.get(uploadDir == null || uploadDir.isBlank() ? "Storage/images" : uploadDir)
        .toAbsolutePath().normalize();
    if (!Files.exists(dirPath)) {
        Files.createDirectories(dirPath);
    }

    // Generate a unique filename to avoid collisions
    String safeFileName = StringUtils.cleanPath(fileName);
    String uniqueFileName = System.currentTimeMillis() + "_" + safeFileName;

    // Create the full file path
    Path filePath = dirPath.resolve(uniqueFileName);
    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    // Log the physical path where the file was saved for debugging
    System.out.println("[ImageService] Saved file to: " + filePath.toAbsolutePath().toString());
    System.out.println("[ImageService] uploadDir resolved to: " + dirPath.toAbsolutePath().toString());

    // useful for creating uri to check the file from frontend
    String fileDownUri = ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/files/")
        .path(uniqueFileName)
        .toUriString();
    imageURIs.add(fileDownUri);
    // Naming convention: <timestamp>_originalFilename
    }

    private static Image getImage(ImageRequestDto data) {
        //create new Image object for each file and copy the image data
        Image image = new Image();
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

    public long countImages() {
        return imageRepo.count();
    }



    // id is entry _id
    public void updateImage(UpdateImageRequestDto request) {
        Optional<Image> optionalImage = imageRepo.findById(request.get_id());
        if (optionalImage.isPresent()) {
            Image image = optionalImage.get();
            image.setLocation(request.getLocation());
            image.setClinicalDiagnosis(request.getClinical_diagnosis());
            image.setLesionsAppear(request.getLesions_appear());
            image.setAnnotation(request.getAnnotation());
            imageRepo.save(image);
        } else {
            throw new ImageNotFoundException("Image not found");
        }
    }

    public Optional<Image> getImageById(String id) {
        try {
            Image image = imageRepo.findById(new ObjectId(id));
            return Optional.ofNullable(image);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
