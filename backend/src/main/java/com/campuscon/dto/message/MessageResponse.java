package com.campuscon.dto.message;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class MessageResponse {
    private Long id;
    private String content;
    private UserDTO sender;
    private LocalDateTime sentAt;
    private boolean isRead;
    private boolean isPinned;
    private Long replyToId;
    private String replyToContent;
    private Set<String> reactions;
    private String imageUrl;
    private String url;
    private String urlMetadata;
    
    @Data
    public static class UserDTO {
        private Long id;
        private String username;
        private String profilePictureUrl;
    }
}
