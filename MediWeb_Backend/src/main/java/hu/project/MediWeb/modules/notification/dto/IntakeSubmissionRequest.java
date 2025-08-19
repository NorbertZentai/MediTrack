package hu.project.MediWeb.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntakeSubmissionRequest {
    @NotNull
    private Long profileMedicationId;

    // Expect HH:mm pattern
    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "Idő formátum HH:mm")
    private String time;

    private boolean taken;
}