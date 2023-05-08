package com.sopterm.makeawish.service;

import com.sopterm.makeawish.common.KakaoPayProperties;
import com.sopterm.makeawish.common.message.ErrorMessage.*;
import com.sopterm.makeawish.domain.Cake;
import com.sopterm.makeawish.domain.Present;
import com.sopterm.makeawish.domain.wish.Wish;
import com.sopterm.makeawish.dto.cake.*;
import com.sopterm.makeawish.repository.CakeRepository;
import com.sopterm.makeawish.repository.PresentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.sopterm.makeawish.common.message.ErrorMessage.INVALID_CAKE;

@Service
@RequiredArgsConstructor
public class CakeService {

    private final CakeRepository cakeRepository;
    private final PresentRepository presentRepository;

    public List<CakeResponseDTO> getAllCakes() {
        return cakeRepository.findAll()
                .stream().map(CakeResponseDTO::from)
                .toList();
    }

    public CakeReadyResponseDto getKakaoPayReady(CakeReadyRequestDto request) {
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(this.getReadyParameters(request), this.getHeaders());

        RestTemplate restTemplate = new RestTemplate();
        CakeReadyResponseDto response = restTemplate.postForObject(
                KakaoPayProperties.readyUrl,
                requestEntity,
                CakeReadyResponseDto.class
        );
        return response;
    }

    private HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        String auth = "KakaoAK " + KakaoPayProperties.adminKey;

        httpHeaders.set("Authorization", auth);
        httpHeaders.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        return httpHeaders;
    }

    private MultiValueMap<String, String> getReadyParameters(CakeReadyRequestDto request) {
        Cake cake = cakeRepository.findById(request.cake()).orElseThrow(EntityNotFoundException::new);

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("cid", KakaoPayProperties.cid);
        parameters.add("partner_order_id", request.partnerOrderId());
        parameters.add("partner_user_id", request.partnerUserId());
        parameters.add("item_name", cake.getName());
        parameters.add("quantity", "1");
        parameters.add("total_amount", String.valueOf(cake.getPrice()));
        parameters.add("vat_amount", request.vatAmount());
        parameters.add("tax_free_amount", request.taxFreeAmount());
        parameters.add("approval_url", request.approvalUrl());
        parameters.add("cancel_url", request.cancelUrl());
        parameters.add("fail_url", request.failUrl());

        return parameters;
    }

    private MultiValueMap<String, String> getApproveParameters(CakeApproveRequestDto request) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("cid", KakaoPayProperties.cid);
        parameters.add("partner_order_id", request.partnerOrderId());
        parameters.add("partner_user_id", request.partnerUserId());
        parameters.add("tid", request.tid());
        parameters.add("pg_token", request.pgToken());

        return parameters;
    }

    public CakeApproveResponseDto getKakaoPayApprove(CakeApproveRequestDto request) {
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(this.getApproveParameters(request), this.getHeaders());

        RestTemplate restTemplate = new RestTemplate();

        CakeApproveResponseDto response = restTemplate.postForObject(
                KakaoPayProperties.approveUrl,
                requestEntity,
                CakeApproveResponseDto.class
        );
        return response;
    }

    @Transactional
    public CakeCreateResponseDto createPresent(String name, Cake cake, Wish wish, String message) {
        Present present = new Present(name, message, wish, cake);
        presentRepository.save(present);
        wish.updateTotalPrice(cake.getPrice());
        String contribute = calculateContribute(cake.getPrice(), wish.getPresentPrice());
        CakeCreateResponseDto response = new CakeCreateResponseDto(cake.getId(), wish.getPresentImageUrl(), wish.getHint1(), wish.getHint2(), contribute, wish.getWisher().getAccount().getName());
        return response;
    }

    private String calculateContribute(int price, int targetPrice) {
        return String.format("%.0f", (double) price / (double) targetPrice * 100);
    }

    public Cake findById(Long id) {
        return cakeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(INVALID_CAKE.getMessage()));
    }
}
