package com.zest.toeic.community.repository;

import com.zest.toeic.community.model.Squad;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface SquadRepository extends MongoRepository<Squad, String> {

    @Query("{ 'members.userId': ?0 }")
    List<Squad> findByMemberUserId(String userId);

    List<Squad> findByOwnerId(String ownerId);
}
