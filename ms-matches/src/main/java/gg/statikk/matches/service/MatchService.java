package gg.statikk.matches.service;

import gg.statikk.matches.dto.CreateMatchRequest;
import gg.statikk.matches.dto.MatchDto;
import gg.statikk.matches.dto.MessageResponse;
import gg.statikk.matches.dto.UpdateMatchRequest;
import gg.statikk.matches.event.ExcelMatchesImportedEvent;

import java.util.List;

public interface MatchService {
    MatchDto createMatch(CreateMatchRequest request);
    MatchDto updateMatch(Long id, UpdateMatchRequest request);
    MatchDto getMatchById(Long id);
    List<MatchDto> getMatchesByUserId(Long userId);
    List<MatchDto> getMatchesByUserAndGame(Long userId, Long gameId);
    MessageResponse deleteMatch(Long id);
    MessageResponse deleteMatchesByUserAndGame(Long userId, Long gameId);
    int processImportedMatches(ExcelMatchesImportedEvent event);
}