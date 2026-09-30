package com.walkmission.domain.user.entity;

import com.walkmission.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
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
    /** 랭킹에만 표시할 닉네임. null이면 nickname을 쓴다 */
    private String rankingNickname;
    private String profileImageUrl;
    /** 출생 연도 (선택 입력) */
    private Integer birthYear;
    private String regionCode;

    protected User() {}

    public User(String email, String password, String nickname, Integer birthYear, String regionCode) {
        this.userUuid = UUID.randomUUID();
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.birthYear = birthYear;
        this.regionCode = regionCode;
    }

    /** 보낸 값만 바꾼다. rankingNickname은 빈 문자열이면 설정을 지운다. */
    public void updateProfile(String nickname, String regionCode, Integer birthYear, String rankingNickname) {
        if (nickname != null) this.nickname = nickname;
        if (regionCode != null) this.regionCode = regionCode;
        if (birthYear != null) this.birthYear = birthYear;
        if (rankingNickname != null) this.rankingNickname = rankingNickname.isBlank() ? null : rankingNickname;
    }

    /** 랭킹에 보이는 이름 */
    public String getDisplayName() {
        return rankingNickname != null ? rankingNickname : nickname;
    }

    public Long getId() { return id; }
    public String getRankingNickname() { return rankingNickname; }
    public UUID getUserUuid() { return userUuid; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getNickname() { return nickname; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public Integer getBirthYear() { return birthYear; }
    public String getRegionCode() { return regionCode; }
}
