package hu.project.MediWeb.modules.medication.controller;

import hu.project.MediWeb.modules.medication.dto.MedicationDetailsResponse;
import hu.project.MediWeb.modules.medication.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medication")
@RequiredArgsConstructor
public class MedicationController {

    @Autowired
    private MedicationService medicationService;
    private static final Logger log = LoggerFactory.getLogger(MedicationController.class);

    @GetMapping("/{itemId}")
    public ResponseEntity<MedicationDetailsResponse> getDetails(@PathVariable Long itemId) {
        try {
            log.debug("medication.controller.details.start id={}", itemId);
            MedicationDetailsResponse response = medicationService.getMedicationDetails(itemId);
            log.info("medication.controller.details.success id={}", itemId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("medication.controller.details.error id={} type={} msg={}", itemId, e.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Failed to get medication details for ID: " + itemId, e);
        }
    }
}