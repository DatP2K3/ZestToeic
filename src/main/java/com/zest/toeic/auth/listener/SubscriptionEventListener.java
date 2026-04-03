package com.zest.toeic.auth.listener;

import com.zest.toeic.auth.model.User;
import com.zest.toeic.auth.repository.UserRepository;
import com.zest.toeic.monetization.event.SubscriptionUpdatedEvent;
import com.zest.toeic.shared.model.enums.SubscriptionTier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SubscriptionEventListener {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionEventListener.class);
    private final UserRepository userRepository;

    public SubscriptionEventListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Async
    @EventListener
    public void handleSubscriptionUpdated(SubscriptionUpdatedEvent event) {
        log.info("Received Subscription Updated Event for user: {} with tier: {}", event.userId(), event.tier());
        
        Optional<User> optionalUser = userRepository.findById(event.userId());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setSubscriptionTier(SubscriptionTier.valueOf(event.tier()));
            userRepository.save(user);
            log.info("Successfully updated subscription tier for user: {}", event.userId());
        } else {
            log.warn("User {} not found when processing subscription event", event.userId());
        }
    }
}
