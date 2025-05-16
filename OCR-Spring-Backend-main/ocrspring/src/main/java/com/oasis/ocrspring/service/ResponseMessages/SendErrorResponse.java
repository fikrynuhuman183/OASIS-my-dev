package com.oasis.ocrspring.service.ResponseMessages;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SendErrorResponse {
    HttpServletResponse response;
    String message;

    public SendErrorResponse() {

    }

    public void setErrorMessage(HttpServletResponse response, String message) throws IOException {
        ErrorMessage errorResponse = new ErrorMessage(message);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    public void setErrorResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(success, message);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

}
