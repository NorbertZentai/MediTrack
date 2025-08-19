package hu.project.MediWeb.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUsernameRequest {
    @NotBlank
    @Size(min = 3, max = 100)
    private String username;
}
