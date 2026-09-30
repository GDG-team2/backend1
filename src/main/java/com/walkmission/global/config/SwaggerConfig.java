package com.walkmission.global.config;

import com.walkmission.global.auth.LoginUser;
import com.walkmission.global.error.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.jackson.TypeNameResolver;
import org.springdoc.core.providers.ObjectMapperProvider;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;

@Configuration
public class SwaggerConfig {

    static {
        // @LoginUser 파라미터는 토큰에서 채워지므로 문서에서 숨긴다 (쿼리 파라미터로 오인되는 문제)
        SpringDocUtils.getConfig().addAnnotationsToIgnore(LoginUser.class);
        // LocalTime은 실제로 "HH:mm:ss" 문자열로 주고받는다 (요청은 "HH:mm"도 허용)
        SpringDocUtils.getConfig().replaceWithSchema(LocalTime.class,
                new StringSchema().format("time").example("22:00:00")
                        .description("시각 문자열. 응답은 \"HH:mm:ss\", 요청은 \"HH:mm\"과 \"HH:mm:ss\" 모두 가능 (시는 두 자리)"));
    }

    /**
     * 응답 안의 하위 record 이름이 겹치면(예: UserProfileResponse.RhythmInfo와 MissionCompleteResponse.RhythmInfo)
     * 명세에서 하나로 합쳐져 서로 덮어쓰므로, 스키마 이름 앞에 바깥 클래스 이름을 붙인다.
     */
    @Bean
    public ModelResolver nestedTypeNameModelResolver(ObjectMapperProvider objectMapperProvider) {
        return new ModelResolver(objectMapperProvider.jsonMapper(), new TypeNameResolver() {
            @Override
            protected String getNameOfClass(Class<?> cls) {
                Class<?> enclosing = cls.getEnclosingClass();
                return enclosing != null ? enclosing.getSimpleName() + cls.getSimpleName() : super.getNameOfClass(cls);
            }
        });
    }

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "JWT Auth";

        // 전역 보안 요구사항 (모든 API에 자물쇠 모양 표시)
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // 보안 스키마 설정 (Bearer 토큰)
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .info(new Info()
                        .title("WalkMission API")
                        .description("오하꼼(WalkMission) 백엔드 API 명세서")
                        .version("1.0.0"))
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    /** 모든 API에 공통 에러 응답(4XX/5XX, ErrorResponse 형식)을 문서화한다. */
    @Bean
    public OpenApiCustomizer errorResponseCustomizer() {
        return openApi -> {
            ModelConverters.getInstance().read(ErrorResponse.class).forEach(openApi.getComponents()::addSchemas);
            Content errorContent = new Content().addMediaType("application/json",
                    new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")));
            ApiResponse clientError = new ApiResponse()
                    .description("요청 오류 (code로 원인 구분: INVALID_INPUT, TOKEN_EXPIRED, MISSION_NOT_FOUND 등)")
                    .content(errorContent);
            ApiResponse serverError = new ApiResponse()
                    .description("서버 오류 (INTERNAL_ERROR, PLACE_SEARCH_FAILED)")
                    .content(errorContent);
            openApi.getPaths().values().forEach(path -> path.readOperations().forEach(operation -> operation.getResponses()
                    .addApiResponse("4XX", clientError)
                    .addApiResponse("5XX", serverError)));
        };
    }
}
