package com.walkmission;

import com.walkmission.global.util.TimeUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class WalkMissionApplication {
    static {
        // 주간 리듬·주간 랭킹·일시 기록이 모두 한국 시간 기준이 되도록 서버 환경과 무관하게 고정한다.
        // main이 아닌 static 블록에 두어 테스트(스프링 테스트 컨텍스트)에서도 똑같이 적용된다.
        TimeZone.setDefault(TimeZone.getTimeZone(TimeUtils.KST));
    }

    public static void main(String[] args) {
        SpringApplication.run(WalkMissionApplication.class, args);
    }
}
