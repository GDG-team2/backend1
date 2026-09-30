package com.walkmission.domain.user.service;

import com.walkmission.domain.auth.dto.LoginRequest;
import com.walkmission.domain.auth.dto.LoginResponse;
import com.walkmission.domain.auth.dto.ReissueRequest;
import com.walkmission.domain.auth.dto.ReissueResponse;
import com.walkmission.domain.auth.dto.SignupRequest;
import com.walkmission.domain.auth.dto.SignupResponse;
import com.walkmission.domain.user.entity.User;
import com.walkmission.domain.user.entity.UserMissionPreference;
import com.walkmission.domain.user.entity.UserProfile;
import com.walkmission.domain.user.entity.UserSetting;
import com.walkmission.domain.user.repository.UserMissionPreferenceRepository;
import com.walkmission.domain.user.repository.UserProfileRepository;
import com.walkmission.domain.user.repository.UserRepository;
import com.walkmission.domain.user.repository.UserSettingRepository;
import com.walkmission.global.auth.JwtProvider;
import com.walkmission.global.error.BusinessException;
import com.walkmission.global.error.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserMissionPreferenceRepository preferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(UserRepository userRepository, UserSettingRepository userSettingRepository,
                       UserProfileRepository userProfileRepository, UserMissionPreferenceRepository preferenceRepository,
                       PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.userSettingRepository = userSettingRepository;
        this.userProfileRepository = userProfileRepository;
        this.preferenceRepository = preferenceRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        
        if (request.password().length() < 8 || !request.password().matches(".*[a-zA-Z].*") || !request.password().matches(".*\\d.*")) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.email(), encodedPassword, request.nickname(), request.birth(), request.regionCode());
        user = userRepository.save(user);

        userSettingRepository.save(new UserSetting(user));
        userProfileRepository.save(new UserProfile(user));
        preferenceRepository.save(new UserMissionPreference(user));

        return new SignupResponse(user.getUserUuid(), user.getNickname(), "회원가입이 성공적으로 완료되었습니다.");
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getUserUuid());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        return new LoginResponse(accessToken, refreshToken, user.getUserUuid(), user.getNickname());
    }

    public ReissueResponse reissue(ReissueRequest request) {
        Long userId;
        try {
            Claims claims = jwtProvider.getClaims(request.refreshToken());
            if (!JwtProvider.REFRESH_TOKEN_TYPE.equals(claims.get(JwtProvider.TOKEN_TYPE_CLAIM, String.class))) {
                throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
            }
            userId = Long.parseLong(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getUserUuid());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        return new ReissueResponse(accessToken, refreshToken);
    }
}
