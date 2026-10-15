package com.statikk.matches.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.statikk.matches.dto.CreateMatchRequest;
import com.statikk.matches.dto.MatchDto;
import com.statikk.matches.dto.MessageResponse;
import com.statikk.matches.service.MatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MatchController.class)
@AutoConfigureMockMvc(addFilters = false)
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MatchService matchService;

    @Test
    @DisplayName("POST /api/matches - Should create manual match and return 201 CREATED")
    void shouldCreateMatch() throws Exception {
        CreateMatchRequest request = CreateMatchRequest.builder()
                .userId(1L)
                .gameId(2L)
                .result("WIN")
                .hoursPlayed(1.0)
                .kills(10)
                .deaths(2)
                .assists(5)
                .build();

        MatchDto dto = MatchDto.builder()
                .id(100L)
                .userId(1L)
                .gameId(2L)
                .result("WIN")
                .hoursPlayed(1.0)
                .build();

        when(matchService.createMatch(any(CreateMatchRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.result").value("WIN"));
    }

    @Test
    @DisplayName("GET /api/matches/user/{userId}/game/{gameId} - Should list matches for user and game")
    void shouldListMatchesForUserAndGame() throws Exception {
        MatchDto dto = MatchDto.builder()
                .id(100L)
                .userId(1L)
                .gameId(2L)
                .result("WIN")
                .build();

        when(matchService.getMatchesByUserAndGame(1L, 2L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/matches/user/1/game/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].result").value("WIN"));
    }

    @Test
    @DisplayName("DELETE /api/matches/{id} - Should delete match and return 200 OK")
    void shouldDeleteMatch() throws Exception {
        MessageResponse response = MessageResponse.builder()
                .id(100L)
                .message("Match deleted successfully")
                .build();

        when(matchService.deleteMatch(100L)).thenReturn(response);

        mockMvc.perform(delete("/api/matches/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Match deleted successfully"));
    }
}
