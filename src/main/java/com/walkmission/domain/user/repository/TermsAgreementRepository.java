package com.walkmission.domain.user.repository;

import com.walkmission.domain.user.entity.TermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TermsAgreementRepository extends JpaRepository<TermsAgreement, Long> {
    List<TermsAgreement> findByUserId(Long userId);
}
