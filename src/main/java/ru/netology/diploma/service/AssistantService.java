package ru.netology.diploma.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma.dto.request.AuthenticationRequestDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AssistantService {
    String createToken(AuthenticationRequestDto authenticationRequest);
    void logout(HttpServletRequest request, HttpServletResponse response);

    String resolveUsername(HttpServletRequest request);
    ResponseEntity<Resource> sendFile(MultipartFile file) throws IOException;

}
