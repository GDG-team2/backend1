package com.walkmission.domain.user.entity;

import com.walkmission.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID userUuid;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private String nickname;
    private String profileImageUrl;
    private LocalDate birth;
    private String regionCode;
    
    protected User() {}

    public User(String email, String password, String nickname) {
        this.userUuid = UUID.randomUUID();
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    public Long getId() { return id; }
    public UUID getUserUuid() { return userUuid; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getNickname() { return nickname; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public LocalDate getBirth() { return birth; }
    public String getRegionCode() { return regionCode; }
}
