package com.campuscon.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for notification settings requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsRequest {
    private boolean pushNotificationsEnabled;
    private boolean bondRequestNotifications;
    private boolean messageNotifications;
    private boolean interactionNotifications;
    private boolean eventNotifications;
    private boolean emailNotificationsEnabled;
}
