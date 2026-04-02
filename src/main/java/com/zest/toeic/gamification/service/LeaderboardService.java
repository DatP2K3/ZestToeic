package com.zest.toeic.gamification.service;

import com.zest.toeic.gamification.dto.LeaderboardEntry;
import com.zest.toeic.auth.model.User;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.community.model.Friend;
import com.zest.toeic.community.repository.FriendRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class LeaderboardService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    public LeaderboardService(MongoTemplate mongoTemplate,
                              UserRepository userRepository,
                              FriendRepository friendRepository) {
        this.mongoTemplate = mongoTemplate;
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
    }

    public List<LeaderboardEntry> getLeaderboard(String period, int page, int size) {
        Instant startDate = getStartDate(period);

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt").gte(startDate)),
                Aggregation.group("userId").sum("xpAmount").as("totalXp"),
                Aggregation.sort(Sort.Direction.DESC, "totalXp"),
                Aggregation.skip((long) page * size),
                Aggregation.limit(size)
        );

        var results = mongoTemplate.aggregate(agg, "xp_transactions", org.bson.Document.class).getMappedResults();
        return buildEntries(results, page * size);
    }

    public LeaderboardEntry getUserRank(String userId, String period) {
        Instant startDate = getStartDate(period);

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt").gte(startDate)),
                Aggregation.group("userId").sum("xpAmount").as("totalXp"),
                Aggregation.sort(Sort.Direction.DESC, "totalXp")
        );

        var results = mongoTemplate.aggregate(agg, "xp_transactions", org.bson.Document.class).getMappedResults();

        for (int i = 0; i < results.size(); i++) {
            if (userId.equals(results.get(i).getString("_id"))) {
                return buildSingleEntry(results.get(i), i);
            }
        }

        return LeaderboardEntry.builder().userId(userId).rank(0).totalXp(0).build();
    }

    public List<LeaderboardEntry> getFriendsLeaderboard(String userId, String period) {
        List<Friend> friends = friendRepository.findAcceptedFriends(userId);
        Set<String> friendIds = new HashSet<>();
        friendIds.add(userId);
        for (Friend f : friends) {
            friendIds.add(f.getSenderId().equals(userId) ? f.getReceiverId() : f.getSenderId());
        }

        Instant startDate = getStartDate(period);

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt").gte(startDate).and("userId").in(friendIds)),
                Aggregation.group("userId").sum("xpAmount").as("totalXp"),
                Aggregation.sort(Sort.Direction.DESC, "totalXp")
        );

        var results = mongoTemplate.aggregate(agg, "xp_transactions", org.bson.Document.class).getMappedResults();
        return buildEntries(results, 0);
    }

    private Instant getStartDate(String period) {
        LocalDate now = LocalDate.now(ZONE);
        return switch (period != null ? period.toUpperCase() : "ALL_TIME") {
            case "DAILY" -> now.atStartOfDay(ZONE).toInstant();
            case "WEEKLY" -> now.with(DayOfWeek.MONDAY).atStartOfDay(ZONE).toInstant();
            case "MONTHLY" -> now.withDayOfMonth(1).atStartOfDay(ZONE).toInstant();
            default -> Instant.EPOCH;
        };
    }

    private List<LeaderboardEntry> buildEntries(List<org.bson.Document> results, int startRank) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            entries.add(buildSingleEntry(results.get(i), startRank + i));
        }
        return entries;
    }

    private LeaderboardEntry buildSingleEntry(org.bson.Document doc, int index) {
        String uid = doc.getString("_id");
        long xp = doc.get("totalXp") instanceof Number n ? n.longValue() : 0;

        User user = userRepository.findById(uid).orElse(null);
        String displayName = user != null ? user.getDisplayName() : "Unknown";
        int level = user != null ? user.getLevel() : 0;

        return LeaderboardEntry.builder()
                .rank(index + 1)
                .userId(uid)
                .displayName(displayName)
                .totalXp(xp)
                .level(level)
                .build();
    }
}
