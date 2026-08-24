package com.hq.backend.provider;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 외부 API URL이 application.yaml에 실제로 어떤 값으로 들어있는지 검증한다.
 *
 * AirKoreaUvEnvironmentProviderTest는 provider에 URL을 직접 주입하므로, 운영 설정이
 * 폐기된 버전으로 되돌아가도 그 테스트는 그대로 통과한다. 실제 property를 읽는 이
 * 테스트가 있어야 같은 회귀(UV V4 폐기로 조회 전량 실패)를 다시 잡을 수 있다.
 */
@SpringBootTest
class EnvironmentProviderUrlConfigTest {

    @Value("${provider.kma.uv.index-url}")
    private String uvIndexUrl;

    @Test
    void 자외선지수는_폐기된_V4가_아니라_V5_엔드포인트를_쓴다() {
        assertThat(uvIndexUrl)
                .as("V4는 NO_OPENAPI_SERVICE_ERROR(코드 12)로 폐기됨")
                .doesNotContain("LivingWthrIdxServiceV4")
                .doesNotContain("getUVIdxV4");
        assertThat(uvIndexUrl).isEqualTo(
                "https://apis.data.go.kr/1360000/LivingWthrIdxServiceV5/getUVIdxV5");
    }
}
