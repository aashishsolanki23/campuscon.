package com.campuscon.dto.brick.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for brick comment responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrickCommentResponse {
    private Long id;
    private String content;
    private Long userId;
    private String username;
    private Long brickId;
    private Long parentCommentId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    private Integer likesCount;
    private Integer replyCount;
}
