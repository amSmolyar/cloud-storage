package ru.netology.diploma.service.impl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.netology.diploma.dto.request.AuthenticationRequestDto;
import ru.netology.diploma.security.jwt.JwtAuthenticationException;
import ru.netology.diploma.security.jwt.JwtTokenProvider;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class AssistantServiceImplTest {

    @InjectMocks
    AssistantServiceImpl assistantService;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @ParameterizedTest
    @ValueSource(strings = {"user1", "user2", "user3"})
    void createToken_ok(String login) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        AuthenticationRequestDto authenticationRequestDto = new AuthenticationRequestDto(login, "password");

        when(jwtTokenProvider.createToken(authenticationRequestDto.getLogin())).thenReturn("Bearer_" + authenticationRequestDto.getLogin() + "_token");

        String actual = assistantService.createToken(authenticationRequestDto);

        assertThat(actual).isEqualTo("Bearer_" + authenticationRequestDto.getLogin() + "_token");
    }

    @ParameterizedTest
    @ValueSource(strings = {"user1", "user2", "user3"})
    void createToken_throwBadCredentialsException(String login) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        AuthenticationRequestDto authenticationRequestDto = new AuthenticationRequestDto(login, "password");

        doThrow(new JwtAuthenticationException("authentication exception")).when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(JwtAuthenticationException.class, () ->
                assistantService.createToken(authenticationRequestDto));
    }

    @Test
    void logout_ok() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn("Bearer_token");

        assistantService.logout(request, response);

        Mockito.verify(jwtTokenProvider, times(1)).resolveToken(any(HttpServletRequest.class));
        Mockito.verify(jwtTokenProvider, times(1)).addTokenToBlackList(any(String.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user1", "user2", "user3"})
    void resolveUsername_ok(String username) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn("Bearer_token");
        when(jwtTokenProvider.getUsername(any(String.class))).thenReturn(username);

        String actual = assistantService.resolveUsername(request);

        assertThat(actual).isEqualTo(username);
    }


    @Test
    void sendFile_ok() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String filename = "test.txt";
        String text = "Text to be uploaded.";
        MockMultipartFile file = new MockMultipartFile(filename, filename, "text/plain", text.getBytes());

        ResponseEntity<Resource> actual = assertDoesNotThrow(() -> assistantService.sendFile(file));
        Assertions.assertThat(actual.getHeaders().getContentType()).isEqualTo(MediaType.MULTIPART_FORM_DATA);
        Assertions.assertThat(actual.getHeaders().getContentLength()).isEqualTo(file.getSize());

        assertDoesNotThrow(() -> Assertions.assertThat(actual.getBody().getInputStream().readAllBytes()).isEqualTo((file.getBytes())));
    }
}