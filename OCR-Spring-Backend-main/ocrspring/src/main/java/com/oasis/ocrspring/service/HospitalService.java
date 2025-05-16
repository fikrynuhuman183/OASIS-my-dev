package com.oasis.ocrspring.service;

import com.oasis.ocrspring.dto.HospitalDto;
import com.oasis.ocrspring.model.Hospital;
import com.oasis.ocrspring.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HospitalService {
    private final HospitalRepository hospitalRepo;

    @Autowired
    public HospitalService(HospitalRepository hospitalRepo) {
        this.hospitalRepo = hospitalRepo;
    }

    public List<Hospital> allHospitalDetails() {
        return hospitalRepo.findAll();
    }

    public boolean addHospital(HospitalDto hospitalDetails) {
        if (hospitalRepo.findByName(hospitalDetails.getName()).isPresent()) {
            return false;
        } else {

            hospitalRepo.save(new Hospital(hospitalDetails));
            return true;
        }
    }
    public Optional<Hospital> getHospitalById(String id) {
        return hospitalRepo.findById(id);
    }

    public void updateHospital(String id, HospitalDto hospitalDetails) {
        Hospital hospital = hospitalRepo.findById(id).orElseThrow(() -> new RuntimeException("Hospital Not Found"));
        hospital.setHospital(hospitalDetails);
        hospitalRepo.save(hospital);
    }
    public boolean deleteHospital(String id) {
        if (hospitalRepo.existsById(id)) {
            hospitalRepo.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}
