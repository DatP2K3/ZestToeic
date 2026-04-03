package com.zest.toeic.community.service;

import com.zest.toeic.auth.model.User;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.community.model.Squad;
import com.zest.toeic.community.repository.SquadRepository;
import com.zest.toeic.shared.exception.BadRequestException;
import com.zest.toeic.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class SquadService {

    private static final Logger log = LoggerFactory.getLogger(SquadService.class);

    private final SquadRepository squadRepository;
    private final UserRepository userRepository;

    public SquadService(SquadRepository squadRepository, UserRepository userRepository) {
        this.squadRepository = squadRepository;
        this.userRepository = userRepository;
    }

    public Squad createSquad(String userId, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        if (user.getLevel() < 2) {
            throw new BadRequestException("Cần đạt Level 2 (500+ XP) để tạo Squad");
        }

        if (name == null || name.isBlank()) {
            throw new BadRequestException("Tên squad không được để trống");
        }

        Squad squad = Squad.builder()
                .name(name.trim())
                .ownerId(userId)
                .build();

        squad.getMembers().add(Squad.SquadMember.builder()
                .userId(userId)
                .displayName(user.getDisplayName())
                .joinedAt(Instant.now())
                .build());

        Squad saved = squadRepository.save(squad);
        log.info("Squad created: '{}' by user {}", name, userId);
        return saved;
    }

    public Squad joinSquad(String userId, String squadId) {
        Squad squad = getSquadOrThrow(squadId);

        if (squad.getMembers().size() >= squad.getMaxMembers()) {
            throw new BadRequestException("Squad đã đầy (" + squad.getMaxMembers() + " thành viên)");
        }

        boolean alreadyMember = squad.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(userId));
        if (alreadyMember) {
            throw new BadRequestException("Bạn đã là thành viên của squad này");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        squad.getMembers().add(Squad.SquadMember.builder()
                .userId(userId)
                .displayName(user.getDisplayName())
                .joinedAt(Instant.now())
                .build());

        Squad saved = squadRepository.save(squad);
        log.info("User {} joined squad '{}'", userId, squad.getName());
        return saved;
    }

    public Squad leaveSquad(String userId, String squadId) {
        Squad squad = getSquadOrThrow(squadId);

        if (squad.getOwnerId().equals(userId)) {
            throw new BadRequestException("Owner không thể rời squad. Hãy chuyển quyền hoặc xóa squad");
        }

        boolean removed = squad.getMembers().removeIf(m -> m.getUserId().equals(userId));
        if (!removed) {
            throw new BadRequestException("Bạn không phải thành viên của squad này");
        }

        Squad saved = squadRepository.save(squad);
        log.info("User {} left squad '{}'", userId, squad.getName());
        return saved;
    }

    public Squad kickMember(String ownerId, String squadId, String targetUserId) {
        Squad squad = getSquadOrThrow(squadId);

        if (!squad.getOwnerId().equals(ownerId)) {
            throw new BadRequestException("Chỉ owner mới có quyền kick thành viên");
        }

        if (ownerId.equals(targetUserId)) {
            throw new BadRequestException("Không thể tự kick chính mình");
        }

        boolean removed = squad.getMembers().removeIf(m -> m.getUserId().equals(targetUserId));
        if (!removed) {
            throw new BadRequestException("User không phải thành viên của squad");
        }

        Squad saved = squadRepository.save(squad);
        log.info("User {} kicked from squad '{}' by owner {}", targetUserId, squad.getName(), ownerId);
        return saved;
    }

    public Squad getSquadDetails(String squadId) {
        return getSquadOrThrow(squadId);
    }

    public List<Squad> getMySquads(String userId) {
        return squadRepository.findByMemberUserId(userId);
    }

    private Squad getSquadOrThrow(String squadId) {
        return squadRepository.findById(squadId)
                .orElseThrow(() -> new ResourceNotFoundException("Squad không tồn tại: " + squadId));
    }
}
