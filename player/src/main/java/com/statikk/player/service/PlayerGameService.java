package com.statikk.player.service;

import com.statikk.player.dto.MessageResponse;
import com.statikk.player.dto.PlayerGameDto;
import com.statikk.player.dto.RegisterGameRequest;

import java.util.List;

public interface PlayerGameService {
    PlayerGameDto registerGame(Long userId, RegisterGameRequest request);
    List<PlayerGameDto> getGamesByUserId(Long userId);
    PlayerGameDto getGameById(Long userId, Long gameId);
    MessageResponse deleteGame(Long userId, Long gameId);
}
