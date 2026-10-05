package org.fadhel.tumoohplatform.service;

import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.dto.in.MockInterviewRequest;
import org.fadhel.tumoohplatform.model.MockInterview;
import org.fadhel.tumoohplatform.model.User;
import org.fadhel.tumoohplatform.repository.MockInterviewRepository;
import org.fadhel.tumoohplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MockInterviewService {

    private final MockInterviewRepository mockInterviewRepository;
    private final UserRepository userRepository;

    public List<MockInterview> getAllMockInterviews() {
        return mockInterviewRepository.findAll();
    }

    public List<MockInterview> getMockInterviewsByUserId(Long userId) {
        return mockInterviewRepository.findAll().stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .toList();
    }

    public void createMockInterview(MockInterviewRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ApiException("User not found"));

        MockInterview mockInterview = new MockInterview();
        mockInterview.setJobTitle(request.getJobTitle());
        mockInterview.setUser(user);
        mockInterview.setCreatedAt(LocalDateTime.now());

        mockInterviewRepository.save(mockInterview);
    }

    public void updateMockInterviewResult(Long id, MockInterviewRequest request) {
        MockInterview mockInterview = mockInterviewRepository.findById(id)
                .orElseThrow(() -> new ApiException("Mock interview not found"));

        mockInterview.setJobTitle(request.getJobTitle());

        mockInterviewRepository.save(mockInterview);
    }

    public void deleteMockInterview(Long id) {
        MockInterview mockInterview = mockInterviewRepository.findById(id)
                .orElseThrow(() -> new ApiException("Mock interview not found"));
        mockInterviewRepository.delete(mockInterview);
    }
}