# ⭐ Fullstack Digital Wallet  
### Microservices Architecture | Spring Boot + React + PostgreSQL

---

## 💸 Digital Money House

**Digital Money House** es una billetera digital fullstack desarrollada con arquitectura de microservicios.

Permite gestionar:

- 👤 Usuarios  
- 💳 Cuentas  
- 💰 Tarjetas  
- 🔄 Transferencias  
- 📊 Historial de transacciones  

El proyecto simula el funcionamiento de una plataforma fintech real, separando responsabilidades en distintos microservicios.

---

## 🚀 Demo del Proyecto

### 🌐 Frontend (Producción)
👉 https://digital-money-front-end.vercel.app/

### 🎥 Video Demo
👉 https://drive.google.com/file/d/1F4T0-C_T-sGwCIoXsECtkf-tLYchHWeQ/view

### 💻 Repositorio Frontend
👉 https://github.com/Gianluca-X/DigitalMoneyFrontEnd

### ⚙️ Repositorio Backend
👉 https://github.com/Gianluca-X/DigitalMoney

---

## 🛠️ Tecnologías Utilizadas

### 🔧 Backend
- Java 21  
- Spring Boot  
- Spring Cloud  
- JWT Authentication  
- JPA / Hibernate  

### 🎨 Frontend
- React  
- TypeScript  
- Axios  
- React Router  

### ☁️ Infraestructura
- Docker  
- Docker Compose  
- PostgreSQL  

### 🧪 Testing
- JUnit  
- RestAssured  

### 📄 Documentación
- Swagger / OpenAPI  

### 🚀 Deploy
- Frontend → Vercel  
- Backend → Render  

---

## 📐 Arquitectura del Sistema

Arquitectura basada en microservicios:
            
             Frontend (React)
                     │
                     ▼
               API Gateway
                     │

     ┌───────────────┼───────────────┐
   
     │               │               │
     Auth Service User Service Account Service
     │               │               │
     
     └───────────────▼───────────────┘
            PostgreSQL Database

            
---

## 📦 Microservicios

### 🔐 Auth Service
Responsable de autenticación.

Funciones:
- Registro  
- Login  
- Generación de JWT  

---

### 👤 User Service
Gestión de usuarios.

Funciones:
- Crear usuario  
- Editar perfil  
- Obtener datos  

---

### 💳 Account Service
Gestión de cuentas.

Funciones:
- Consultar saldo  
- Asociar tarjetas  
- Movimientos  

---

### 🔄 Transaction Service
Transferencias entre cuentas.

Funciones:
- Enviar dinero  
- Historial de actividad  

---

### 🚪 API Gateway
Entrada centralizada del sistema.

Responsabilidades:
- Routing  
- Seguridad  
- Comunicación entre servicios  

---

### 🧭 Eureka Server
Service discovery para microservicios.

---

## 🗄️ Base de Datos

El proyecto inicialmente utilizaba **MySQL**, pero fue migrado a **PostgreSQL** en la rama:


feature/migrate-to-postgresql


---

## ⚙️ Instalación Local

### 1️⃣ Clonar repositorio
    ```bash
     git clone https://github.com/Gianluca-X/DigitalMoney.git
     cd DigitalMoney

2️⃣ Levantar servicios
    docker-compose up

📚 Documentación API

Swagger disponible en:

👉 http://localhost:8080/swagger-ui.html

📬 Colección Postman

Para probar los endpoints fácilmente:

👉 https://www.postman.com/cryosat-cosmologist-51288854/workspace/moneydigitalhouse/collection/23314152-70434730-fb31-45b6-bb1f-4d56618f7af9?action=share&source=copy-link&creator=23314152

🔹 Incluye:
Auth (login / register)
Users
Accounts
Transfers
Cards

🧪 Testing
🔹 Testing manual
Postman
🔹 Testing automatizado
mvn test
🔹 Tecnologías
JUnit
RestAssured

⚡ Concurrencia y Consistencia de Datos

El sistema implementa mecanismos para garantizar la integridad de los saldos en escenarios concurrentes.

🧠 Problema

Race conditions
Inconsistencias en balances
Pérdida de dinero

✅ Solución Implementada
🔒 Lock pesimista
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Account a WHERE a.id = :id")
Optional<Account> findByIdForUpdate(Long id);

✔ Evita accesos simultáneos sobre la misma cuenta.

🔁 Transacciones atómicas
@Transactional

✔ Rollback automático ante errores.

🔐 Usuario autenticado
SecurityContextHolder.getContext().getAuthentication().getPrincipal();

✔ Mejora seguridad y evita manipulación externa.

🧪 Test de concurrencia
- Uso de ExecutorService
- Simulación de múltiples transferencias simultáneas
- Validación de saldo final consistente

✔ El sistema mantiene integridad bajo carga concurrente.

🛠️ Mejoras Implementadas

🔥 Refactor del Service
- Eliminación de accountId como parámetro
- Uso de usuario autenticado

❌ Manejo de excepciones
- Reemplazo de AccountNotFoundException
- Uso de ResourceNotFoundException

💰 Validaciones de negocio
- Monto mayor a 0
- No auto-transferencias
- Validación de saldo
- Validación de tarjeta

📊 Registro de actividad
- transfer-in
- transfer-out
- deposit

🔄 Estados de transferencia (mejora futura)
- PENDING
- SUCCESS
- FAILED

👨‍💻 Autor

Gianluca Fucci
Backend Developer

🔗 GitHub
👉 https://github.com/Gianluca-X
