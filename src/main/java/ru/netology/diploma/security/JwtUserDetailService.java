package ru.netology.diploma.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.netology.diploma.dao.User;
import ru.netology.diploma.security.jwt.JwtUser;
import ru.netology.diploma.security.jwt.JwtUserFactory;
import ru.netology.diploma.service.UserService;

@Service("jwtUserDetailService")
public class JwtUserDetailService implements UserDetailsService {
    private final UserService userService;

    @Autowired
    public JwtUserDetailService(@Qualifier("userServiceImpl") UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username);

        if (user == null)
            throw new UsernameNotFoundException("User with username " + username + " not found");

        JwtUser jwtUser = JwtUserFactory.create(user);
        return jwtUser;
    }

}
