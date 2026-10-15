package com.statikk.player.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.statikk.player.dto.MessageResponse;
import com.statikk.player.dto.PlayerGameDto;
import com.statikk.player.dto.RegisterGameRequest;
import com.statikk.player.service.PlayerGameService;
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

@WebMvcTest(controllers = PlayerGameController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlayerGameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlayerGameService playerGameService;

    @Test
    @DisplayName("POST /api/players/{userId}/games - RF-03: Should register game and return 201 Created")
    void shouldRegisterGame() throws Exception {
        RegisterGameRequest request = RegisterGameRequest.builder()
                .gameName("Counter-Strike 2")
                .genre("FPS")
                .platform("PC")
                .build();

        PlayerGameDto dto = PlayerGameDto.builder()
                .id(1L)
                .userId(1L)
                .gameName("Counter-Strike 2")
                .genre("FPS")
                .platform("PC")
                .build();

        when(playerGameService.registerGame(eq(1L), any(RegisterGameRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/players/1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.gameName").value("Counter-Strike 2"));
    }

    @Test
    @DisplayName("GET /api/players/{userId}/games - Should list user games")
    void shouldListGames() throws Exception {
        PlayerGameDto dto = PlayerGameDto.builder()
                .id(1L)
                .userId(1L)
                .gameName("Counter-Strike 2")
                .build();

        when(playerGameService.getGamesByUserId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/players/1/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gameName").value("Counter-Strike 2"));
    }

    @Test
    @DisplayName("DELETE /api/players/{userId}/games/{gameId} - RF-07: Should delete game and return success message")
    void shouldDeleteGame() throws Exception {
        MessageResponse response = MessageResponse.builder()
                .id(1L)
                .message("Registro eliminado")
                .build();

        when(playerGameService.deleteGame(1L, 1L)).thenReturn(response);

        mockMvc.perform(delete("/api/players/1/games/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registro eliminado"));
    }
}
