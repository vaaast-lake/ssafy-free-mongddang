package com.onetwo.mongddang.domain.user.service;

import com.onetwo.mongddang.common.responseDto.ResponseDto;
import com.onetwo.mongddang.domain.game.coinLog.application.CoinLogUtils;
import com.onetwo.mongddang.domain.game.coinLog.model.CoinLog;
import com.onetwo.mongddang.domain.game.coinLog.repository.CoinLogRepository;
import com.onetwo.mongddang.domain.game.gameLog.application.GameLogUtils;
import com.onetwo.mongddang.domain.user.dto.SignupRequestDto;
import com.onetwo.mongddang.domain.user.error.CustomUserErrorCode;
import com.onetwo.mongddang.domain.user.jwt.JwtTokenProvider;
import com.onetwo.mongddang.domain.user.model.User;
import com.onetwo.mongddang.domain.user.oauth.GoogleTokenService;
import com.onetwo.mongddang.domain.user.repository.UserRepository;
import com.onetwo.mongddang.errors.exception.RestApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupService {

    private final UserRepository userRepository;
    private final GoogleTokenService googleTokenService;
    private final MakeRandomCodeService makeRandomCodeService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GameLogUtils gameLogUtils;
    private final CoinLogUtils coinLogUtils;
    private final CoinLogRepository coinLogRepository;

    @Transactional
    public ResponseDto signup(SignupRequestDto signupRequestDto){

        // idtoken에서 회원 정보 뽑기(email)
        String idToken = signupRequestDto.getIdToken();
        String email = googleTokenService.getEmailFromIdToken(idToken);

        // 이미 있는 사용자인지 검사
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RestApiException(CustomUserErrorCode.USER_IS_EXISTS);
        }

        // 회원 가입
        User newUser = signup(signupRequestDto, email);

        if ("child".equals(newUser.getRole().toString())) {
            log.info(newUser.getId().toString());
            // 게임 로그 초기화
            initializeGameLog(newUser.getId());
            // 초기 재화 지급
            coinGrant(newUser);
        }

        // jwt token 생성
        String jwtToken = jwtTokenProvider.generateToken(newUser.getEmail(), newUser.getRole().toString(), newUser.getId());
        // response
        Map<Object, Object> data = new HashMap<>();
        data.put("accessToken", jwtToken);

        ResponseDto response = ResponseDto.builder()
                .message("회원가입을 성공했습니다.")
                .data(data)
                .build();

        return response;
    }

    // 회원가입
    @Transactional
    public User signup(SignupRequestDto signupRequestDto,String email){
        User.Role role = signupRequestDto.getRole();
        String invitedCode = null; // 어른은 초대코드 null

        if ("child".equals(role.toString())) {
            // 어린이일 때 초대코드 생성
            invitedCode =  makeRandomCodeService.makeCodeAndCheckUnique();
        }
        // 저장
        User user = User.builder()
                .email(email)
                .birth(signupRequestDto.getBirth())
                .gender(signupRequestDto.getGender())
                .nickname(signupRequestDto.getNickname())
                .name(signupRequestDto.getName())
                .role(role)
                .invitationCode(invitedCode)
                .build();

        return userRepository.save(user);
    }

    // 게임 로그 초기화
    @Transactional
    public void initializeGameLog(Long userId) {
        gameLogUtils.initGameLog(userId);
    }

    // 초기 코인 지급
    @Transactional
    public void coinGrant(User user) {
        CoinLog newCoinLog = CoinLog.builder()
                .coin(500)
                .child(user)
                .category(CoinLog.CoinCategory.achievement)
                .build();
        coinLogRepository.save(newCoinLog);
    }
}