package com.campuscon.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MessageRequest {
    @NotBlank(message = "Message content cannot be empty")
    private String content;
    
    private Long replyToId;
    
    private MultipartFile image;
    
    private String url;
}
