package com.campuscon.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for ChatGroup information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatGroupDTO {
    private Long id;
    private String name;
    private String description;
    private String groupImageUrl;
    private String createdAt;
    private String groupType;
    private Long teamId;
    private String teamName;
    private boolean teamShortlisted;
    private int teamCurrentRound;
    private int memberCount;
    private int unreadCount;
}
