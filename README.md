⭐ Fullstack Digital Wallet
Microservices Architecture
Spring Boot + React + PostgreSQL
# 💸 Digital Money House

Digital Money House es una **billetera digital fullstack** desarrollada con arquitectura de **microservicios**.

Permite gestionar:

- usuarios
- cuentas
- tarjetas
- transferencias
- historial de transacciones

El proyecto simula el funcionamiento de una **plataforma fintech**, separando responsabilidades en distintos microservicios.

---

# 🚀 Demo del Proyecto

### 🌐 Frontend (Producción)

https://digital-money-front-end.vercel.app/

### 🎥 Video Demo

https://drive.google.com/file/d/1F4T0-C_T-sGwCIoXsECtkf-tLYchHWeQ/view

### 💻 Repositorio Frontend

https://github.com/Gianluca-X/DigitalMoneyFrontEnd

### ⚙️ Repositorio Backend

https://github.com/Gianluca-X/DigitalMoney

---

# 🛠️ Tecnologías Utilizadas

## Backend

- Java 21
- Spring Boot
- Spring Cloud
- JWT Authentication
- JPA / Hibernate

## Frontend

- React
- TypeScript
- Axios
- React Router

## Infraestructura

- Docker
- Docker Compose
- PostgreSQL

## Testing

- JUnit
- RestAssured

## Documentación

- Swagger / OpenAPI

## Deploy

Frontend → Vercel  
Backend → Render

---

# 📐 Arquitectura del Sistema

Arquitectura basada en **microservicios**.
            Frontend (React)
                    │
                    ▼
             API Gateway
                    │
    ┌───────────────┼───────────────┐
    │               │               │
 Auth Service    User Service    Account Service
    │               │               │
    └───────────────▼───────────────┘
            PostgreSQL Database

---

# 📦 Microservicios

## Auth Service

Responsable de autenticación.

Funciones:

- Registro
- Login
- Generación de JWT

---

## User Service

Gestión de usuarios.

Funciones:

- Crear usuario
- Editar perfil
- Obtener datos

---

## Account Service

Gestión de cuentas.

Funciones:

- consultar saldo
- asociar tarjetas
- movimientos

---

## Transaction Service

Transferencias entre cuentas.

Funciones:

- enviar dinero
- historial de actividad

---

## API Gateway

Entrada centralizada del sistema.

Responsabilidades:

- routing
- seguridad
- comunicación entre servicios

---

## Eureka Server

Service discovery para microservicios.

---

# 🗄️ Base de Datos

El proyecto inicialmente utilizaba **MySQL** pero fue migrado a **PostgreSQL** en la rama:


feature/migrate-to-postgresql


---

# ⚙️ Instalación Local

Clonar repositorio


git clone https://github.com/Gianluca-X/DigitalMoney.git

cd DigitalMoney


Levantar servicios


docker-compose up


---

# 📚 Documentación API

Swagger disponible en:


http://localhost:8080/swagger-ui.html


---

# 🧪 Testing

Testing manual:

- Postman

Testing automatizado:


mvn test


Tecnologías:

- JUnit
- RestAssured

---

# 👨‍💻 Autor

Gianluca Fucci  

Backend Developer

GitHub  
https://github.com/Gianluca-X
