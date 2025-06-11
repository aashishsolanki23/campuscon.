package com.campuscon.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for chat messages.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private Long groupId;
    private String content;
    private String imageUrl;
    private String url;
    private String urlMetadata;
    private Long senderId;
    private String senderName;
    private String senderProfileImage;
    private String sentAt;
    private boolean isDeleted;
    private boolean isPinned;
    private Long replyToId;
    private String replyToContent;
    private Long replyToSenderId;
    private String replyToSenderName;
}
