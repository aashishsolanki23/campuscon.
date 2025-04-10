package com.campuscon.dto.brick;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrickResponse {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private Long userId;
    private String username;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    private Integer likesCount;
    private Integer commentsCount;
    private Integer savesCount;
    private Integer sharesCount;
    private Boolean liked;
    private Boolean saved;
}
