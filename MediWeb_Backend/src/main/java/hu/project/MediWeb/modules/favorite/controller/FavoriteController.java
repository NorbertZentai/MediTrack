package hu.project.MediWeb.modules.favorite.controller;

import hu.project.MediWeb.modules.favorite.dto.FavoriteDTO;
import hu.project.MediWeb.modules.favorite.entity.Favorite;
import hu.project.MediWeb.modules.favorite.service.FavoriteService;
import hu.project.MediWeb.modules.user.entity.User;
import hu.project.MediWeb.modules.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        String email = authentication.getName();
        Optional<User> userOptional = userService.findUserByEmail(email);
        return userOptional.orElse(null);
    }

    @GetMapping
    public ResponseEntity<List<FavoriteDTO>> getMyFavorites() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long currentUserId = currentUser.getId();
    List<FavoriteDTO> favorites = favoriteService.findByUserId(currentUserId).stream()
        .map(FavoriteDTO::from)
        .toList();
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/{medicationId}")
    public ResponseEntity<FavoriteDTO> addToFavorites(@PathVariable Long medicationId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Favorite favorite = favoriteService.addFavorite(currentUser, medicationId);
    FavoriteDTO dto = FavoriteDTO.from(favorite);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<Void> removeFromFavorites(@PathVariable Long favoriteId) {
        favoriteService.deleteById(favoriteId);
        return ResponseEntity.ok().build();
    }
}