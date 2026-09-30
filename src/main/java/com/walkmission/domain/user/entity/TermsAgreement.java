package com.walkmission.domain.user.entity;

import com.walkmission.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 약관 동의 기록. 위치기반서비스 약관은 동의 사실을 보관해야 하므로 항목·버전·시각을 남긴다. */
@Entity
public class TermsAgreement extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TermsType termsType;

    private String version;
    private Boolean agreed;
    private LocalDateTime agreedAt;

    protected TermsAgreement() {}

    public TermsAgreement(User user, TermsType termsType, boolean agreed, LocalDateTime now) {
        this.user = user;
        this.termsType = termsType;
        this.version = termsType.getVersion();
        this.agreed = agreed;
        this.agreedAt = agreed ? now : null;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public TermsType getTermsType() { return termsType; }
    public String getVersion() { return version; }
    public Boolean getAgreed() { return agreed; }
    public LocalDateTime getAgreedAt() { return agreedAt; }
}
