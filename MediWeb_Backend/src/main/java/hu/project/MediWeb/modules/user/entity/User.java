package hu.project.MediWeb.modules.user.entity;

import hu.project.MediWeb.modules.user.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=100, unique=true)
    private String name;

    @Column(nullable=false, length=100, unique=true)
    private String email;

    @Column(nullable=false, length=255)
    private String password;

    @Column(length=10)
    private String gender;

    private String date_of_birth;

    private String address;

    @Column(length=200)
    private String phone_number;

    private LocalDateTime registration_date;

    private LocalDateTime last_login;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore // Kerüljük el, hogy a LAZY LOB mező közvetlenül szerializálódjon (HttpMessageNotWritableException)
    private byte[] profile_picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private UserRole role = UserRole.USER;

    private Boolean is_active = true;

    @Column(length=10)
    private String language = "hu";

    private LocalDateTime deleted_at;
}
