package hu.project.MediWeb.modules.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteDTO {
    private Long id;
    private Long userId;
    private Long medicationId;
    private String medicationName;

    public static FavoriteDTO from(hu.project.MediWeb.modules.favorite.entity.Favorite favorite) {
        if (favorite == null) return null;
        return FavoriteDTO.builder()
                .id(favorite.getId())
                .userId(favorite.getUser().getId())
                .medicationId(favorite.getMedication().getId())
                .medicationName(favorite.getMedication().getName())
                .build();
    }
}
