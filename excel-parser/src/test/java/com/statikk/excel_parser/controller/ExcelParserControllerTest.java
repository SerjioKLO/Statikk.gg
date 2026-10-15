package com.statikk.excel_parser.controller;

import com.statikk.excel_parser.dto.ExcelUploadResponse;
import com.statikk.excel_parser.service.ExcelParserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ExcelParserController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExcelParserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExcelParserService parserService;

    @Test
    @DisplayName("POST /api/excel/upload - RF-04: Should accept multipart upload and return 200 OK")
    void shouldUploadExcel() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "stats.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "dummy".getBytes()
        );

        ExcelUploadResponse response = ExcelUploadResponse.builder()
                .filename("stats.xlsx")
                .userId(1L)
                .gameId(2L)
                .totalRowsParsed(5)
                .status("SUCCESS")
                .message("Archivo procesado exitosamente")
                .build();

        when(parserService.processFile(any(), eq(1L), eq(2L))).thenReturn(response);

        mockMvc.perform(multipart("/api/excel/upload")
                        .file(file)
                        .param("userId", "1")
                        .param("gameId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.totalRowsParsed").value(5));
    }

    @Test
    @DisplayName("GET /api/excel/template - Should download Excel template")
    void shouldDownloadTemplate() throws Exception {
        when(parserService.generateTemplate()).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/excel/template"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=statikk_template.xlsx"))
                .andExpect(content().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")));
    }
}
