package com.karlacastilho.apiseletivo.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private final AppUserRepository userRepo;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final long refreshDays;

    public AuthService(AppUserRepository userRepo,
                       RefreshTokenRepository refreshRepo,
                       PasswordEncoder encoder,
                       JwtService jwtService,
                       @Value("${security.jwt.refreshTokenDays}") long refreshDays) {
        this.userRepo = userRepo;
        this.refreshRepo = refreshRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.refreshDays = refreshDays;
    }

    public void register(String username, String password) {
        if (userRepo.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já existe");
        }
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(password));
        u.setRole("USER");
        userRepo.save(u);
    }

    public AuthDtos.TokenResponse login(String username, String password) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        String access = jwtService.generateAccessToken(user);
        String refresh = issueRefreshToken(user);

        return new AuthDtos.TokenResponse(access, refresh);
    }

    public AuthDtos.TokenResponse refresh(String refreshToken) {
        RefreshToken rt = refreshRepo.findByToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido"));

        if (rt.isRevoked() || rt.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado/revogado");
        }

        AppUser user = rt.getUser();
        String access = jwtService.generateAccessToken(user);

        // rotação simples: emite novo refresh e revoga o anterior
        rt.setRevoked(true);
        refreshRepo.save(rt);

        String newRefresh = issueRefreshToken(user);
        return new AuthDtos.TokenResponse(access, newRefresh);
    }

    private String issueRefreshToken(AppUser user) {
        String token = UUID.randomUUID().toString() + "." + UUID.randomUUID();

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(token);
        rt.setExpiresAt(Instant.now().plus(refreshDays, ChronoUnit.DAYS));
        rt.setRevoked(false);

        refreshRepo.save(rt);
        return token;
    }
}