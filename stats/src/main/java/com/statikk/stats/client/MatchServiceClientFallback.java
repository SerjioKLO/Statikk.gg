package com.statikk.stats.client;

import com.statikk.stats.dto.MatchDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class MatchServiceClientFallback implements MatchServiceClient {

    @Override
    public List<MatchDto> getMatchesByUserAndGame(Long userId, Long gameId) {
        log.warn("Fallback triggered: Unable to fetch matches from ms-matches for user {} and game {}", userId, gameId);
        return Collections.emptyList();
    }

    @Override
    public List<MatchDto> getMatchesByUserId(Long userId) {
        log.warn("Fallback triggered: Unable to fetch matches from ms-matches for user {}", userId);
        return Collections.emptyList();
    }
}
