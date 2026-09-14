# schoolapp10-ssr

A simple server-side rendered (SSR) school management web application built with Spring Boot, Thymeleaf and MySQL. The project provides basic user authentication and CRUD operations for teachers, with database migrations managed by Flyway.

## Features
- Server-side UI using Thymeleaf templates
- Teacher management (list, insert, edit, soft-delete)
- User registration and authentication with Spring Security
- Role/capability model for authorization
- Database migrations with Flyway (resources/db/migration)
- Clean JPA model with validation and custom validators
- Logging configuration and profiles for dev/prod

## Tech stack
- Java 21 (Amazon Corretto toolchain configured in build)
- Spring Boot 4
- Spring Data JPA (Hibernate)
- Thymeleaf (+ Spring Security dialect)
- Flyway (mysql)
- MySQL (connector provided)
- Lombok (compile-time)
- Gradle wrapper (included)

## Repository layout (high level)
- src/main/java/gr/aueb/cf/schoolapp — application code
    - authentication — security, success/failure handlers, user details service
    - controller — MVC controllers (LoginController, TeacherController, UserController)
    - model — JPA entities (Teacher, User, Role, Region, …)
    - repository — Spring Data repositories
    - service — service layer and interfaces
    - dto / mapper / validator — DTOs, mapping and validation logic
- src/main/resources
    - templates — Thymeleaf views
    - static — CSS, images
    - db/migration — Flyway SQL migrations (V1..V6)
    - application-*.properties — profile-specific configuration
    - messages.properties / messages_el.properties — i18n

## Requirements
- Java 21 JDK (Amazon Corretto vendor recommended by the build)
- MySQL server
- Gradle wrapper (bundled; commands shown below)

## Environment variables
The project supports loading an optional `.env` file for DB credentials. See `.env.example`:

- MYSQL_HOST
- MYSQL_PORT
- MYSQL_DB
- MYSQL_USER
- MYSQL_PASSWORD

You can put these in a `.env` file or set them in your environment. The `application-dev.properties` imports `.env` optionally:
spring.config.import=optional:file:.env[.properties]

## Configuration notes
- By default `application.properties` sets `spring.profiles.active=prod`. For local development use the `dev` profile.
- JPA: `spring.jpa.hibernate.ddl-auto=validate` — the database schema must match the entity model. Flyway migrations are provided to create the schema (see `src/main/resources/db/migration`).
- `spring.jpa.open-in-view=false` — OSIV is disabled (best practice). Use explicit fetch strategies (JOIN FETCH / EntityGraph) to avoid lazy-loading problems.
- Flyway is enabled / used; migrations live under `src/main/resources/db/migration`.

## Build & Run

1. Create a `.env` file or provide the MySQL connection values as environment variables (or edit the profile `application-*.properties`):
    - Copy `.env.example` -> `.env` and fill values.

2. Run with the Gradle wrapper:

- Build:
    - Linux / macOS:
        - ./gradlew clean build
    - Windows:
        - gradlew.bat clean build

- Run the application (dev profile recommended for local):
    - ./gradlew bootRun --args='--spring.profiles.active=dev'
    - or (after build) java -jar build/libs/schoolapp10-ssr-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

3. Tests:
- ./gradlew test

## Database migration
Flyway SQL files are present in `src/main/resources/db/migration` (V1..V6). When the application runs, Flyway will apply migrations to the configured MySQL database.

If you change entities and need to update schema:
- Create a new Flyway migration SQL (e.g. V7__your_changes.sql) and place it in the migrations folder.
- Keep `spring.jpa.hibernate.ddl-auto=validate` in production to prevent accidental destructive changes.

## Common troubleshooting
- Schema validation failures (Hibernate validate): ensure your DB has all Flyway migrations applied; run the app with the proper DB credentials and check Flyway logs.
- Authentication or role problems: roles and capabilities are seeded via migrations (see V6). Ensure the data exists in the DB.
- If Thymeleaf templates show stale content locally, set `spring.thymeleaf.cache=false` (already set in `application-dev.properties`).

## Development tips
- Use the `dev` profile for local development to get logging and non-cached Thymeleaf templates:
    - ./gradlew bootRun --args='--spring.profiles.active=dev'
- Check logs in `logs/schoolapp.log` (rolling policy configured in `application-dev.properties`).
- The application uses Spring Security with custom success/failure handlers and a custom `UserDetailsService` implementation — see `authentication` package.

## License
No license specified in the repository. Add a LICENSE file if you want to make the project open source under a standard license.

## Contributing
Contributions are welcome. Suggested workflow:
1. Fork the repo
2. Create a feature branch
3. Add tests for new behaviour
4. Open a PR describing your changes

## Contact / Author
Repository owner: a8anassis
GitHub: https://github.com/a8anassis/schoolapp10-ssr
