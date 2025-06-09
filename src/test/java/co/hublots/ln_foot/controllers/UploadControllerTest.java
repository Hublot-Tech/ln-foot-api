package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.DeleteImageDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlRequestDto;
import co.hublots.ln_foot.dto.ImagePresignedUrlResponseDto;
import co.hublots.ln_foot.services.UploadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@AutoConfigureMockMvc
class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UploadService uploadService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getImagePresignedUrl_isOk_withAdminRole() throws Exception {
        ImagePresignedUrlRequestDto requestDto = ImagePresignedUrlRequestDto.builder()
                .fileName("test.png").contentType("image/png").build();
        ImagePresignedUrlResponseDto responseDto = ImagePresignedUrlResponseDto.builder()
                .uploadUrl("http://mock.upload.url/test.png")
                .key("test.png")
                .finalUrl("http://mock.final.url/test.png")
                .build();
        when(uploadService.getImagePresignedUrl(any(ImagePresignedUrlRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/upload/image-presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl", is(responseDto.getUploadUrl())))
                .andExpect(jsonPath("$.key", is(responseDto.getKey())));
    }

    @Test
    void getImagePresignedUrl_isUnauthorized_withoutAuth() throws Exception {
        ImagePresignedUrlRequestDto requestDto = ImagePresignedUrlRequestDto.builder().build();
        mockMvc.perform(post("/api/v1/upload/image-presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getImagePresignedUrl_isForbidden_withUserRole() throws Exception {
        ImagePresignedUrlRequestDto requestDto = ImagePresignedUrlRequestDto.builder().build();
        mockMvc.perform(post("/api/v1/upload/image-presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteImage_isNoContent_withAdminRole() throws Exception {
        DeleteImageDto deleteDto = DeleteImageDto.builder().key("some/key.png").build();
        doNothing().when(uploadService).deleteImage(any(DeleteImageDto.class));

        mockMvc.perform(delete("/api/v1/upload/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isNoContent());
        verify(uploadService, times(1)).deleteImage(any(DeleteImageDto.class));
    }

    @Test
    void deleteImage_isUnauthorized_withoutAuth() throws Exception {
        DeleteImageDto deleteDto = DeleteImageDto.builder().build();
        mockMvc.perform(delete("/api/v1/upload/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteImage_isForbidden_withUserRole() throws Exception {
        DeleteImageDto deleteDto = DeleteImageDto.builder().build();
        mockMvc.perform(delete("/api/v1/upload/image")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDto)))
                .andExpect(status().isForbidden());
    }
}
