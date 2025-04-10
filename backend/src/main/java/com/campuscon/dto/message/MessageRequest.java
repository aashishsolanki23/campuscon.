package com.campuscon.dto.message;

import lombok.Data;

@Data
public class MessageRequest {
    private String content;
    private Long replyToId;
}
