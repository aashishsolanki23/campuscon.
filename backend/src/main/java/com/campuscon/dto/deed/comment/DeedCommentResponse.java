package com.campuscon.dto.deed.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for deed comment responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeedCommentResponse {
    private Long id;
    private String content;
    private Long userId;
    private String userName;
    private String userProfilePicture;
    private Long deedId;
    private Long parentCommentId;
    private int replyCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
