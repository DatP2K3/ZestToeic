package com.zest.toeic.admin.service;

import com.zest.toeic.auth.model.User;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> listUsers(String search, String status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (search != null && !search.isBlank()) {
            return userRepository.findByDisplayNameContainingIgnoreCase(search, pageable);
        }
        if (status != null) {
            return userRepository.findByStatus(status, pageable);
        }
        return userRepository.findAll(pageable);
    }

    public User getUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public User suspendUser(String id, String reason) {
        User user = getUser(id);
        user.setStatus("SUSPENDED");
        return userRepository.save(user);
    }

    public User banUser(String id) {
        User user = getUser(id);
        user.setStatus("BANNED");
        return userRepository.save(user);
    }

    public User activateUser(String id) {
        User user = getUser(id);
        user.setStatus("ACTIVE");
        return userRepository.save(user);
    }
}
