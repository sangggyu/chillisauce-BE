package com.example.chillisauce.spaces.controller;

import com.example.chillisauce.message.ResponseMessage;
import com.example.chillisauce.security.UserDetailsImpl;
import com.example.chillisauce.spaces.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class LocationController {
    private final LocationService locationService;
    @PatchMapping("/locations/{companyName}/{locationId}")
    public ResponseEntity<ResponseMessage<String>> moveBoxWithUser(@PathVariable String companyName, @PathVariable Long locationId, @AuthenticationPrincipal UserDetailsImpl details) {
        locationService.moveWithUser(companyName,locationId, details);
        return ResponseMessage.responseSuccess("사용자 등록 및 이동 완료", "");

    }
}
