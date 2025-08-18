package hu.project.MediWeb.modules.user.dto;

import hu.project.MediWeb.modules.user.entity.User;
import java.util.Base64;

public record UserPublicDTO(
        Long id,
        String name,
        String email,
        String phoneNumber,
        String imageUrl,
        String role,
        Boolean active,
        String language
) {
    public static UserPublicDTO from(User user) {
        if (user == null) return null;
        String img = null;
        if (user.getProfile_picture() != null && user.getProfile_picture().length > 0) {
            img = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(user.getProfile_picture());
        }
        return new UserPublicDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone_number(),
                img,
                user.getRole() != null ? user.getRole().name() : null,
                user.getIs_active(),
                user.getLanguage()
        );
    }
}
