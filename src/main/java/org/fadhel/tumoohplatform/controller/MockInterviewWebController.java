package org.fadhel.tumoohplatform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.dto.in.MockInterviewRequest;
import org.fadhel.tumoohplatform.model.MockInterview;
import org.fadhel.tumoohplatform.service.MockInterviewService;
import org.fadhel.tumoohplatform.service.MockInterviewSessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class MockInterviewWebController {

    private static final long DEFAULT_USER_ID = 1L;

    private final MockInterviewService mockInterviewService;
    private final MockInterviewSessionService mockInterviewSessionService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/mock-interview/setup")
    public String setupForm(@RequestParam(value = "userId", required = false) Long userId, Model model) {
        MockInterviewRequest request = new MockInterviewRequest();
        request.setUserId(userId != null ? userId : DEFAULT_USER_ID);
        model.addAttribute("mockInterviewRequest", request);
        model.addAttribute("userId", request.getUserId());
        return "mock-interview/setup";
    }

    @PostMapping("/mock-interview/setup")
    public String submitSetup(
            @Valid MockInterviewRequest mockInterviewRequest,
            BindingResult bindingResult,
            @RequestParam(value = "userId", required = false) Long userId,
            Model model) {
        if (userId != null) {
            mockInterviewRequest.setUserId(userId);
        }
        if (mockInterviewRequest.getUserId() == null) {
            mockInterviewRequest.setUserId(DEFAULT_USER_ID);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("userId", mockInterviewRequest.getUserId());
            return "mock-interview/setup";
        }

        Long id = mockInterviewService.createMockInterview(mockInterviewRequest);
        return "redirect:/mock-interview/room/" + id;
    }

    @GetMapping("/mock-interview/room/{id}")
    public String room(@PathVariable Long id, Model model) {
        MockInterview session = mockInterviewService.getMockInterviewById(id);
        model.addAttribute("sessionId", id);
        model.addAttribute("jobTitle", session.getJobTitle());
        try {
            model.addAttribute("questions", mockInterviewSessionService.loadOrCreateSessionQuestions(id));
        } catch (ApiException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "mock-interview/error";
        }
        return "mock-interview/room";
    }

    @GetMapping("/mock-interview/feedback/{id}")
    public String feedback(@PathVariable Long id, Model model) {
        MockInterview session = mockInterviewService.getMockInterviewById(id);
        model.addAttribute("sessionId", id);
        model.addAttribute("jobTitle", session.getJobTitle());
        return "mock-interview/feedback";
    }
}
