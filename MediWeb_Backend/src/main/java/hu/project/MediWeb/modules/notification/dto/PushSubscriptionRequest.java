package hu.project.MediWeb.modules.notification.dto;

public record PushSubscriptionRequest(
        String endpoint,
        String p256dh,
        String auth
) {}
