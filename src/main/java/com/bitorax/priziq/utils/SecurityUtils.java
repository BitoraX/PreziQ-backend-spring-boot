package com.bitorax.priziq.utils;

import com.bitorax.priziq.constant.RoleType;
import com.bitorax.priziq.constant.TokenType;
import com.bitorax.priziq.dto.response.auth.AuthenticationResponse;
import com.bitorax.priziq.domain.User;
import com.bitorax.priziq.exception.AppException;
import com.bitorax.priziq.exception.ErrorCode;
import com.bitorax.priziq.mapper.UserMapper;
import com.bitorax.priziq.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityUtils {
    UserRepository userRepository;
    UserMapper userMapper;

    @NonFinal
    @Value("${jwt.access-signer-key}")
    protected String ACCESS_SIGNER_KEY;

    @NonFinal
    @Value("${jwt.refresh-signer-key}")
    protected String REFRESH_SIGNER_KEY;

    @NonFinal
    @Value("${jwt.access-token-duration-in-seconds}")
    protected long ACCESS_TOKEN_EXPIRATION;

    @NonFinal
    @Value("${jwt.refresh-token-duration-in-seconds}")
    protected long REFRESH_TOKEN_EXPIRATION;

    public ResponseEntity<AuthenticationResponse> createAuthResponse(User currentUser) {
        if (Objects.isNull(currentUser))
            throw new AppException(ErrorCode.USER_NOT_FOUND);

        // Add information about current user login to response and create access token
        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .userSecured(userMapper.userToSecureResponse(currentUser))
                .accessToken(this.generateAccessToken(currentUser))
                .build();

        // Create refresh token and update refresh token to User entity
        String refreshToken = this.generateRefreshToken(currentUser);
        this.updateUserRefreshToken(refreshToken, currentUser.getEmail());

        // Set refresh token to cookies
        ResponseCookie resCookies = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true) // avoid javascript (client) to access cookies
                .secure(true) // use HTTPS
                .sameSite(String.valueOf(Cookie.SameSite.NONE)) // LAX
                .path("/")
                .maxAge(REFRESH_TOKEN_EXPIRATION)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookies.toString()).body(authResponse);
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        String userId = authentication.getName();
        return userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public boolean isAdmin(User user) {
        return user.getRoles() != null
                && user.getRoles().stream().anyMatch(role -> RoleType.ADMIN_ROLE.getName().equals(role.getName()));
    }

    private String generateToken(User user, String keyType) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("priziq") // domain
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now()
                        .plus(Objects.equals(keyType, TokenType.ACCESS_TOKEN.getKey()) ? ACCESS_TOKEN_EXPIRATION
                                : REFRESH_TOKEN_EXPIRATION, ChronoUnit.SECONDS)
                        .toEpochMilli()))
                .claim("email", user.getEmail())
                .jwtID(UUID.randomUUID().toString())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(
                    (Objects.equals(keyType, TokenType.ACCESS_TOKEN.getKey()) ? ACCESS_SIGNER_KEY : REFRESH_SIGNER_KEY)
                            .getBytes()));
            return jwsObject.serialize(); // Convert jwsObject to string
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateAccessToken(User user) {
        return generateToken(user, TokenType.ACCESS_TOKEN.getKey());
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, TokenType.REFRESH_TOKEN.getKey());
    }

    private SignedJWT verifyToken(String token, String keyType) throws JOSEException, ParseException {
        if (token == null || token.trim().isEmpty()) {
            throw new AppException(ErrorCode.MISSING_TOKEN);
        }

        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(token);
        } catch (ParseException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        JWSVerifier verifier = new MACVerifier(
                (Objects.equals(keyType, TokenType.ACCESS_TOKEN.getKey()) ? ACCESS_SIGNER_KEY : REFRESH_SIGNER_KEY)
                        .getBytes());
        boolean isVerified = signedJWT.verify(verifier);
        if (!isVerified) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expiryTime.before(new Date())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        return signedJWT;
    }

    public SignedJWT verifyAccessToken(String token) throws ParseException, JOSEException {
        return verifyToken(token, TokenType.ACCESS_TOKEN.getKey());
    }

    public SignedJWT verifyRefreshToken(String token) throws JOSEException, ParseException {
        return verifyToken(token, TokenType.REFRESH_TOKEN.getKey());
    }

    public void updateUserRefreshToken(String refreshToken, String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        currentUser.setRefreshToken(refreshToken);
        userRepository.save(currentUser);
    }

    public static String getCurrentUserEmailFromJwt() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof Jwt)
                .map(principal -> (Jwt) principal)
                .map(jwt -> jwt.getClaimAsString("email"))
                .orElse("system");
    }

    public void enforceProtectedEmailPolicy(String email) {
        List<String> protectedEmails = Arrays.asList(
                "priziq.admin@gmail.com",
                "priziq.user@gmail.com");

        if (protectedEmails.contains(email))
            throw new AppException(ErrorCode.SYSTEM_EMAIL_CANNOT_BE_DELETED);
    }
}
