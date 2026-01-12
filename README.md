# Falcon Airlines – Flight Reservation System (Backend)

Backend system for managing flight scheduling, seat availability, and reservations..

This project is designed as a modular, scalable backend service that automatically generates flights based on predefined routes and schedules, and supports seat management, reservations, and future operational extensions.

---

## 🚀 Project Overview

The Flight Reservation System handles:

- Route and schedule definition
- Automatic flight generation up to a configurable horizon (e.g. 180 days)
- Aircraft type and seat capacity management
- Seat availability and reservation logic

The system is built with a clean architecture mindset, focusing on maintainability, clarity, and real-world airline domain modeling.

---

## 🏗️ Architecture

The application follows a layered architecture inspired by MVC principles:

- **Web layer** – REST API endpoints and external communication
- **Service layer** – Business logic
- **Persistence layer** – Data access using Spring Data JPA

Key domain concepts:

- Route
- RouteSchedule
- Flight
- AirplaneType
- Seat
- Reservation

---

## 🛠️ Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA
- Spring Security
- PostgreSQL
- Flyway (database migrations)
- Gradle

---

## 📦 Database Strategy

- Database schema is managed using Flyway migrations
- No automatic schema generation in production
- Entities are aligned strictly with migration scripts


## 📄 License

This project is for educational and portfolio purposes.

---

## 👤 Author

Juan Camilo Londoño  
Backend Developer (Java / Spring Boot)
