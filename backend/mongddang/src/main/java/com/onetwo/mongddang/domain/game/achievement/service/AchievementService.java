package com.onetwo.mongddang.domain.game.achievement.service;

import com.onetwo.mongddang.common.responseDto.ResponseDto;
import com.onetwo.mongddang.domain.game.achievement.dto.RequestAchievementListDto;
import com.onetwo.mongddang.domain.game.achievement.errors.CustomAchievementErrorCode;
import com.onetwo.mongddang.domain.game.achievement.model.Achievement;
import com.onetwo.mongddang.domain.game.achievement.repository.AchievementRepository;
import com.onetwo.mongddang.domain.game.gameLog.application.GameLogUtils;
import com.onetwo.mongddang.domain.game.gameLog.errors.CustomGameLogErrorCode;
import com.onetwo.mongddang.domain.game.gameLog.model.GameLog;
import com.onetwo.mongddang.domain.game.gameLog.repository.GameLogRepository;
import com.onetwo.mongddang.domain.game.title.model.MyTitle;
import com.onetwo.mongddang.domain.game.title.model.Title;
import com.onetwo.mongddang.domain.game.title.repository.MyTitleRepository;
import com.onetwo.mongddang.domain.game.title.repository.TitleRepository;
import com.onetwo.mongddang.domain.user.error.CustomUserErrorCode;
import com.onetwo.mongddang.domain.user.model.User;
import com.onetwo.mongddang.domain.user.repository.UserRepository;
import com.onetwo.mongddang.errors.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final TitleRepository titleRepository;
    private final MyTitleRepository myTitleRepository;
    private final UserRepository userRepository;
    private final GameLogRepository gameLogRepository;
    private final GameLogUtils gameLogUtils;

    // 업적 목록 조회
    @Transactional
    public ResponseDto getAchievementList(Long childId) {
        log.info("getAchievementList childId: {}", childId);

        List<Achievement> achievementList = achievementRepository.findAll();
        List<RequestAchievementListDto> achievementListDto = achievementList.stream()
                .map(achievement -> {
                    // 업적에 해당하는 칭호 조회
                    Title title = titleRepository.findById(achievement.getId()).orElse(null);

                    // 유저의 게임 로그 조회
                    GameLog gameLog = gameLogRepository.findTopByChildIdOrderByIdDesc(childId)
                            .orElseThrow(() -> new RestApiException(CustomGameLogErrorCode.GAME_LOG_NOT_FOUND));
                    MyTitle myTitle = myTitleRepository.findByTitleId(title.getId());

                    // 업적 달성 횟수
                    int executionCount = gameLogUtils.getGameLogCountByCategory(childId, achievement.getCategory());

                    // 업적 달성 여부 -1: 달성하지 않음
                    boolean isAchieved = executionCount != -1;
                    return RequestAchievementListDto.builder()
                            .titleId(title.getId())
                            .titleName(title.getName())
                            .description(achievement.getDescription())
                            .executionCount(executionCount)
                            .count(achievement.getCount())
                            .category(achievement.getCategory())
                            .isOwned(isAchieved)
                            .isNew(isAchieved ? myTitle.getIsNew() : false)
                            .isMain(isAchieved ? myTitle.getIsMain() : false)
                            .build();
                })
                .toList();

        ResponseDto responseDto = ResponseDto.builder()
                .message("업적 목록 조회에 성공했습니다.")
                .data(achievementListDto)
                .build();

        return responseDto;
    }

    // 업적 보상 수령
    @Transactional
    public ResponseDto claimAchievementReward(Long userId, Long achievementId) {
        log.info("claimAchievementReward userId: {}, achievementId: {}", userId, achievementId);

        User user = userRepository.findById(userId).orElseThrow(() -> new RestApiException(CustomUserErrorCode.USER_NOT_FOUND));
        Achievement achievement = achievementRepository.findById(achievementId).orElseThrow(() -> new RestApiException(CustomAchievementErrorCode.INVALID_ACHIEVEMENT_ID));
        Title title = titleRepository.findByAchievementId(achievement
                .getId()).orElseThrow(() -> new RestApiException(CustomAchievementErrorCode.INVALID_ACHIEVEMENT_ID));
        MyTitle myTitle = myTitleRepository.findByTitleId(title.getId());
        if (myTitle != null) {
            throw new RestApiException(CustomAchievementErrorCode.ACHIEVEMENT_ALREADY_REWARDED);
        }

        // 업적 달성 횟수
        int executionCount = gameLogUtils.getGameLogCountByCategory(userId, achievement.getCategory());

        if (executionCount < achievement.getCount()) {
            throw new RestApiException(CustomAchievementErrorCode.ACHIEVEMENT_NOT_UNLOCKED);
        }

        MyTitle newMyTitle = MyTitle.builder()
                .child(user)
                .title(title)
                .isNew(true)
                .isMain(false)
                .createdAt(LocalDateTime.now())
                .build();

        // 칭호 소유 처리
        myTitleRepository.save(newMyTitle);

        ResponseDto responseDto = ResponseDto.builder()
                .message("업적 보상 수령에 성공했습니다.")
                .build();

        return responseDto;
    }

    // 메인 업적 설정
}