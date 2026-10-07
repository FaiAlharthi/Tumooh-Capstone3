package org.fadhel.tumoohplatform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiResponse;
import org.fadhel.tumoohplatform.dto.in.GmailConnectRequest;
import org.fadhel.tumoohplatform.service.GmailConnectionService;
import org.fadhel.tumoohplatform.service.GmailSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gmail")
@RequiredArgsConstructor
public class GmailController {

    private final GmailConnectionService gmailConnectionService;
    private final GmailSyncService gmailSyncService;

    @PostMapping("/connect/{userId}")
    public ResponseEntity<?> connectGmail(@PathVariable Long userId, @RequestBody @Valid GmailConnectRequest request) {
        gmailConnectionService.connectGmail(userId, request);
        return ResponseEntity.status(200).body(new ApiResponse("Gmail connected successfully"));
    }

    //to try email connection anytime
    @PostMapping("/sync/{userId}")
    public ResponseEntity<?> syncGmail(@PathVariable Long userId) {
        return ResponseEntity.status(200).body(new ApiResponse(gmailSyncService.syncUser(userId)));
    }
}