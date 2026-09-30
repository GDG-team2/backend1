package com.walkmission.domain.user.entity;

/** 가입 시 동의받는 약관. 문서가 바뀌면 version을 올리고 다시 동의를 받는다. */
public enum TermsType {
    SERVICE("서비스 이용약관", true, "2026-09"),
    PRIVACY("개인정보 처리방침", true, "2026-09"),
    LOCATION("위치기반서비스 이용약관", true, "2026-09"),
    MARKETING("혜택·이벤트 알림 수신", false, "2026-09");

    private final String label;
    private final boolean required;
    private final String version;

    TermsType(String label, boolean required, String version) {
        this.label = label;
        this.required = required;
        this.version = version;
    }

    public String getLabel() { return label; }
    public boolean isRequired() { return required; }
    public String getVersion() { return version; }
}
