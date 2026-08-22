# Ensom

늦지 않게, 서두르지 않게. 다음 일정까지 언제부터 준비하면 되는지 알려주는 앱.

```
.
├── backend/    Spring Boot 4.1 · Java 21 · PostgreSQL (Flyway)
└── frontend/   Flutter · Riverpod · go_router
```

## 문서

화면 동작과 전이의 기준은 `Ensom_Screen_Flow_Spec`(화면 연결 명세서 v2.0)이고,
시각·카피 기준은 프로토타입 27개 HTML이다. 이 저장소에는 코드만 있으므로 두
문서는 별도로 참조해야 한다.

API 계약은 `backend/doc/API.md`에 있다. 손으로 쓰지 않고 컨트롤러·DTO 소스에서
생성한다.

```bash
cd backend
python3 tools/generate_api_spec.py            # 생성
python3 tools/generate_api_spec.py --check    # 소스와 어긋나면 실패
```

## backend

```bash
docker compose up -d          # PostgreSQL
./gradlew test                # 컨텍스트 로드 테스트가 DB에 붙으므로 위가 먼저다
./gradlew bootRun
```

Flyway가 `src/main/resources/db/migration`의 마이그레이션을 기동 시 적용한다.
JWT·암호화 시크릿은 로컬 기본값이 있어 그대로 뜨지만, 배포 전에 반드시
환경변수로 교체해야 한다. `.env.example` 참고.

`/v1` 접두사는 애플리케이션이 아니라 nginx가 붙인다(`proxy_pass http://api/`가
제거). `bootRun`에 직접 붙을 때는 `/v1` 없이 호출한다.

## frontend

```bash
cd frontend
flutter pub get
dart run build_runner build --delete-conflicting-outputs
flutter analyze --no-fatal-infos --no-fatal-warnings
flutter test
```

실행에 필요한 키는 `--dart-define`으로 주입한다. 이름은
`lib/core/app_config.dart`의 `String.fromEnvironment`와 정확히 같아야 한다.
자세한 사용법과 웹 릴리스 빌드 명령은 `frontend/README.md`에 있다.

## 로컬에서 앱 ↔ 서버 붙여 보기

```bash
# 1) 서버 — 로컬엔 SMTP가 없으므로 이메일 인증을 끈다
cd backend
./gradlew bootRun --args='--server.port=8090 --app.email-verification.enabled=false'

# 2) 앱 — /v1 없이 서버를 가리킨다
cd frontend
flutter build web --release --pwa-strategy=none --no-wasm-dry-run \
  --dart-define=API_BASE_URL=http://localhost:8090 \
  --dart-define=OAUTH_GOOGLE_CLIENT_ID=... \
  --dart-define=KAKAO_REST_API_KEY=... \
  --dart-define=KAKAO_JAVASCRIPT_APP_KEY=... \
  --dart-define=KAKAO_NATIVE_APP_KEY=...
python3 -m http.server 5500 --directory build/web
```

CORS는 `http://localhost:*`를 허용하므로 포트는 자유롭게 골라도 된다.

## 비밀값

`.env`는 양쪽 모두 `.gitignore` 대상이다. 커밋하지 않는다.

공개해도 되는 값과 아닌 값을 섞지 않는다. 앱에 `--dart-define`으로 넣는 값은
빌드 결과물에 그대로 박히므로, 클라이언트에 들어가도 되는 것만 넣는다
(Kakao 앱 키, Google client ID, API base URL). DB 비밀번호·JWT 시크릿·SMTP
비밀번호·OAuth client secret 같은 서버 비밀값은 서버 환경변수로만 주입한다.
