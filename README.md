# Sistema de Reservas de Restaurante

Spring Boot 3.2.5 + Java 17 + Thymeleaf + Spring Data JPA (Hibernate) + Spring Security + MySQL.

## Requisitos
- Java 17
- Maven (o usa el wrapper si lo generas desde Spring Initializr)
- MySQL corriendo en localhost:3306

## Configuración de la base de datos
El proyecto crea la base de datos automáticamente (`createDatabaseIfNotExist=true`).
Edita `src/main/resources/application.properties` si tu usuario/contraseña de MySQL son distintos:

```
spring.datasource.username=root
spring.datasource.password=root
```

## Ejecutar
```
mvn spring-boot:run
```
o desde tu IDE, ejecuta `ReservasApplication.java`.

La app queda disponible en: http://localhost:8080

## Usuarios de prueba (se crean solos al arrancar)
- Admin  → DNI: 00000000 / contraseña: admin123
- Cliente → DNI: 11111111 / contraseña: cliente123

## Reglas de negocio implementadas
- Un cliente solo puede tener **una reserva activa** a la vez.
- Al reservar, el **plato principal es obligatorio**; postre y bebida son opcionales.
- El admin puede **crear/eliminar mesas y platos**, ver todas las reservas, **cancelarlas** o **moverlas** a otra mesa libre.
- Login con **DNI + contraseña** (BCrypt) para ambos roles.
