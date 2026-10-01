package gg.statikk.auth.service;

import gg.statikk.auth.domain.User;
import gg.statikk.auth.dto.AuthResponse;
import gg.statikk.auth.dto.LoginRequest;
import gg.statikk.auth.dto.RegisterRequest;
import gg.statikk.auth.exception.EmailAlreadyExistsException;
import gg.statikk.auth.exception.InvalidCredentialsException;
import gg.statikk.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new EmailAlreadyExistsException(request.username());
        }

        String hash = passwordEncoder.encode(request.password());
        User user = new User(request.username(), request.email(), hash);
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return AuthResponse.of(token, jwtService.getExpirationMinutes());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);
        return AuthResponse.of(token, jwtService.getExpirationMinutes());
    }
}
