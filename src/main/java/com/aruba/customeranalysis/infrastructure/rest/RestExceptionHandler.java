package com.aruba.customeranalysis.infrastructure.rest;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@ControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    private Map<String, Object> buildResponse(HttpStatus status, String error, String message) {
    	
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        
        return body;
        
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
    	
        log.error("Unexpected error: ", ex);
       
        return new ResponseEntity<>(
            buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage()),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
        
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Object> handleMultipartException(MultipartException ex) {
        
    	log.error("Multipart upload error: {}", ex.getMessage());
        
    	return new ResponseEntity<>(
            buildResponse(HttpStatus.BAD_REQUEST, "Invalid File Upload", "Error during file upload. Please check the format or size."),
            HttpStatus.BAD_REQUEST
        );
    	
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
       
    	log.error("File too large: {}", ex.getMessage());
        
    	return new ResponseEntity<>(
            buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "File Too Large", "The uploaded file exceeds the maximum allowed size."),
            HttpStatus.PAYLOAD_TOO_LARGE
        );
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleIOException(IOException ex) {
    	
        log.error("I/O error: ", ex);
        
        return new ResponseEntity<>(
            buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "File Processing Error", "Error occurred while reading or processing the file."),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
        
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParam(MissingServletRequestParameterException ex) {
    	
        log.error("Missing request parameter: {}", ex.getParameterName());
        
        return new ResponseEntity<>(
            buildResponse(HttpStatus.BAD_REQUEST, "Missing Parameter", "Missing parameter: " + ex.getParameterName()),
            HttpStatus.BAD_REQUEST
        );
        
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
    	
        log.error("Bad credentials: {}", ex.getMessage());
        
        return new ResponseEntity<>(
            buildResponse(HttpStatus.UNAUTHORIZED, "Invalid Credentials", "Incorrect username or password."),
            HttpStatus.UNAUTHORIZED
        );
        
    }
    
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFound(UsernameNotFoundException ex) {
    	
        log.error("User not found: {}", ex.getMessage());
        
        return new ResponseEntity<>(
            buildResponse(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage()),
            HttpStatus.NOT_FOUND
        );
        
    }

}
