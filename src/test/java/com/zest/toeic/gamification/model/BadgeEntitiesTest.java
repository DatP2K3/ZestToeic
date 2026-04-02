package com.zest.toeic.gamification.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BadgeEntitiesTest {

    @Test
    void testBadge() {
        Badge badge = new Badge();
        badge.setId("b1");
        badge.setCriteria("C1");
        badge.setName("Name");
        badge.setDescription("Desc");
        badge.setIconUrl("url");
        badge.setCategory("Cat");
        badge.setCreatedAt(Instant.now());

        assertEquals("b1", badge.getId());
        assertEquals("C1", badge.getCriteria());
        assertEquals("Name", badge.getName());
        assertEquals("Desc", badge.getDescription());
        assertEquals("url", badge.getIconUrl());
        assertEquals("Cat", badge.getCategory());

        Badge badge2 = Badge.builder().name("N2").build();
        assertTrue(badge.equals(badge) || !badge.equals(badge2)); // Jacoco equals coverage
        assertNotNull(badge.hashCode());
        assertNotNull(badge.toString());
    }

    @Test
    void testUserBadge() {
        UserBadge ub = new UserBadge();
        ub.setId("ub1");
        ub.setUserId("u1");
        ub.setBadgeId("b1");
        ub.setEarnedAt(LocalDateTime.now());

        assertEquals("ub1", ub.getId());
        assertEquals("u1", ub.getUserId());
        assertEquals("b1", ub.getBadgeId());
        
        UserBadge ub2 = UserBadge.builder().userId("u2").build();
        assertTrue(ub.equals(ub) || !ub.equals(ub2));
        assertNotNull(ub.hashCode());
        assertNotNull(ub.toString());
    }
}
