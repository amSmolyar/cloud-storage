package ru.netology.diploma.security.jwt;

import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.netology.diploma.dao.Status;
import ru.netology.diploma.dao.User;

import java.util.List;

@NoArgsConstructor
public final class JwtUserFactory {

    public static JwtUser create(User user) {
        return new JwtUser(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPassword(),
                user.getStatus().equals(Status.ACTIVE),
                user.getUpdated(),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
