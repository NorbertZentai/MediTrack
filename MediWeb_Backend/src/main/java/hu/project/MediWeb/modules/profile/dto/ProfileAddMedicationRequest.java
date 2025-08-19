package hu.project.MediWeb.modules.profile.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProfileAddMedicationRequest {
    @NotNull
    private Long itemId;
}
