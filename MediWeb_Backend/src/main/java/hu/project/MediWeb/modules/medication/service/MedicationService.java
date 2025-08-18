package hu.project.MediWeb.modules.medication.service;

import hu.project.MediWeb.modules.GoogleImage.service.GoogleImageService;
import hu.project.MediWeb.modules.medication.dto.*;
import hu.project.MediWeb.modules.medication.entity.Medication;
import hu.project.MediWeb.modules.medication.repository.MedicationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final GoogleImageService googleImageService;
    private final MedicationRepository medicationRepository;
    private final HazipatikaSearchService hazipatikaSearchService;
    private static final Logger log = LoggerFactory.getLogger(MedicationService.class);

    public MedicationDetailsResponse getMedicationDetails(Long itemId) throws Exception {
        log.debug("medication.details.start id={}", itemId);
        try {
            log.debug("medication.details.check-cache id={}", itemId);
            Optional<Medication> optional = medicationRepository.findById(itemId);
            
            if (optional.isPresent()) {
                log.debug("medication.details.cache-hit id={}", itemId);
                Medication medication = optional.get();

                if (medication.getLastUpdated() != null && medication.getLastUpdated().isAfter(LocalDate.now().minusDays(7))) {
                    log.info("medication.details.return-cached id={} ageDays<7", itemId);
                    return MedicationDetailsMapper.toDto(medication);
                }

                log.info("medication.details.stale-refresh id={}", itemId);
                medicationRepository.deleteById(itemId);
            } else {
                log.debug("medication.details.cache-miss id={}", itemId);
            }

            log.info("medication.details.fetch.remote id={}", itemId);
            String url = "https://ogyei.gov.hu/gyogyszeradatbazis&action=show_details&item=" + itemId;
            log.debug("medication.details.fetch.url id={} url={}", itemId, url);
            
            Document doc = Jsoup.connect(url).get();
            log.debug("medication.details.fetch.success id={}", itemId);

            String name = doc.selectFirst("h3.gy-content__title").text();
            log.debug("medication.details.name id={} name={}", itemId, name);

            Element topTable = doc.selectFirst(".gy-content__top-table");
            System.out.println("📋 [MED-SERVICE] Processing medication details table for ID: " + itemId);

        String regNum = textFromTitle(topTable, "Nyilvántartási szám");
        String substance = textFromTitle(topTable, "Hatóanyag");
        String atc = textFromTitle(topTable, "ATC kód 1/ATC kód 2");
        String company = textFromTitle(topTable, "Forgalomba hozatali engedély jogosultja");
        String basis = textFromTitle(topTable, "Jogalap");
        String status = textFromTitle(topTable, "Státusz");
        String date = textFromTitle(topTable, "Készítmény engedélyezésének dátuma");
        LocalDate authorizationDate = null;
        if (date != null && !date.isBlank()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            authorizationDate = LocalDate.parse(date, formatter);
        }

        String narcotic = textFromTitle(topTable, "Kábítószer / pszichotróp anyagokat tartalmaz");
        String patientInfoUrl = getDocUrl(topTable, "betegtájékoztató");
        String smpcUrl = getDocUrl(topTable, "alkalmazási előírás");
        String labelUrl = getDocUrl(topTable, "cimkeszöveg");

    log.trace("medication.details.substitutes.start id={}", itemId);
        List<SubstituteMedication> substitutes = doc.select("#substitution .table__line.line").stream()
                .map(line -> {
                    try {
                        var cells = line.select("div.cell");
                        if (cells.size() < 2) {
                            log.warn("medication.details.substitute.skip id={} reason=insufficient-cells", itemId);
                            return null;
                        }
                        
                        String substituteMedicationName = cells.get(0).text();
                        String substituteMedicationRegNum = cells.get(1).ownText();
                        Element link = line.selectFirst("a[href*=item=]");
                        int id = 0;
                        if (link != null) {
                            String href = link.attr("href");
                            String substituteMedicationItemId = href.replaceAll(".*item=(\\d+).*", "$1");
                            id = Integer.parseInt(substituteMedicationItemId);
                        }
                        return new SubstituteMedication(substituteMedicationName, substituteMedicationRegNum, id);
                    } catch (Exception e) {
                        log.error("medication.details.substitute.error id={} msg={}", itemId, e.getMessage());
                        return null;
                    }
                })
                .filter(substitute -> substitute != null)
                .toList();

    log.trace("medication.details.packages.start id={}", itemId);
        List<PackageInfo> packages = doc.select("#packsizes .table__line.line").stream()
                .map(line -> {
                    try {
                        List<Element> cells = line.select(".cell");
                        if (cells.size() < 5) {
                            log.warn("medication.details.package.skip id={} reason=insufficient-cells", itemId);
                            return null;
                        }
                        return new PackageInfo(
                                cells.get(0).text(),
                                cells.get(1).text(),
                                cells.get(2).text(),
                                cells.get(3).text(),
                                cells.get(4).text()
                        );
                    } catch (Exception e) {
                        log.error("medication.details.package.error id={} msg={}", itemId, e.getMessage());
                        return null;
                    }
                })
                .filter(pkg -> pkg != null)
                .toList();

        Element datasheetTable = doc.select(".gy-content__datasheet").first();

        Boolean containsLactose = parseBooleanFromLine(datasheetTable, "Laktóz");
        Boolean containsStarch = parseBooleanFromLine(datasheetTable, "Búzakeményítő");
        Boolean containsBenzoate = parseBooleanFromLine(datasheetTable, "Benzoát");

        List<FinalSampleApproval> finalSamples = extractFinalSampleApprovals(doc);
        List<DefectiveFormApproval> defectiveForms = extractDefectiveForms(doc);

        String imageUrl = null;
        try {
            var img = googleImageService.searchImages(name);
            imageUrl = img != null ? img.link() : null;
        } catch (Exception e) {
            log.warn("medication.details.image.error id={} msg={}", itemId, e.getMessage());
        }

        HazipatikaResponse hazipatikaInfo = hazipatikaSearchService.searchMedication(name);

        MedicationDetailsResponse response = MedicationDetailsResponse.builder()
                .name(name)
                .imageUrl(imageUrl)
                .registrationNumber(regNum)
                .substance(substance)
                .atcCode(atc)
                .company(company)
                .legalBasis(basis)
                .status(status)
                .authorizationDate(authorizationDate)
                .narcotic(narcotic)
                .patientInfoUrl(patientInfoUrl)
                .smpcUrl(smpcUrl)
                .labelUrl(labelUrl)
                .substitutes(substitutes)
                .packages(packages)
                .containsLactose(containsLactose)
                .containsGluten(containsStarch)
                .containsBenzoate(containsBenzoate)
                .finalSamples(finalSamples)
                .defectiveForms(defectiveForms)
                .hazipatikaInfo(hazipatikaInfo)
                .build();

        Medication entity = MedicationDetailsMapper.toEntity(itemId, response);
    log.debug("medication.details.persist.start id={}", itemId);
        saveIfNotExists(entity);
    log.info("medication.details.success id={} name={} imagePresent={}", itemId, name, imageUrl != null);

        return response;
    } catch (Exception e) {
        log.error("medication.details.error id={} type={} msg={}", itemId, e.getClass().getSimpleName(), e.getMessage(), e);
        throw e;
    }
    }

    @Transactional
    public void saveIfNotExists(Medication medication) {
    log.trace("medication.persist.check id={}", medication.getId());
        try {
            if (!medicationRepository.existsById(medication.getId())) {
        log.debug("medication.persist.insert id={}", medication.getId());
                if (medication.getLastUpdated() == null) {
                    medication.setLastUpdated(LocalDate.now());
                }
                medicationRepository.save(medication);
        log.info("medication.persist.success id={}", medication.getId());
            } else {
        log.debug("medication.persist.skip.exists id={}", medication.getId());
            }
        } catch (Exception e) {
        log.error("medication.persist.error id={} type={} msg={}", medication.getId(), e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    private String textFromTitle(Element table, String title) {
        Element row = table.selectFirst(".line:has(.line__title:contains(" + title + "))");
        return row != null ? row.selectFirst(".line__desc").text() : "";
    }

    private String getDocUrl(Element table, String keyword) {
        Element link = table.select("a").stream()
                .filter(el -> el.text().toLowerCase().contains(keyword.toLowerCase()))
                .findFirst().orElse(null);
        return link != null ? link.absUrl("href") : "";
    }

    private Boolean parseBooleanFromLine(Element table, String label) {
        Element line = table.select(".line:has(.cell:containsOwn(" + label + "))").first();
        if (line != null) {
            String value = line.select(".cell").get(1).text().toLowerCase();
            return value.contains("van");
        }
        return null;
    }

    private List<FinalSampleApproval> extractFinalSampleApprovals(Document doc) {
        List<FinalSampleApproval> list = new ArrayList<>();
        for (Element section : doc.select(".gy-content__datasheet")) {
            Element title = section.selectFirst(".datasheet__title");
            if (title != null && title.text().toLowerCase().contains("véglegminta engedély")) {
                Element table = section.selectFirst(".table");
                if (table != null) {
                    for (Element row : table.select(".table__line.line")) {
                        List<Element> cells = row.select(".cell");
                        if (cells.size() >= 4) {
                            list.add(new FinalSampleApproval(
                                    cells.get(0).text(),
                                    cells.get(1).text(),
                                    cells.get(2).text(),
                                    cells.get(3).text()
                            ));
                        }
                    }
                }
            }
        }
        return list;
    }

    private List<DefectiveFormApproval> extractDefectiveForms(Document doc) {
        List<DefectiveFormApproval> list = new ArrayList<>();
        for (Element section : doc.select(".gy-content__datasheet")) {
            Element title = section.selectFirst(".datasheet__title");
            if (title != null && title.text().toLowerCase().contains("alaki hiba engedély")) {
                Element table = section.selectFirst(".table");
                if (table != null) {
                    for (Element row : table.select(".table__line.line")) {
                        List<Element> cells = row.select(".cell");
                        if (cells.size() >= 5) {
                            list.add(new DefectiveFormApproval(
                                    cells.get(0).text(),
                                    cells.get(1).text(),
                                    cells.get(2).text(),
                                    cells.get(3).text(),
                                    cells.get(4).text()
                            ));
                        }
                    }
                }
            }
        }
        return list;
    }
}