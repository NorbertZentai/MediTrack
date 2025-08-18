package hu.project.MediWeb.modules.GoogleImage.controller;

import hu.project.MediWeb.modules.GoogleImage.dto.GoogleImageResult;
import hu.project.MediWeb.modules.GoogleImage.service.GoogleImageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/images")
public class GoogleImageSearchController {

    private final GoogleImageService svc;

    public GoogleImageSearchController(GoogleImageService svc) {
        this.svc = svc;
    }

    @GetMapping
    public GoogleImageResult search(@RequestParam String q) {
        return svc.searchImages(q);
    }

}
