package hu.project.MediWeb.modules.medication.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.project.MediWeb.modules.medication.entity.Medication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

public class MedicationDetailsMapper {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(MedicationDetailsMapper.class);

    public static Medication toEntity(Long id, MedicationDetailsResponse dto) {
        try {
            return Medication.builder()
                    .id(id)
                    .name(dto.getName())
                    .imageUrl(dto.getImageUrl())
                    .registrationNumber(dto.getRegistrationNumber())
                    .substance(dto.getSubstance())
                    .atcCode(dto.getAtcCode())
                    .company(dto.getCompany())
                    .legalBasis(dto.getLegalBasis())
                    .status(dto.getStatus())
                    .authorizationDate(dto.getAuthorizationDate())
                    .narcotic(dto.getNarcotic())
                    .patientInfoUrl(dto.getPatientInfoUrl())
                    .smpcUrl(dto.getSmpcUrl())
                    .labelUrl(dto.getLabelUrl())
                    .containsLactose(dto.isContainsLactose())
                    .containsGluten(dto.isContainsGluten())
                    .containsBenzoate(dto.isContainsBenzoate())
                    .packagesJson(mapper.writeValueAsString(dto.getPackages() != null ? dto.getPackages() : new ArrayList<>()))
                    .substitutesJson(mapper.writeValueAsString(dto.getSubstitutes() != null ? dto.getSubstitutes() : new ArrayList<>()))
                    .finalSamplesJson(mapper.writeValueAsString(dto.getFinalSamples() != null ? dto.getFinalSamples() : new ArrayList<>()))
                    .defectiveFormsJson(mapper.writeValueAsString(dto.getDefectiveForms() != null ? dto.getDefectiveForms() : new ArrayList<>()))
                    .hazipatikaJson(mapper.writeValueAsString(dto.getHazipatikaInfo()))
                    .build();
        } catch (JsonProcessingException e) {
            log.error("mapper.toEntity.error msg={}", e.getMessage(), e);
            throw new RuntimeException("Hiba DTO-ból Entity konvertálás közben", e);
        }
    }

    public static MedicationDetailsResponse toDto(Medication e) {
        try {
            return MedicationDetailsResponse.builder()
                    .name(e.getName())
                    .imageUrl(e.getImageUrl())
                    .registrationNumber(e.getRegistrationNumber())
                    .substance(e.getSubstance())
                    .atcCode(e.getAtcCode())
                    .company(e.getCompany())
                    .legalBasis(e.getLegalBasis())
                    .status(e.getStatus())
                    .authorizationDate(e.getAuthorizationDate())
                    .narcotic(e.getNarcotic())
                    .patientInfoUrl(e.getPatientInfoUrl())
                    .smpcUrl(e.getSmpcUrl())
                    .labelUrl(e.getLabelUrl())
                    .containsLactose(e.isContainsLactose())
                    .containsGluten(e.isContainsGluten())
                    .containsBenzoate(e.isContainsBenzoate())
                    .packages(mapper.readValue(e.getPackagesJson(), new TypeReference<>() {}))
                    .substitutes(mapper.readValue(e.getSubstitutesJson(), new TypeReference<>() {}))
                    .finalSamples(mapper.readValue(e.getFinalSamplesJson(), new TypeReference<>() {}))
                    .defectiveForms(mapper.readValue(e.getDefectiveFormsJson(), new TypeReference<>() {}))
                    .hazipatikaInfo(mapper.readValue(e.getHazipatikaJson(), new TypeReference<>() {}))
                    .build();
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Hiba Entity-ből DTO konvertálás közben", ex);
        }
    }
}