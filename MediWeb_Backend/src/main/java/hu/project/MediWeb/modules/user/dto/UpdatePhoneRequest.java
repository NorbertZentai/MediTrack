package hu.project.MediWeb.modules.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePhoneRequest {
    @Size(min = 5, max = 25)
    @Pattern(regexp = "^[+0-9 ()-]+$", message = "Csak szám, szóköz, +, -, zárójelek")
    private String phoneNumber;
}
