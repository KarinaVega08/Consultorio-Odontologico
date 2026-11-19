# Skill Registry

**Delegator use only.** Any agent that launches sub-agents reads this registry to resolve compact rules, then injects them directly into sub-agent prompts. Sub-agents do NOT read this registry or individual SKILL.md files.

See `_shared/skill-resolver.md` for the full resolution protocol.

## User Skills

| Trigger | Skill | Path |
|---------|-------|------|
| spring boot best practices | java-springboot | C:\Users\thedi\.agents\skills\java-springboot\SKILL.md |
| spring boot testing patterns | spring-boot-test-patterns | C:\Users\thedi\.agents\skills\spring-boot-test-patterns\SKILL.md |

## Compact Rules

Pre-digested rules per skill. Delegators copy matching blocks into sub-agent prompts as `## Project Standards (auto-resolved)`.

### java-springboot
- Use constructor injection for required dependencies; declare dependency fields `private final`.
- Organize code by feature/domain package, not by layer; use Spring Boot starters (web, data-jpa).
- Use DTOs for API request/response; never expose JPA entities directly to clients.
- Validate request payloads with Bean Validation (`@Valid`, `@NotNull`, `@Size`) on DTOs.
- Handle errors globally with `@ControllerAdvice` + `@ExceptionHandler` for consistent error responses.
- Encapsulate business logic in `@Service` classes; keep services stateless.
- Use `@Transactional` on service methods at the most granular level necessary.
- Extend `JpaRepository`/`CrudRepository`; use `@Query`/Criteria API for complex queries; use DTO projections.
- Log with SLF4J (`LoggerFactory.getLogger`) and parameterized messages, not string concatenation.
- Use Spring Security with BCrypt for password encoding; prevent SQL injection via JPA/parameterized queries.
- Externalize config in `application.yml`; bind type-safe props with `@ConfigurationProperties`; no hardcoded secrets.

### spring-boot-test-patterns
- Use unit tests with `@ExtendWith(MockitoExtension.class)` for business logic without Spring context (< 50ms).
- Use `@DataJpaTest` for repository tests with minimal context (< 100ms).
- Use `@WebMvcTest`/`@WebFluxTest` for controller/REST layer tests with MockMvc (< 100ms).
- Reserve `@SpringBootTest` only for full integration tests (< 500ms); never for unit tests.
- Prefer `@ServiceConnection` for Testcontainers on Spring Boot 3.5+ over `@DynamicPropertySource`.
- Keep tests deterministic: initialize test data explicitly in `@BeforeEach`; each test independent.
- Never use `@DirtiesContext` (forces context rebuild, hurts performance).
- Reuse Testcontainers at JVM level (`withReuse(true)` + `TESTCONTAINERS_REUSE_ENABLE=true`).
- Organize tests by layer to maximize context caching; avoid mixing `@MockBean` across configurations.

## Project Conventions

| File | Path | Notes |
|------|------|-------|
| none yet | - | CLAUDE.md/AGENTS.md not created yet (agent-instruction phase next) |
