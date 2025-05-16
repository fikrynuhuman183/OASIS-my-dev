package com.oasis.ocrspring.controller;

import com.oasis.ocrspring.dto.AdminSignUpRequestDto;
import com.oasis.ocrspring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
    private final UserService userService;

    @Autowired
    public AdminAuthController( UserService userService){
        this.userService = userService;
    }

    @ApiIgnore
    public void redirrect(HttpServletResponse response) throws IOException {
        response.sendRedirect("/swaggr-ui.html");
    }

    @PostMapping("/signup")
    public ResponseEntity<?> adminSignUp(@RequestBody AdminSignUpRequestDto signupRequest) {

        return userService.adminSignUp(signupRequest);
    }
}

