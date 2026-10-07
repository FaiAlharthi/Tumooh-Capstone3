package org.fadhel.tumoohplatform.service;

import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.dto.in.MockInterviewRequest;
import org.fadhel.tumoohplatform.dto.out.MockInterviewStartResponse;
import org.fadhel.tumoohplatform.model.MockInterview;
import org.fadhel.tumoohplatform.repository.MockInterviewRepository;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MockInterviewSessionService {

    private final MockInterviewService mockInterviewService;
    private final MockInterviewRepository mockInterviewRepository;
    private final MockInterviewQuestionService mockInterviewQuestionService;
    private final ObjectMapper objectMapper;

    public MockInterviewStartResponse startMockInterview(MockInterviewRequest request) {
        Long id = mockInterviewService.createMockInterview(request);
        List<String> questions = loadOrCreateSessionQuestions(id);
        MockInterview session = mockInterviewService.getMockInterviewById(id);
        return new MockInterviewStartResponse(id, session.getJobTitle(), questions);
    }

    public List<String> loadOrCreateSessionQuestions(Long id) {
        MockInterview mockInterview = mockInterviewService.getMockInterviewById(id);
        if (mockInterview.getSessionQuestions() != null && !mockInterview.getSessionQuestions().isBlank()) {
            try {
                return objectMapper.readValue(mockInterview.getSessionQuestions(), new TypeReference<List<String>>() {});
            } catch (Exception ignored) {
            }
        }
        List<String> questions = mockInterviewQuestionService.generateQuestionsForJob(mockInterview.getJobTitle());
        try {
            mockInterview.setSessionQuestions(objectMapper.writeValueAsString(questions));
            mockInterviewRepository.save(mockInterview);
        } catch (Exception e) {
            throw new ApiException("Could not store session questions");
        }
        return questions;
    }

    public void saveSessionTelemetry(Long id, String telemetry) {
        MockInterview mockInterview = mockInterviewService.getMockInterviewById(id);
        mockInterview.setSessionTelemetry(telemetry);
        mockInterviewRepository.save(mockInterview);
    }

    public String getSessionTelemetry(Long id) {
        return mockInterviewService.getMockInterviewById(id).getSessionTelemetry();
    }

    public List<String> getSessionQuestions(MockInterview mockInterview) {
        if (mockInterview.getSessionQuestions() == null || mockInterview.getSessionQuestions().isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(mockInterview.getSessionQuestions(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
