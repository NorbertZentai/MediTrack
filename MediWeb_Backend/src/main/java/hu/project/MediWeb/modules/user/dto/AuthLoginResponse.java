package hu.project.MediWeb.modules.user.dto;

public record AuthLoginResponse(
        UserPublicDTO user,
        String token,
        String type
) {}
