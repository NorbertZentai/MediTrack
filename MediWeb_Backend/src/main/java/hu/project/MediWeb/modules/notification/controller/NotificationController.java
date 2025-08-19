package hu.project.MediWeb.modules.notification.controller;

import hu.project.MediWeb.modules.notification.dto.IntakeSubmissionRequest;
import hu.project.MediWeb.modules.notification.dto.TodaysMedicationDTO;
import hu.project.MediWeb.modules.notification.service.MedicationIntakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/intake")
@RequiredArgsConstructor
public class NotificationController {

    private final MedicationIntakeService medicationIntakeService;

    @GetMapping("/today/{profileId}")
    public ResponseEntity<List<TodaysMedicationDTO>> getTodayMedications(@PathVariable Long profileId) {
        return ResponseEntity.ok(medicationIntakeService.getMedicationsForToday(profileId));
    }

    @PostMapping
    public ResponseEntity<Void> submitIntake(@Valid @RequestBody IntakeSubmissionRequest request) {
        medicationIntakeService.recordIntake(request);
        return ResponseEntity.ok().build();
    }
}