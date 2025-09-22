package com.oasis.ocrspring.controller;

import com.oasis.ocrspring.dto.*;
import com.oasis.ocrspring.model.*;
import com.oasis.ocrspring.service.*;
import com.oasis.ocrspring.service.ResponseMessages.ErrorMessage;
import com.oasis.ocrspring.service.auth.AuthenticationToken;
import com.oasis.ocrspring.service.auth.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/v3/admin")
public class AdminController {
    // connect admin to the service layer
    private final TokenService tokenService;
    private final RequestService requestService;
    private final AuthenticationToken authenticationToken;
    private final UserService userService;
    private final RoleService roleService;
    private final OptionService optionService;
    private final HospitalService hospitalService;

    @Autowired
    public AdminController(TokenService tokenService, RequestService requestService, AuthenticationToken authenticationToken,
                           UserService userService, RoleService roleService, OptionService optionService, HospitalService hospitalService) {
        this.tokenService = tokenService;
        this.requestService = requestService;
        this.authenticationToken = authenticationToken;
        this.userService = userService;
        this.roleService = roleService;
        this.optionService = optionService;
        this.hospitalService = hospitalService;
    }

    @ApiIgnore
    @RequestMapping(value = "/")
    public void redirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("/swagger-ui.html");
    }
    static String unAuthorized = "Unauthorized access";
    static String internalServerError = "Internal Server Error!";
    static String userNotFund = "User not found";
    static String roleNotFound = "Role not found";
    static String requestNotFound = "Request not found";

    //get all requests
    @GetMapping("/requests")
    public ResponseEntity<?> getAllRequests(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationToken.authenticateRequest(request, response);
        if (!tokenService.checkPermissions(request, List.of("100"))) {
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }

        try {
            List<Request> requests = requestService.allRequestDetails();
            List<RequestResDetailsDto> requestResDetailsDtos = new ArrayList<>();
            for (Request reviewer : requests) {
                requestResDetailsDtos.add(new RequestResDetailsDto(reviewer));
            }
            return ResponseEntity.ok(requestResDetailsDtos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorMessage(internalServerError));
        }
    }
    /* @GetMapping("/requests/test")
    public ResponseEntity<?> getAllRequestsTest() {
        try {
            List<Request> requests = requestService.allRequestDetails();
            List<RequestResDetailsDto> requestResDetailsDtos = new ArrayList<>();
            for (Request request : requests) {
                requestResDetailsDtos.add(new RequestResDetailsDto(request));
            }
            return ResponseEntity.ok(requestResDetailsDtos);
        } catch (Exception e) {
            e.printStackTrace(); // This will help you see the actual error
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    } */


    //get one request
    @GetMapping("/requests/{id}")
    public ResponseEntity<?> getRequest(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) throws IOException {
        authenticationToken.authenticateRequest(request, response);
        if (!tokenService.checkPermissions(request, List.of("100"))) {
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }

        try {
            Optional<Request> requestOptional = requestService.getRequestById(id);
            if (requestOptional.isPresent()) {
                return ResponseEntity.ok(new RequestDetailsDto(requestOptional.get()));
            } else {
                return ResponseEntity.status(404).body(new ErrorMessage(requestNotFound));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorMessage(internalServerError));
        }
    }


    //reject a request
    @PostMapping("/requests/{id}")
    public ResponseEntity<?> rejectRequest(HttpServletRequest request, HttpServletResponse response, @PathVariable String id, @RequestBody ReqestDeleteReasonDto reason) throws IOException {
        authenticationToken.authenticateRequest(request, response);
        if (!tokenService.checkPermissions(request, List.of("100"))) {
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }

        try {
            boolean isDeleted = requestService.rejectRequest(id, reason.getReason());
            if (isDeleted) {
                return ResponseEntity.ok().body(new ErrorMessage("Request has been deleted!"));
            } else {
                return ResponseEntity.status(404).body(new ErrorMessage(requestNotFound));
            }
        }
        catch (MessagingException e) {
            throw new EmailSendingException("Failed to send email", e);
        }catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorMessage(e.toString()));
        }
    }
    static class EmailSendingException extends RuntimeException {
        public EmailSendingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    //approve a request
    @PostMapping("/accept/{id}")
    public ResponseEntity<?> acceptRequest(HttpServletRequest request, HttpServletResponse response, @PathVariable String id, @RequestBody ReqToUserDto userDto) throws IOException {
        ResponseEntity<?> authResponse = authenticateAndAuthorize(request, response, List.of("100"));
        if (authResponse != null) return authResponse;

        try {
            Optional<Request> requestOptional = requestService.getRequestById(id);
            if (requestOptional.isPresent()) {
                Request req = requestOptional.get();
                if (userService.isRegNoInUse(req.getRegNo())) {
                    return ResponseEntity.status(401).body(new ErrorMessage("Reg No already in use"));
                }

                if (userService.isEmailInUse(req.getEmail())) {
                    return ResponseEntity.status(401).body(new ErrorMessage("Email address already in use"));
                }

                User newUser = createUserFromRequest(req, userDto);
                requestService.acceptRequest(id, newUser, userDto.getReason());
                userService.sendAcceptanceEmail(req.getEmail(), userDto.getReason(), req.getUserName());
                return buildUserResponse(newUser);
            } else {
                return ResponseEntity.status(404).body(new ErrorMessage(requestNotFound));
            }
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body(e);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorMessage(internalServerError));
        }
    }

    private ResponseEntity<?> authenticateAndAuthorize(HttpServletRequest request, HttpServletResponse response, List<String> permissions) throws IOException {
        authenticationToken.authenticateRequest(request, response);
        if (!tokenService.checkPermissions(request, permissions)) {
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }
        return null;
    }
    private User createUserFromRequest(Request req, ReqToUserDto userDto) {
        return new User(
                userDto.getUsername() != null ? userDto.getUsername() : req.getUserName(),
                req.getEmail(),
                req.getRegNo(),
                userDto.getRole(),
                req.getHospital(),
                userDto.getDesignation() != null ? userDto.getDesignation() : "",
                userDto.getContact_no() != null ? userDto.getContact_no() : "",
                true
        );
    }
    private ResponseEntity<?> buildUserResponse(User newUser) {
        if (newUser.getUpdatedAt() == null) {
            return ResponseEntity.ok(new UserResDto(
                    newUser.getUsername(),
                    newUser.getEmail(), newUser.getRegNo(),
                    newUser.getHospital(), newUser.getDesignation(),
                    newUser.getContactNo(), newUser.isAvailable(),
                    newUser.getRole(),
                    newUser.getId().toString(),
                    newUser.getCreatedAt().toString(),
                    "User created successfully"));
        }
        return ResponseEntity.ok(new UserResDto(
                newUser.getUsername(),
                newUser.getEmail(),
                newUser.getRegNo(),
                newUser.getHospital(),
                newUser.getDesignation(),
                newUser.getContactNo(),
                newUser.isAvailable(),
                newUser.getRole(),
                newUser.getId().toString(),
                newUser.getCreatedAt().toString(),
                newUser.getUpdatedAt().toString(), "User created successfully"));
    }

    //get users by thier roles
    //only for read or write access permission
    @GetMapping("/users/role/{role}")
    public ResponseEntity<?> getUsersByRole(HttpServletRequest request, HttpServletResponse response, @PathVariable String role) throws IOException {
        authenticationToken.authenticateRequest(request, response);
        if (!tokenService.checkPermissions(request, List.of("106", "107"))) {
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }

        try {
            Optional<List<User>> users;
            if ("All".equals(role)) {
                users = userService.allUserDetails();
                if(users.isEmpty()){
                    return ResponseEntity.status(404).body(new ErrorMessage("No users found"));
                }
            } else {
                users = userService.getUsersByRole(role);
                if(users.isEmpty()){
                    return ResponseEntity.status(404).body(new ErrorMessage("No users found"));
                }
            }

            List<UserResDto> userResDtos = users.get().stream()
                    .map(user -> new UserResDto(
                            user.getUsername(),
                            user.getEmail(),
                            user.getRegNo(),
                            user.getHospital(),
                            user.getDesignation(),
                            user.getContactNo(),
                            user.isAvailable(),
                            user.getRole(),
                            user.getId().toString(),
                            user.getCreatedAt().toString(),
                            user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null,
                            null))
                    .toList();

            return ResponseEntity.ok(userResDtos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorMessage(internalServerError));
        }
    }

    //get all user roles

    @GetMapping("/roles")
    public ResponseEntity<?> getAllRoles(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Authenticate request
        authenticationToken.authenticateRequest(request, response);

        // Check permissions
        if (!tokenService.checkPermissions(request, List.of("106", "107", "100", "109"))) {
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }

        try {
            List<Role> roles = roleService.allRoleDetails();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorMessage(internalServerError));
        }
    }


    //get one user role
    @GetMapping("/roles/{id}")
    public ResponseEntity<?> getRoleById(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) throws IOException {
        // Authenticate request
        authenticationToken.authenticateRequest(request, response);

        // Check permissions
        if (!tokenService.checkPermissions(request, List.of("109"))) {
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }

        try {
            Optional<Role> role = roleService.getRoleById(id);
            if (role.isPresent()) {
                return ResponseEntity.ok(role.get());
            } else {
                return ResponseEntity.status(404).body(new ErrorMessage(roleNotFound));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorMessage(internalServerError));
        }
    }

    //add a new role
    @PostMapping("/roles")
    public ResponseEntity<ErrorMessage> addRole(HttpServletRequest request, HttpServletResponse response, @RequestBody RoleReqDto role) throws IOException{
        // Authenticate request
        authenticationToken.authenticateRequest(request, response);

        // Check permissions
        if (!tokenService.checkPermissions(request, List.of("109"))) {
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }

        try {
            boolean isRoleAdded = roleService.addRole(role);
            if (isRoleAdded) {
                return ResponseEntity.ok(new ErrorMessage("New role added successfully"));
            } else {

                return ResponseEntity.status(401).body(new ErrorMessage("Role already exists"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorMessage(internalServerError));
        }
    }

    //update user permission
    @PostMapping("/roles/{id}")
    public ResponseEntity<ErrorMessage> updateRole(HttpServletRequest request, HttpServletResponse response, @PathVariable String id, @RequestBody RoleDto roleDetails) throws IOException {
        // Authenticate request
        authenticationToken.authenticateRequest(request, response);

        // Check permissions
        if (!tokenService.checkPermissions(request, List.of("109"))) {
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }

        try {
            Optional<Role> role = roleService.getRoleById(id);
            if (role.isPresent()) {
                roleService.updateRole(id, roleDetails);
                return ResponseEntity.ok(new ErrorMessage("Role updated successfully"));
            } else {
                return ResponseEntity.status(404).body(new ErrorMessage(roleNotFound));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorMessage(roleNotFound));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorMessage(internalServerError));
        }
    }

    //get a specific user
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) throws IOException {
        // Authenticate request
        authenticationToken.authenticateRequest(request, response);

        // Check permissions
        if (!tokenService.checkPermissions(request, List.of("106", "107"))) {
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }

        try {
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(new UserResDto(user.get()));
            } else {
                return ResponseEntity.status(404).body(new ErrorMessage(userNotFund));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorMessage(internalServerError));
        }
    }

    //update a user
    @PostMapping("/update/user/{id}")
    public ResponseEntity<?> updateUser(HttpServletRequest request, HttpServletResponse response, @PathVariable String id, @RequestBody UserNameAndRoleDto userDetails) throws IOException {
        // Authenticate request
        authenticationToken.authenticateRequest(request, response);

        // Check permissions
        if (!tokenService.checkPermissions(request, List.of("107"))) {
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }

        try {
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                userService.updateUser(id, userDetails);
                User updatedUser = userService.getUserById(id).orElseThrow(() -> new RuntimeException(userNotFund));
                UserResDto userResDto = new UserResDto(updatedUser);
                userResDto.setMessage("User details updated successfully");
                return ResponseEntity.ok(userResDto);
            } else {
                return ResponseEntity.status(404).body(new ErrorMessage(userNotFund));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorMessage(userNotFund));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorMessage(internalServerError));
        }
    }

    //delete a user

    @PostMapping("/delete/user/{id}")
    public ResponseEntity<ErrorMessage> deleteUser(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) throws IOException {
        // Authenticate request
        authenticationToken.authenticateRequest(request, response);
        if (!tokenService.checkPermissions(request, List.of("107"))) {
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }

        try {
            boolean isDeleted = userService.deleteUser(id);
            if (isDeleted) {
                return ResponseEntity.ok(new ErrorMessage("User deleted successfully"));
            } else {
                return ResponseEntity.status(404).body(new ErrorMessage("User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorMessage(internalServerError));
        }
    }

    //add hospital
    @PostMapping("/hospital")
    public ResponseEntity<ErrorMessage> addHospital(HttpServletRequest request, HttpServletResponse response, @RequestBody HospitalDto hospitalDetails) throws IOException {
        // Authenticate request
        authenticationToken.authenticateRequest(request, response);

        // Check permissions
        if (!tokenService.checkPermissions(request, List.of("101"))) {
            return ResponseEntity.status(401).body(new ErrorMessage(unAuthorized));
        }

        try {
            boolean isAdded = hospitalService.addHospital(hospitalDetails);
            if (isAdded) {
                return ResponseEntity.ok(new ErrorMessage("Hospital is added successfully!"));
            } else {
                return ResponseEntity.status(401).body(new ErrorMessage("Hospital is already added"));
            }
        } catch (Exception e) {
            e.printStackTrace(); // This will print the stack trace to your server logs
            return ResponseEntity.status(500).body(new ErrorMessage(e.getMessage()));
        }
    }

    //update hospital details
    @PostMapping("/hospitals/update/{id}")
    public ResponseEntity<MessageDto> updateHospital(HttpServletRequest request, HttpServletResponse response, @PathVariable String id, @RequestBody HospitalDto hospitalDetails) throws IOException {
        // Authenticate request
        authenticationToken.authenticateRequest(request, response);

        // Check permissions
        if (!tokenService.checkPermissions(request, List.of("101"))) {
            return ResponseEntity.status(401).body(new MessageDto(unAuthorized));
        }

        try {
            Optional<Hospital> hospital = hospitalService.getHospitalById(id);
            if (hospital.isPresent()) {
                hospitalService.updateHospital(id, hospitalDetails);
                return ResponseEntity.ok(new MessageDto("Hospital details updated successfully!"));
            } else {
                return ResponseEntity.status(404).body(new MessageDto("Hospital Not Found"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new MessageDto("Hospital Not Found"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageDto(internalServerError));
        }
    }

    //delete hospital
    @PostMapping("/hospitals/delete/{id}")
    public ResponseEntity<MessageDto> deleteHospital(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) throws IOException {
        // Authenticate request
        authenticationToken.authenticateRequest(request, response);

        // Check permissions
        if (!tokenService.checkPermissions(request, List.of("101"))) {
            return ResponseEntity.status(401).body(new MessageDto(unAuthorized));
        }

        try {
            boolean isDeleted = hospitalService.deleteHospital(id);
            if (isDeleted) {
                return ResponseEntity.ok(new MessageDto("Hospital deleted successfully"));
            } else {
                return ResponseEntity.status(404).body(new MessageDto("Hospital not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageDto(internalServerError));
        }
    }
    //get all hospitals
    @GetMapping("/hospitals/{id}")
    public ResponseEntity<?> getHospitalDetails(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) throws IOException {
        // Authenticate request
        authenticationToken.authenticateRequest(request, response);

        // Check permissions
        if (!tokenService.checkPermissions(request, List.of("101"))) {
            return ResponseEntity.status(401).body(new MessageDto(unAuthorized));
        }

        try {
            Optional<Hospital> hospital = hospitalService.getHospitalById(id);
            if (hospital.isPresent()) {
                return ResponseEntity.ok(hospital.get());
            } else {
                return ResponseEntity.status(404).body(new MessageDto("Hospital not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageDto(internalServerError));
        }
    }



    //get options (labels and values)
    @GetMapping("option/{name}")
    public ResponseEntity<?> getOptionByName(HttpServletRequest request,HttpServletResponse response,@PathVariable String name) throws IOException {
        authenticationToken.authenticateRequest(request, response);

        try {
            Option option = optionService.findByName(name);
            if (option == null) {
                return ResponseEntity.status(404).body(new MessageDto(name + " not found"));
            } else {
                return ResponseEntity.ok(new OptionsDto(option));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageDto(internalServerError));
        }
    }

    //Only to add options by tech team



    @PostMapping("/option")
    public ResponseEntity<MessageDto> addOption(@RequestBody OptionsDto optionsDto) {

        try {
            Option existingOption = optionService.findByName(optionsDto.getName());
            if (existingOption != null) {
                return ResponseEntity.status(401).body(new MessageDto("Option already exists"));
            } else {
                return saveOption(optionsDto);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageDto(internalServerError));
        }
    }
    private ResponseEntity<MessageDto> saveOption(OptionsDto optionsDto) {
        try {
            optionService.saveOption(new Option(optionsDto.getName(), optionsDto.getOptions()));
            return ResponseEntity.ok(new MessageDto("Option is saved"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageDto(internalServerError));
        }
    }
}
