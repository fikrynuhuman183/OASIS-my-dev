package com.oasis.ocrspring.service;

import com.oasis.ocrspring.dto.*;
import com.oasis.ocrspring.model.Request;
import com.oasis.ocrspring.model.User;
import com.oasis.ocrspring.repository.RequestRepository;
import com.oasis.ocrspring.repository.UserRepository;
import com.oasis.ocrspring.service.email.EmailService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService
{
    private final UserRepository userRepo;
    private final RequestRepository requestRepo;
    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepo, RequestRepository requestRepo, EmailService emailService){
        this.userRepo = userRepo;
        this.requestRepo = requestRepo;
        this.emailService =  emailService;
    }

    public Optional<List<User>> allUserDetails(){
        return Optional.of(userRepo.findAll());
    }

    public User createUser(User user){
        return userRepo.save(user);
    }
    public String signup(RequestDto request){
        Optional<User> userRepoByRegNo = userRepo.findByRegNo(request.getReg_no());
        Optional<User> userByEmail = userRepo.findByEmail(request.getEmail());
        Optional<Request> requestByRegNo = requestRepo.findByRegNo(request.getReg_no());
        Optional<Request> requestByEmail = requestRepo.findByEmail(request.getEmail());

        if(userRepoByRegNo.isPresent()){
            return "User reg no already exist";
        }
        if(userByEmail.isPresent()){
            return "User Email is already registered";
        }
        if (requestByRegNo.isPresent()||requestByEmail.isPresent() ){
            return "A request for registration is already exists";
        }

        requestRepo.save(new Request(request.getUsername(),request.getEmail(),request.getReg_no(),request.getHospital(),request.getDesignation(),request.getContact_no()));
        return "Request is sent successfully. You will receive an Email on acceptance";

    }
    public Optional<User> getUserById(String id){

        return userRepo.findById(new ObjectId(id));
    }

    public Optional<User> getUserByEmail(String id){

        return userRepo.findByEmail(id);
    }
    public User updateUser(String id, UserDto userReqBody){
        User user = userRepo.findById(id).orElseThrow(()->new RuntimeException("User not found"));
        user.setUsername(userReqBody.getUsername());
        user.setHospital(userReqBody.getHospital());
        user.setContactNo(userReqBody.getContactNo());
        user.setAvailability(userReqBody.isAvailability());
        return userRepo.save(user);

    }

    public boolean isRegNoInUse(String regNo){
        return userRepo.findByRegNo(regNo).isPresent();
    }
    public boolean isEmailInUse(String email) {
        return userRepo.findByEmail(email).isPresent();
    }
    public User addUser(User user) {
        return userRepo.save(user);
    }
    public void sendAcceptanceEmail(String email, String reason, String username) throws MessagingException {
        emailService.sendEmail(email, "ACCEPT", reason, username);

    }
    public Optional<List<User>> getUsersByRole(String role) {
        List<User> users = userRepo.findByRole(role);
        return users.isEmpty() ? Optional.empty() : Optional.of(users);
    }

    public ResponseEntity<?> adminSignUp(AdminSignUpRequestDto signupRequest){
        User userName = null;
        User userEmail = null;
        try{
            userName = userRepo.findByUsername(signupRequest.getUsername()).orElse(null);
            userEmail = userRepo.findByEmail(signupRequest.getEmail()).orElse(null);
        if (userName != null){
            return  ResponseEntity.status(401).body(new MessageDto("User name is taken"));
        } else if (userEmail != null){
            return ResponseEntity.status(401).body(new MessageDto("The email address is already in use"));
        }else {
            User newUser = new User();
            newUser.setRegNo(signupRequest.getRegNo());
            newUser.setUsername(signupRequest.getUsername());
            newUser.setEmail(signupRequest.getEmail());
            newUser.setHospital(signupRequest.getHospital());
            newUser.setRole("System Admin");
            userRepo.save(newUser);
            AdminSignUpResponse response = new AdminSignUpResponse(newUser,"Successfully signed in");
            return ResponseEntity.status(200).body(response);
        }
    }catch (Exception e){
            return ResponseEntity.status(500).body(new ErrorResponseDto("Internal Server Error!",e.toString()));
        }
}

    public void updateUser(String id, UserNameAndRoleDto userDetails) {
        User user = userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(userDetails.getUsername());
        user.setRole(userDetails.getRole());
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);
    }

    public boolean deleteUser(String id) {
        if (userRepo.existsById(id)) {
            userRepo.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public long countUsers() {
        return userRepo.count();
    }
}
