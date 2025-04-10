package com.campuscon.dto.settings;

import com.campuscon.model.Settings.ProfileVisibility;
import com.campuscon.model.Settings.BondRequestPermission;
import com.campuscon.model.Settings.CommentPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for profile management settings requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSettingsRequest {
    private ProfileVisibility profileVisibility;
    private BondRequestPermission bondRequestPermission;
    private CommentPermission commentPermission;
}
