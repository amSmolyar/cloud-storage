package ru.netology.diploma.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma.dto.request.AuthenticationRequestDto;
import ru.netology.diploma.security.jwt.JwtTokenProvider;
import ru.netology.diploma.service.AssistantService;
import ru.netology.diploma.service.CloudStorageService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class AssistantServiceImpl implements AssistantService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AssistantServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @Override
    public String createToken(AuthenticationRequestDto authenticationRequest) {
        String username = authenticationRequest.getLogin();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, authenticationRequest.getPassword()));
        return jwtTokenProvider.createToken(username);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtTokenProvider.resolveToken(request);
        jwtTokenProvider.addTokenToBlackList(token);

        //Authentication authentication = jwtTokenProvider.getAuthentication(token);
        SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
        securityContextLogoutHandler.logout(request, response, null);
    }

    @Override
    public String resolveUsername(HttpServletRequest request) {
        return jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(request));
    }

    @Override
    public ResponseEntity<Resource> sendFile(MultipartFile file) throws IOException {
        byte[] body = file.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getOriginalFilename());
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(body));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.getSize())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                //.contentType(MediaType.parseMediaType(multipartFile.getContentType()))
                .body(resource);
    }
}
