package com.oasis.ocrspring.service;

import com.oasis.ocrspring.model.Request;
import com.oasis.ocrspring.model.User;
import com.oasis.ocrspring.repository.RequestRepository;
import com.oasis.ocrspring.repository.UserRepository;
import com.oasis.ocrspring.service.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.List;
import java.util.Optional;

@Service
public class RequestService {
    private final RequestRepository requestRepo;
    private final EmailService emailService;
    private final UserRepository userRepo;

    @Autowired
    public RequestService(RequestRepository requestRepo, EmailService emailService, UserRepository userRepo) {
        this.requestRepo = requestRepo;
        this.emailService = emailService;
        this.userRepo = userRepo;
    }
    public List<Request> allRequestDetails(){
        return requestRepo.findAll();
    }
     public Optional<Request> getRequestById(String id) {
         return requestRepo.findById(id);
     }
    public Request createRequest(Request request){
        return requestRepo.save(request);
    }
    public boolean rejectRequest(String id, String reason) throws MessagingException {
        Optional<Request> requestOptional = requestRepo.findById(id);
        if (requestOptional.isPresent()) {
            Request request = requestOptional.get();
            requestRepo.deleteById(id);
            emailService.sendEmail(request.getEmail(), "REJECT", reason, request.getUserName());
            return true;
        } else {
            return false;
        }
    }
    public void acceptRequest(String id, User newUser, String reason) throws MessagingException{
        Optional<Request> requestOptional = requestRepo.findById(id);
            Request request = requestOptional.get();
            userRepo.save(newUser);
            requestRepo.deleteById(id);
            emailService.sendEmail(request.getEmail(), "ACCEPT", reason, request.getUserName());


    }
}
