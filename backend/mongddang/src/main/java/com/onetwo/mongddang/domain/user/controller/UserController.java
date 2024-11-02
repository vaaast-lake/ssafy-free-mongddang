package com.onetwo.mongddang.domain.user.controller;

import com.onetwo.mongddang.common.responseDto.ResponseDto;
import com.onetwo.mongddang.domain.user.dto.CheckNicknameRequestDto;
import com.onetwo.mongddang.domain.user.jwt.JwtExtratService;
import com.onetwo.mongddang.domain.user.repository.UserRepository;
import com.onetwo.mongddang.domain.user.service.ModifyUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
@Tag(name = "User API", description = "사용자 정보 관련 api")
public class UserController {

    private final JwtExtratService jwtExtratService;
    private final ModifyUserService modifyUserService;

    @PostMapping("/modify")
    @Operation(summary = "유저 정보 수정 API", description = "유저 정보를 수정합니다. 현재 닉네임만 수정됩니다.")
    public ResponseEntity<ResponseDto> modifyUser(@Valid @RequestBody CheckNicknameRequestDto checkNicknameRequestDto, HttpServletRequest request){
        Long userId = jwtExtratService.jwtFindId(request);
        ResponseDto response = modifyUserService.modifyUser(checkNicknameRequestDto,userId);
        return ResponseEntity.ok(response);
    }
}