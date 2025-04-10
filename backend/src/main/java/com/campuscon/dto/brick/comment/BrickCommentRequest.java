package com.campuscon.dto.brick.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for brick comment requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrickCommentRequest {
    private String content;
    private Long parentCommentId;
}
