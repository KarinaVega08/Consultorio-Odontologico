# Design Document — C-01 Setup Base y Seguridad Inicial

## 1. Visión General de la Arquitectura

El sistema se estructura como un monolito modular en **Spring Boot 3** (Java 21) gestionado con **Gradle**. Sigue una arquitectura en capas limpias bajo el paquete raíz `com.consultorio`, desacoplando completamente el modelo de dominio de las interfaces externas (API REST y persistencia).

```
                      +-----------------------------------+
                      |   Frontend (HTML / Vanilla TS)    |
                      +-----------------+-----------------+
                                        | (HTTP / JSON)
                                        v
                      +-----------------------------------+
                      |             API Layer             |
                      |  (REST Controllers, DTOs, Advices)|
                      +-----------------+-----------------+
                                        |
                                        v
                      +-----------------------------------+
                      |         Application Layer         |
                      |   (Services, Use Cases, Mappers)  |
                      +-----------------+-----------------+
                                        |
                                        v
                      +-----------------------------------+
                      |           Domain Layer            |
                      |  (Entities, Enums, Repo Contracts)|
                      +-----------------+-----------------+
                                        |
                                        v
                      +-----------------------------------+
                      |       Infrastructure Layer        |
                      |  (Spring Security, JPA Repos, DB) |
                      +-----------------------------------+
```

## 2. Estructura de Directorios y Paquetes

```
Consultorio-Odontologico/
├── backend/
│   ├── build.gradle
│   ├── settings.gradle
│   └── src/
│       ├── main/
│       │   ├── java/com/consultorio/
│       │   │   ├── ConsultorioApplication.java
│       │   │   ├── api/
│       │   │   │   ├── common/
│       │   │   │   │   ├── ErrorResponseDTO.java
│       │   │   │   │   ├── FieldErrorDTO.java
│       │   │   │   │   └── GlobalExceptionHandler.java
│       │   │   │   └── health/
│       │   │   │       └── HealthController.java
│       │   │   ├── application/
│       │   │   │   └── common/
│       │   │   │       ├── BusinessException.java
│       │   │   │       └── ResourceNotFoundException.java
│       │   │   ├── domain/
│       │   │   │   └── common/
│       │   │   │       ├── BaseEntity.java
│       │   │   │       ├── RolUsuario.java
│       │   │   │       └── EstadoTurno.java
│       │   │   └── infrastructure/
│       │   │       ├── config/
│       │   │       │   ├── SecurityConfig.java
│       │   │       │   └── WebConfig.java
│       │   │       └── security/
│       │   │           └── PasswordEncoderConfig.java
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-dev.yml
│       │       ├── data.sql
│       │       └── static/
│       │           ├── index.html
│       │           ├── css/styles.css
│       │           └── js/app.js
│       └── test/
│           ├── java/com/consultorio/
│           │   ├── ConsultorioApplicationTests.java
│           │   ├── api/HealthControllerTest.java
│           │   └── infrastructure/SecurityConfigTest.java
│           └── resources/
│               └── application-test.yml
├── frontend/
│   ├── src/
│   │   └── app.ts
│   ├── package.json
│   ├── tsconfig.json
│   └── index.html
├── docs/
├── knowledge-base/
├── openspec/
├── gradlew
├── gradlew.bat
├── settings.gradle
└── README.md
```

## 3. Matriz de Dependencias (Gradle)

En `backend/build.gradle`:
- `id 'org.springframework.boot' version '3.3.5'` (o 3.4.x)
- `id 'io.spring.dependency-management' version '1.1.6'`
- `id 'java'`

Dependencias principales:
```groovy
dependencies {
    // Web & REST
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // Security & Auth
    implementation 'org.springframework.boot:spring-boot-starter-security'

    // Data & Persistence
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'org.postgresql:postgresql'

    // Utilities
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testRuntimeOnly 'com.h2database:h2'
}
```

## 4. Diseño de Seguridad y RBAC

### 4.1 Configuración de Seguridad (`SecurityConfig`)
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/health",
                    "/api/auth/**",
                    "/api/availability",
                    "/",
                    "/index.html",
                    "/css/**",
                    "/js/**",
                    "/favicon.ico"
                ).permitAll()
                .requestMatchers("/api/admin/**").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                .anyRequest().authenticated()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 4.2 Enums de Dominio Fundacionales
- `RolUsuario`: `PACIENTE`, `RECEPCIONISTA`, `ODONTOLOGO`, `ADMINISTRADOR`.
- `EstadoTurno`: `RESERVADO`, `CONFIRMADO`, `CANCELADO`, `COMPLETADO`.

## 5. Diseño del Manejo Global de Errores

`GlobalExceptionHandler` captura de manera unificada:
1. `MethodArgumentNotValidException`: mapea cada `FieldError` a un `FieldErrorDTO(field, message)`.
2. `BusinessException`: mapea a status 400 o 422 con mensaje legible para el cliente.
3. `ResourceNotFoundException`: mapea a status 404.
4. `AccessDeniedException`: mapea a status 403.
5. `Exception` general: loguea con SLF4J a nivel `ERROR` con stack trace interno, pero devuelve status 500 con mensaje genérico `"Ha ocurrido un error interno en el servidor"`.

## 6. Estrategia de Perfiles de Base de Datos y Reproducibilidad

- **Producción / Desarrollo Local (`application.yml` + `application-dev.yml`)**:
  - PostgreSQL con variables de entorno parametrizadas.
  - Driver `org.postgresql.Driver`.
- **Suite de Pruebas (`src/test/resources/application.yml`)**:
  - H2 Database en memoria (`MODE=PostgreSQL`).
  - `ddl-auto: create-drop`.
  - No depende de que PostgreSQL esté levantado ni requiere Docker instalado.
  - Tiempos de ejecución: unit tests < 50ms, slice tests < 100ms, suite completa < 5000ms.

## 7. Servido Estático del Frontend
Spring Boot por defecto sirve los contenidos de `src/main/resources/static/` en la raíz `/`.
Se genera un shell estático con:
- `index.html`: interfaz inicial responsiva y limpia.
- `styles.css`: estilos visuales consistentes con diseño médico/odontológico moderno.
- `app.js` (generado desde TypeScript vanilla): script cliente para consumir `/api/health` y validar conectividad.
