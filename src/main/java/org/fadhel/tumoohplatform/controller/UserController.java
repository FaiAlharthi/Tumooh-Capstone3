package org.fadhel.tumoohplatform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiResponse;
import org.fadhel.tumoohplatform.dto.in.UserRequest;
import org.fadhel.tumoohplatform.dto.out.InterviewUpcomingResponse;
import org.fadhel.tumoohplatform.dto.out.MockInterviewSummaryResponse;
import org.fadhel.tumoohplatform.dto.out.UserResponse;
import org.fadhel.tumoohplatform.service.InterviewService;
import org.fadhel.tumoohplatform.service.MockInterviewService;
import org.fadhel.tumoohplatform.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MockInterviewService mockInterviewService;
    private final InterviewService interviewService;

    @GetMapping("/get")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.mapToUserResponse(userService.getUserById(id)));
    }

    @PostMapping("/add")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserRequest userRequest) {
        userService.registerUser(userRequest);
        return ResponseEntity.status(200).body(new ApiResponse("User added"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody @Valid UserRequest userRequest) {
        userService.updateUser(id, userRequest);
        return ResponseEntity.status(200).body(new ApiResponse("User updated"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.status(200).body(new ApiResponse("User deleted"));
    }

    @GetMapping("/{userId}/mock-interviews/summary")
    public ResponseEntity<MockInterviewSummaryResponse> mockInterviewSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(mockInterviewService.getMockInterviewSummaryByUserId(userId));
    }

    @GetMapping("/{userId}/interviews/upcoming")
    public ResponseEntity<List<InterviewUpcomingResponse>> upcomingInterviews(
            @PathVariable Long userId,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "status", required = false) String status) {
        return ResponseEntity.ok(interviewService.getUpcomingInterviewsByUserId(userId, from, status));
    }
}