package org.fadhel.tumoohplatform.service;

import lombok.RequiredArgsConstructor;
import org.fadhel.tumoohplatform.Api.ApiException;
import org.fadhel.tumoohplatform.dto.in.UserRequest;
import org.fadhel.tumoohplatform.dto.out.UserResponse;
import org.fadhel.tumoohplatform.model.User;
import org.fadhel.tumoohplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToUserResponse).toList();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found"));
    }

    public UserResponse mapToUserResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
    }

    public void registerUser(UserRequest userRequest) {
        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setPassword(userRequest.getPassword());
        userRepository.save(user);
    }

    public void updateUser(Long id, UserRequest userRequest) {
        User user = getUserById(id);
        user.setEmail(userRequest.getEmail());
        user.setPassword(userRequest.getPassword());
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.delete(getUserById(id));
    }
}