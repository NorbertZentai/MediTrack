package hu.project.MediWeb.modules.review.controller;

import hu.project.MediWeb.modules.review.dto.ReviewDTO;
import hu.project.MediWeb.modules.review.dto.ReviewListResponse;
import hu.project.MediWeb.modules.review.service.ReviewService;
import hu.project.MediWeb.modules.user.entity.User;
import hu.project.MediWeb.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    @GetMapping("/{itemId}")
    public ReviewListResponse getReviews(@PathVariable int itemId) {
        return reviewService.getReviewListForItem(itemId);
    }

    @PostMapping("/{itemId}")
    public ReviewDTO submitReview(@PathVariable int itemId, @Valid @RequestBody ReviewDTO dto) {
        User user = userService.findUserById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));

        dto.setUserId(user.getId());

        return reviewService.submitReview(itemId, dto, user);
    }

    @PutMapping("/{itemId}")
    public ReviewDTO updateReview(@PathVariable int itemId, @Valid @RequestBody ReviewDTO dto ) {
        User user = userService.findUserById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));

        return reviewService.updateReview(itemId, dto, user);
    }
}