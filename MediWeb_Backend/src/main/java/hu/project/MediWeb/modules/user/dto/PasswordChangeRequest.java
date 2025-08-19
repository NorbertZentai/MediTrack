package hu.project.MediWeb.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeRequest {
    @NotBlank(message = "currentPassword required")
    private String currentPassword;
    @NotBlank(message = "newPassword required")
    @Size(min = 8, message = "newPassword min length 8")
    private String newPassword;
    @NotBlank(message = "reNewPassword required")
    private String reNewPassword;
}