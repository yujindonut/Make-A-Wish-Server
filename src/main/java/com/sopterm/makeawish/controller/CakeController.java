package com.sopterm.makeawish.controller;

import com.sopterm.makeawish.common.ApiResponse;
import com.sopterm.makeawish.domain.Cake;
import com.sopterm.makeawish.domain.wish.Wish;
import com.sopterm.makeawish.dto.cake.*;
import com.sopterm.makeawish.dto.present.PresentDto;
import com.sopterm.makeawish.dto.present.PresentResponseDto;
import com.sopterm.makeawish.service.CakeService;
import com.sopterm.makeawish.service.WishService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import static com.sopterm.makeawish.common.message.ErrorMessage.NULL_PRINCIPAL;
import static com.sopterm.makeawish.common.message.SuccessMessage.*;
import static java.util.Objects.isNull;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cakes")
public class CakeController {

    private final CakeService cakeService;
    private final WishService wishService;

    @Operation(summary = "케이크 리스트 조회")
    @GetMapping
    public ResponseEntity<ApiResponse> getAllCakes() {
        List<CakeResponseDTO> response = cakeService.getAllCakes();
        return ResponseEntity.ok(ApiResponse.success(SUCCESS_GET_ALL_CAKE.getMessage(), response));
    }

    @Operation(summary = "카카오페이 결제 준비")
    @PostMapping("/pay/ready")
    public ResponseEntity<ApiResponse> getKakaoPayReady(@RequestBody CakeReadyRequestDto request) {
        CakeReadyResponseDto response = cakeService.getKakaoPayReady(request);
        return ResponseEntity.ok(ApiResponse.success(SUCCESS_GET_READY_KAKAOPAY.getMessage(), response));
    }

    @Operation(summary = "카카오페이 결제 승인 및 선물 저장")
    @PostMapping("/pay/approve")
    public ResponseEntity<ApiResponse> createCake(@RequestBody CakeApproveRequestDto request) {
        Cake cake = cakeService.findById(request.cake());
        if (cake.getId() != 1) {
            CakeApproveResponseDto response = cakeService.getKakaoPayApprove(request);
        }
        Wish wish = wishService.getWish(request.wishId());
        CakeCreateResponseDto response = cakeService.createPresent(request.name(), cake, wish, request.message());
        return ResponseEntity.ok(ApiResponse.success(SUCCESS_CREATE_CAKE.getMessage(), response));
    }

    @Operation(summary = "카카오페이 결제 준비 후 pg 토큰 발급")
    @GetMapping("/pay/approve")
    public ResponseEntity<ApiResponse> getPgToken(@RequestParam("pg_token") String pgToken) {
        return ResponseEntity.ok(ApiResponse.success(SUCCESS_GET_PGTOKEN.getMessage(), pgToken));
    }

    @Operation(summary = "해당 소원에 대한 케이크 결과 조회")
    @GetMapping("/{wishId}")
    public ResponseEntity<ApiResponse> getPresents(Principal principal, @PathVariable("wishId") Long wishId) {
        Long userId = getUserId(principal);
        List<PresentDto> response = cakeService.getPresents(userId, wishId);
        return ResponseEntity.ok(ApiResponse.success(SUCCESS_GET_ALL_PRESENT.getMessage(), response));
    }

    @Operation(summary = "해당 소원에 대한 케이크 조회")
    @GetMapping("/{wishId}/{cakeId}")
    public ResponseEntity<ApiResponse> getEachPresent(Principal principal, @PathVariable("wishId") Long wishId, @PathVariable("cakeId") Long cakeId) {
        Long userId = getUserId(principal);
        List<PresentResponseDto> response = cakeService.getEachPresent(userId, wishId, cakeId);
        return ResponseEntity.ok(ApiResponse.success(SUCCESS_GET_PRESENT_MESSAGE.getMessage(), response));
    }


    private Long getUserId(Principal principal) {
        if (isNull(principal)) {
            throw new IllegalArgumentException(NULL_PRINCIPAL.getMessage());
        }
        return Long.valueOf(principal.getName());
    }
}
