package com.zest.toeic.gamification.service;

import com.zest.toeic.auth.model.User;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.community.model.Friend;
import com.zest.toeic.community.repository.FriendRepository;
import com.zest.toeic.gamification.dto.LeaderboardEntry;
import com.zest.toeic.shared.model.enums.FriendStatus;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendRepository friendRepository;

    @InjectMocks
    private LeaderboardService leaderboardService;

    private User mockUser1;
    private User mockUser2;

    @BeforeEach
    void setUp() {
        mockUser1 = User.builder().displayName("Alice").level(5).build();
        mockUser1.setId("u1");
        mockUser2 = User.builder().displayName("Bob").level(3).build();
        mockUser2.setId("u2");
    }

    @Test
    void getLeaderboard_shouldReturnRankedUsers() {
        Document doc1 = new Document("_id", "u1").append("totalXp", 1500L);
        Document doc2 = new Document("_id", "u2").append("totalXp", 1000L);
        List<Document> mappedResults = List.of(doc1, doc2);

        @SuppressWarnings("unchecked")
        AggregationResults<Document> aggResults = mock(AggregationResults.class);
        when(aggResults.getMappedResults()).thenReturn(mappedResults);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("xp_transactions"), eq(Document.class)))
                .thenReturn(aggResults);

        when(userRepository.findById("u1")).thenReturn(Optional.of(mockUser1));
        when(userRepository.findById("u2")).thenReturn(Optional.of(mockUser2));

        List<LeaderboardEntry> result = leaderboardService.getLeaderboard("WEEKLY", 0, 10);

        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getDisplayName());
        assertEquals(1, result.get(0).getRank());
        assertEquals(1500L, result.get(0).getTotalXp());

        assertEquals("Bob", result.get(1).getDisplayName());
        assertEquals(2, result.get(1).getRank());
        assertEquals(1000L, result.get(1).getTotalXp());
    }

    @Test
    void getUserRank_shouldReturnCorrectRank() {
        Document doc1 = new Document("_id", "u1").append("totalXp", 1500L);
        Document doc2 = new Document("_id", "u2").append("totalXp", 1000L);
        List<Document> mappedResults = List.of(doc1, doc2);

        @SuppressWarnings("unchecked")
        AggregationResults<Document> aggResults = mock(AggregationResults.class);
        when(aggResults.getMappedResults()).thenReturn(mappedResults);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("xp_transactions"), eq(Document.class)))
                .thenReturn(aggResults);

        when(userRepository.findById("u2")).thenReturn(Optional.of(mockUser2));

        LeaderboardEntry result = leaderboardService.getUserRank("u2", "DAILY");

        assertEquals(2, result.getRank());
        assertEquals("Bob", result.getDisplayName());
    }

    @Test
    void getUserRank_shouldReturnRankZeroIfNotFound() {
        List<Document> mappedResults = List.of(new Document("_id", "u1").append("totalXp", 1500L));

        @SuppressWarnings("unchecked")
        AggregationResults<Document> aggResults = mock(AggregationResults.class);
        when(aggResults.getMappedResults()).thenReturn(mappedResults);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("xp_transactions"), eq(Document.class)))
                .thenReturn(aggResults);

        LeaderboardEntry result = leaderboardService.getUserRank("u2", "MONTHLY");

        assertEquals(0, result.getRank());
        assertEquals(0L, result.getTotalXp());
    }

    @Test
    void getFriendsLeaderboard_shouldReturnOnlyFriends() {
        Friend friend = Friend.builder().senderId("u1").receiverId("u2").status(FriendStatus.ACCEPTED).build();
        when(friendRepository.findAcceptedFriends("u1")).thenReturn(List.of(friend));

        Document doc1 = new Document("_id", "u1").append("totalXp", 1500L);
        Document doc2 = new Document("_id", "u2").append("totalXp", 1000L);
        List<Document> mappedResults = List.of(doc1, doc2);

        @SuppressWarnings("unchecked")
        AggregationResults<Document> aggResults = mock(AggregationResults.class);
        when(aggResults.getMappedResults()).thenReturn(mappedResults);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("xp_transactions"), eq(Document.class)))
                .thenReturn(aggResults);

        when(userRepository.findById("u1")).thenReturn(Optional.of(mockUser1));
        when(userRepository.findById("u2")).thenReturn(Optional.of(mockUser2));

        List<LeaderboardEntry> result = leaderboardService.getFriendsLeaderboard("u1", "ALL_TIME");

        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getDisplayName());
        assertEquals("Bob", result.get(1).getDisplayName());
    }
}
