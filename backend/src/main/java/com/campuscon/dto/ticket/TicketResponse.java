package com.campuscon.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private Long id;
    private Long userId;
    private Long deedId;
    private String ticketCode;
    private String qrCodePath;
    private LocalDateTime createdAt;
    private String deedTitle;
    private String userName;
}
