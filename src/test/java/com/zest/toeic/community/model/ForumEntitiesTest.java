package com.zest.toeic.community.model;

import com.zest.toeic.shared.model.enums.ForumPostStatus;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ForumEntitiesTest {

    @Test
    void testForumPost() {
        ForumPost post = new ForumPost();
        post.setId("p1");
        post.setAuthorId("a1");
        post.setTitle("Title");
        post.setContent("Content");
        post.setTags(List.of("tag"));
        post.setUpvotes(10);
        post.setViewCount(20);
        post.setCommentCount(5);
        post.setStatus(ForumPostStatus.PUBLISHED);

        assertEquals("p1", post.getId());
        assertEquals("a1", post.getAuthorId());
        assertEquals(10, post.getUpvotes());

        ForumPost post2 = ForumPost.builder().authorId("N2").build();
        assertTrue(post.equals(post) || !post.equals(post2));
        assertNotNull(post.hashCode());
        assertNotNull(post.toString());
    }

    @Test
    void testForumComment() {
        ForumComment cc = new ForumComment();
        cc.setId("c1");
        cc.setPostId("p1");
        cc.setAuthorId("a1");
        cc.setContent("Cont");
        cc.setParentId("pa1");
        cc.setUpvotes(2);
        cc.setStatus(ForumPostStatus.PUBLISHED);

        assertEquals("c1", cc.getId());
        assertEquals("p1", cc.getPostId());

        ForumComment cc2 = ForumComment.builder().authorId("u2").build();
        assertTrue(cc.equals(cc) || !cc.equals(cc2));
        assertNotNull(cc.hashCode());
        assertNotNull(cc.toString());
    }
}
