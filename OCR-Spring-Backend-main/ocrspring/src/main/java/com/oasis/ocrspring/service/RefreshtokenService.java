package com.oasis.ocrspring.service;

import com.oasis.ocrspring.model.RefreshToken;
import com.oasis.ocrspring.repository.RefreshtokenRepsitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RefreshtokenService {
    private final RefreshtokenRepsitory refreshtokenRepo;

    @Autowired
    public RefreshtokenService(RefreshtokenRepsitory refreshtokenRepo) {
        this.refreshtokenRepo = refreshtokenRepo;
    }

    public List<RefreshToken> allRefreshtokenDetails() {
        return refreshtokenRepo.findAll();
    }
}
