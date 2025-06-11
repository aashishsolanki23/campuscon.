package com.campuscon.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIDescriptionResponse {
    private String generatedDescription;
    private boolean success;
    private String message;
}
