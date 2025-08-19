package hu.project.MediWeb.modules.review.dto;

import hu.project.MediWeb.modules.user.entity.User;
import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {
    @NotNull
    private Long userId;

    @Min(1)
    @Max(5)
    private int rating;

    @Size(max = 2000)
    private String positive;

    @Size(max = 2000)
    private String negative;

    @Size(max = 150)
    private String author;
    private LocalDateTime createdAt;
}