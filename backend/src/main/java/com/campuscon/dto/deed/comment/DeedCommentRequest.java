package com.campuscon.dto.deed.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for deed comment requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeedCommentRequest {
    private String content;
    private Long parentCommentId;
}
