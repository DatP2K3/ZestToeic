package com.zest.toeic.community.repository;

import com.zest.toeic.community.model.Friend;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends MongoRepository<Friend, String> {

    @Query("{ $or: [ { 'senderId': ?0, 'receiverId': ?1 }, { 'senderId': ?1, 'receiverId': ?0 } ] }")
    Optional<Friend> findBetweenUsers(String userId1, String userId2);

    @Query("{ $and: [ { $or: [ { 'senderId': ?0 }, { 'receiverId': ?0 } ] }, { 'status': 'ACCEPTED' } ] }")
    List<Friend> findAcceptedFriends(String userId);

    List<Friend> findByReceiverIdAndStatus(String receiverId, String status);

    long countByReceiverIdAndStatus(String receiverId, String status);
}
