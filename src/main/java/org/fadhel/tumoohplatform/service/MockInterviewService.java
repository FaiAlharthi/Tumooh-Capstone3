package org.fadhel.tumoohplatform.service;

import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.dto.in.MockInterviewRequest;
import org.fadhel.tumoohplatform.dto.out.MockInterviewEvaluationResponse;
import org.fadhel.tumoohplatform.dto.out.MockInterviewResultsResponse;
import org.fadhel.tumoohplatform.dto.out.MockInterviewResponse;
import org.fadhel.tumoohplatform.dto.out.MockInterviewSummaryResponse;
import org.fadhel.tumoohplatform.model.MockInterview;
import org.fadhel.tumoohplatform.model.User;
import org.fadhel.tumoohplatform.repository.MockInterviewRepository;
import org.fadhel.tumoohplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MockInterviewService {

    private final MockInterviewRepository mockInterviewRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public List<MockInterview> getAllMockInterviews() {
        return mockInterviewRepository.findAll();
    }

    public List<MockInterview> getMockInterviewsByUserId(Long userId) {
        return mockInterviewRepository.findByUserId(userId);
    }

    public MockInterview getMockInterviewById(Long id) {
        return mockInterviewRepository.findById(id)
                .orElseThrow(() -> new ApiException("Mock interview not found"));
    }

    public Long createMockInterview(MockInterviewRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ApiException("User not found"));

        MockInterview mockInterview = new MockInterview();
        mockInterview.setJobTitle(request.getJobTitle());
        mockInterview.setUser(user);
        mockInterview.setCreatedAt(LocalDateTime.now());

        return mockInterviewRepository.save(mockInterview).getId();
    }

    public void updateMockInterviewResult(Long id, MockInterviewRequest request) {
        MockInterview mockInterview = getMockInterviewById(id);
        mockInterview.setJobTitle(request.getJobTitle());
        mockInterviewRepository.save(mockInterview);
    }

    public void deleteMockInterview(Long id) {
        mockInterviewRepository.delete(getMockInterviewById(id));
    }

    public void saveMockInterviewEvaluation(Long id, MockInterviewEvaluationResponse dto) {
        MockInterview mockInterview = getMockInterviewById(id);
        if (dto.getAiScore() != null) {
            mockInterview.setAiScore(dto.getAiScore());
        }
        if (dto.getSpeechClarity() != null) {
            mockInterview.setSpeechClarity(dto.getSpeechClarity());
        }
        if (dto.getStrengths() != null) {
            mockInterview.setStrengths(dto.getStrengths());
        }
        if (dto.getWeaknesses() != null) {
            mockInterview.setWeaknesses(dto.getWeaknesses());
        }
        if (dto.getBodyLanguageTips() != null) {
            mockInterview.setBodyLanguageTips(dto.getBodyLanguageTips());
        }
        mockInterviewRepository.save(mockInterview);
    }

    public MockInterviewResultsResponse getMockInterviewResults(Long id) {
        MockInterview session = getMockInterviewById(id);
        boolean available = session.getAiScore() != null;
        return new MockInterviewResultsResponse(
                session.getId(),
                session.getJobTitle(),
                session.getCreatedAt(),
                session.getAiScore(),
                session.getSpeechClarity(),
                session.getStrengths(),
                session.getWeaknesses(),
                session.getBodyLanguageTips(),
                available,
                available ? null : "Evaluation not completed yet.");
    }

    public List<MockInterviewResponse> getMockInterviewResponsesByUserId(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new ApiException("User not found"));
        return getMockInterviewsByUserId(userId).stream()
                .sorted(Comparator.comparing(MockInterview::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::mapToMockInterviewResponse)
                .toList();
    }

    public MockInterviewSummaryResponse getMockInterviewSummaryByUserId(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new ApiException("User not found"));
        List<MockInterview> sessions = getMockInterviewsByUserId(userId);
        long total = sessions.size();
        LocalDateTime lastAt = sessions.stream()
                .map(MockInterview::getCreatedAt)
                .filter(d -> d != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
        Double avg = sessions.stream()
                .map(MockInterview::getAiScore)
                .filter(s -> s != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(Double.NaN);
        return new MockInterviewSummaryResponse(
                userId,
                total,
                Double.isNaN(avg) ? null : avg,
                lastAt);
    }

    public void sendMockInterviewFeedbackEmail(Long id) {
        MockInterview session = getMockInterviewById(id);
        if (session.getAiScore() == null) {
            throw new ApiException("Complete AI evaluation before emailing feedback.");
        }
        User user = session.getUser();
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ApiException("User email is not available.");
        }
        emailService.sendTemplatedEmail(
                user.getEmail(),
                "Your Tumooh mock interview feedback",
                "email/mock-interview-feedback",
                Map.of(
                        "emailTitle", "Mock interview feedback",
                        "emailSubtitle", "Your AI evaluation summary",
                        "jobTitle", nullToDash(session.getJobTitle()),
                        "aiScore", session.getAiScore(),
                        "speechClarity", session.getSpeechClarity() != null ? session.getSpeechClarity() : "—",
                        "strengths", nullToDash(session.getStrengths()),
                        "weaknesses", nullToDash(session.getWeaknesses()),
                        "bodyLanguageTips", nullToDash(session.getBodyLanguageTips())));
    }

    private MockInterviewResponse mapToMockInterviewResponse(MockInterview session) {
        MockInterviewResponse response = new MockInterviewResponse();
        response.setId(session.getId());
        response.setUserId(session.getUser() != null ? session.getUser().getId() : null);
        response.setJobTitle(session.getJobTitle());
        response.setAudioFilePath(session.getAudioFilePath());
        response.setAiScore(session.getAiScore());
        response.setSpeechClarity(session.getSpeechClarity());
        response.setStrengths(session.getStrengths());
        response.setWeaknesses(session.getWeaknesses());
        response.setBodyLanguageTips(session.getBodyLanguageTips());
        response.setCreatedAt(session.getCreatedAt());
        return response;
    }

    private static String nullToDash(String value) {
        return value != null ? value : "—";
    }
}
