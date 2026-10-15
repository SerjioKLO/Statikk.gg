package com.statikk.notification.controller;

import com.statikk.notification.dto.NotificationDto;
import com.statikk.notification.dto.NotificationResponse;
import com.statikk.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("GET /api/notifications/user/{userId} - Should return notification list")
    void shouldGetNotifications() throws Exception {
        NotificationDto dto = NotificationDto.builder()
                .id(1L)
                .userId(1L)
                .title("Planilla procesada")
                .message("Se han guardado 5 partidas")
                .type("FILE_PROCESSED")
                .readStatus(false)
                .build();

        when(notificationService.getNotificationsByUserId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/notifications/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Planilla procesada"));
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/read - Should mark notification as read")
    void shouldMarkAsRead() throws Exception {
        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .message("Notification marked as read")
                .unreadCount(2L)
                .build();

        when(notificationService.markAsRead(1L)).thenReturn(response);

        mockMvc.perform(put("/api/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.unreadCount").value(2L));
    }

    @Test
    @DisplayName("GET /api/notifications/user/{userId}/unread-count - Should return unread count")
    void shouldGetUnreadCount() throws Exception {
        when(notificationService.getUnreadCount(1L)).thenReturn(3L);

        mockMvc.perform(get("/api/notifications/user/1/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(3L));
    }
}
