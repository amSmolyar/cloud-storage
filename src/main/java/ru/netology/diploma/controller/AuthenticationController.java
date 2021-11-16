package ru.netology.diploma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import ru.netology.diploma.dto.request.AuthenticationRequestDto;
import ru.netology.diploma.dto.response.AuthenticationResponseDto;
import ru.netology.diploma.service.AssistantService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


@RestController
public class AuthenticationController {
    private final AssistantService assistantService;

    @Autowired
    public AuthenticationController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @CrossOrigin
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> login(@Valid @RequestBody AuthenticationRequestDto authenticationRequest) {
        try {
            String token = assistantService.createToken(authenticationRequest);
            return ResponseEntity.ok(new AuthenticationResponseDto(token));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }


    @CrossOrigin
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        assistantService.logout(request, response);
        return ResponseEntity.ok(HttpStatus.OK);
    }

}
