package ru.netology.diploma.controller;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

            log.info("User " + authenticationRequest.getLogin() + " logged in");
            return ResponseEntity.ok(new AuthenticationResponseDto(token));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }


    @CrossOrigin
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String username = assistantService.logout(request, response);

        log.info("User " + username + " logged out");
        return ResponseEntity.ok(HttpStatus.OK);
    }

}
