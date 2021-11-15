package ru.netology.diploma.controller;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.netology.diploma.dto.request.AuthenticationRequestDto;
import ru.netology.diploma.dto.response.AuthenticationResponseDto;
import ru.netology.diploma.security.jwt.JwtAuthenticationException;
import ru.netology.diploma.security.jwt.JwtTokenProvider;


import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class AuthenticationControllerTest {

    @InjectMocks
    AuthenticationController authenticationController;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @ParameterizedTest
    @ValueSource(strings = {"user1", "user2", "user3"})
    void login_ok(String login) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        AuthenticationRequestDto authenticationRequestDto = new AuthenticationRequestDto(login, "password");

        when(jwtTokenProvider.createToken(authenticationRequestDto.getLogin())).thenReturn("Bearer_" + authenticationRequestDto.getLogin() + "_token");

        ResponseEntity<AuthenticationResponseDto> responseEntity = authenticationController.login(authenticationRequestDto);

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(responseEntity.getBody().getToken()).isEqualTo("Bearer_" + authenticationRequestDto.getLogin() + "_token");
    }

    @ParameterizedTest
    @ValueSource(strings = {"user1", "user2", "user3"})
    void login_throwBadCredentialsException(String login) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        AuthenticationRequestDto authenticationRequestDto = new AuthenticationRequestDto(login, "password");

        doThrow(new JwtAuthenticationException("authentication exception")).when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        BadCredentialsException e = assertThrows(BadCredentialsException.class, () ->
                authenticationController.login(authenticationRequestDto));

        String message = e.getMessage();
        assertTrue(message.equals("Invalid username or password"));
    }

    @Test
    void logout_ok() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn("Bearer_token");

        ResponseEntity<?> responseEntity = authenticationController.logout(request, response);

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        Mockito.verify(jwtTokenProvider, times(1)).resolveToken(any(HttpServletRequest.class));
        Mockito.verify(jwtTokenProvider, times(1)).addTokenToBlackList(any(String.class));
    }
}