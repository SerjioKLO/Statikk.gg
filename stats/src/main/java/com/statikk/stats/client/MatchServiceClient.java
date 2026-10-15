package com.statikk.stats.client;

import com.statikk.stats.dto.MatchDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ms-matches", fallback = MatchServiceClientFallback.class)
public interface MatchServiceClient {

    @GetMapping("/api/matches/user/{userId}/game/{gameId}")
    List<MatchDto> getMatchesByUserAndGame(
            @PathVariable("userId") Long userId,
            @PathVariable("gameId") Long gameId
    );

    @GetMapping("/api/matches/user/{userId}")
    List<MatchDto> getMatchesByUserId(
            @PathVariable("userId") Long userId
    );
}
