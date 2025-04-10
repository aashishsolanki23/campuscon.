package com.campuscon.dto.deed;

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
public class DeedResponse {
    private Long id;
    private String title;
    private String description;
    private String bannerUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventDate;
    
    private String venue;
    private String category;
    private Long societyId;
    private String societyName;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    private Integer likesCount;
    private Integer commentsCount;
    private Integer savesCount;
    private Integer sharesCount;
    private Boolean liked;
    private Boolean saved;
}
