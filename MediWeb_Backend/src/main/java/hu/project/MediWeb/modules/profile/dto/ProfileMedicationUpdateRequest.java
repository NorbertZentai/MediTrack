package hu.project.MediWeb.modules.profile.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileMedicationUpdateRequest {
    @Size(max = 1000)
    private String note;
    private Object reminders;
}
