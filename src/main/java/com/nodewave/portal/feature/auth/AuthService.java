package com.nodewave.portal.feature.auth;

import com.nodewave.portal.core.entity.User;
import com.nodewave.portal.core.repository.UserRepository;
import com.nodewave.portal.core.security.JwtTokenProvider;
import com.nodewave.portal.core.security.UserPrincipal;
import com.nodewave.portal.exception.ApiException;
import com.nodewave.portal.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!user.getPassword().equals(request.password())) {
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        String token = tokenProvider.generateToken(user);
        return new LoginResponse("Login successful", token, UserDto.fromEntity(user));
    }

    public UserDto getCurrentUser(UserPrincipal principal) {
        if (principal == null) {
            throw new ApiException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        User user = userRepository.findByIdAndDeletedAtIsNull(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserDto.fromEntity(user);
    }
}
