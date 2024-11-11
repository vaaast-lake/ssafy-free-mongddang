package com.onetwo.mongddang.domain.record.service;

import com.onetwo.mongddang.common.responseDto.ResponseDto;
import com.onetwo.mongddang.common.s3.S3ImageService;
import com.onetwo.mongddang.common.utils.DateTimeUtils;
import com.onetwo.mongddang.common.utils.JsonUtils;
import com.onetwo.mongddang.domain.game.gameLog.application.GameLogUtils;
import com.onetwo.mongddang.domain.game.gameLog.model.GameLog;
import com.onetwo.mongddang.domain.missionlog.application.MissionLogUtils;
import com.onetwo.mongddang.domain.missionlog.dto.MissionDto;
import com.onetwo.mongddang.domain.record.errors.CustomRecordErrorCode;
import com.onetwo.mongddang.domain.record.model.Record;
import com.onetwo.mongddang.domain.record.repository.RecordRepository;
import com.onetwo.mongddang.domain.user.application.CtoPUtils;
import com.onetwo.mongddang.domain.user.error.CustomUserErrorCode;
import com.onetwo.mongddang.domain.user.model.User;
import com.onetwo.mongddang.domain.user.repository.UserRepository;
import com.onetwo.mongddang.errors.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.onetwo.mongddang.domain.record.model.Record.RecordCategoryType.exercise;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordExerciseService {

    
    private final RecordRepository recordRepository;
    private final UserRepository userRepository;
    private final CtoPUtils ctoPUtils;
    private final DateTimeUtils dateTimeUtils;
    private final S3ImageService s3ImageService;
    private final JsonUtils jsonUtils;
    private final MissionLogUtils missionLogUtils;
    private final GameLogUtils gameLogUtils;


    /**
     * 운동 시작하기
     *
     * @param childId 운동 시작을 시도하는 어린이의 아이디
     * @return ResponseDto
     */
    @Transactional
    public ResponseDto startExercise(Long childId) {
        log.info("startExercise childId: {}", childId);

        User child = userRepository.findById(childId)
                .orElseThrow(() -> new RestApiException(CustomUserErrorCode.USER_NOT_FOUND));
        log.info("child: {}", child.getEmail());

        // 가장 최근에 시작된 운동 기록
        Optional<Record> lastedExerciseRecord = recordRepository.findTopByChildAndCategoryAndEndTimeIsNullOrderByIdDesc(child, exercise);

        // 이미 시작된 운동이 있는지 확인
        if (lastedExerciseRecord.isPresent()) {
            throw new RestApiException(CustomRecordErrorCode.EXERCISE_ALREADY_STARTED);
        }

        // 운동 시작 시간 기록
        Record exerciseRecord = Record.builder()
                .child(child)
                .category(exercise)
                .startTime(LocalDateTime.now())
                .endTime(null)
                .content(null)
                .imageUrl(null)
                .isDone(false)
                .mealTime(null)
                .build();

        recordRepository.save(exerciseRecord);

        // 미션 업데이트
        missionLogUtils.completeMission(child, MissionDto.Mission.exercise);

        // 게임 로그 업데이트
        gameLogUtils.addGameLog(child, GameLog.GameLogCategory.exercise_count);

        return ResponseDto.builder()
                .message("운동을 시작합니다.")
                .build();

    }

    /**
     * 운동 종료하기
     *
     * @param childId 운동 종료를 시도하는 어린이의 아이디
     * @return ResponseDto
     */
    @Transactional
    public ResponseDto endExercise(Long childId) {
        log.info("endExercise childId: {}", childId);

        User child = userRepository.findById(childId)
                .orElseThrow(() -> new RestApiException(CustomUserErrorCode.USER_NOT_FOUND));
        log.info("child: {}", child.getEmail());

        // 가장 최근에 시작된 운동 기록 조회
        Optional<Record> lastedExerciseRecord = recordRepository.findTopByChildAndCategoryAndEndTimeIsNullOrderByIdDesc(child, exercise);
        log.info("가장 최근에 시작된 운동 기록 조회 성공");

        // 운동 중인지 확인
        log.info("이미 시작된 운동 기록 확인");
        if (lastedExerciseRecord.isEmpty()) {
            throw new RestApiException(CustomRecordErrorCode.EXERCISE_NOT_STARTED);
        }

        // 운동 종료 시간 기록
        Record exerciseRecord = lastedExerciseRecord.get();
        exerciseRecord.setEndTime(LocalDateTime.now());
        exerciseRecord.setIsDone(true);

        recordRepository.save(exerciseRecord);
        log.info("운동 종료 기록 완료. 종료시간 : {}", exerciseRecord.getEndTime());

        return ResponseDto.builder()
                .message("운동을 종료합니다.")
                .build();
    }

}
