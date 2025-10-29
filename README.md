# 💸 Digital Money House

**Billetera Digital** desarrollada con **arquitectura de microservicios** para gestionar usuarios, cuentas, transferencias y tarjetas.

---

## 🛠️ Tecnologías Utilizadas

- Java 21 / Spring Boot 3.x  
- Spring Cloud (Eureka, Gateway, Feign)  
- MySQL  
- JPA / Hibernate  
- Docker / Docker Compose  
- JWT (Autenticación basada en tokens)  
- Swagger / OpenAPI  
- JUnit / RestAssured  
- Git / GitHub  

---

## 📐 Arquitectura del Sistema

```
[User Service]   [Account Service]   [Auth Service]
      \               |                   /
               [API Gateway]
                    |
              [Eureka Server]
```

- **Auth Service:** Registro, login y verificación de usuarios.  
- **User Service:** Gestión de usuarios y perfil.  
- **Account Service:** Cuentas digitales, alias, saldos y movimientos.  
- **Transaction Service:** Transferencias y actividad de la cuenta.  
- **API Gateway:** Enrutamiento centralizado.  
- **Eureka Server:** Descubrimiento de servicios.  

---

## 🚀 Guía de Instalación y Ejecución

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/Gianluca-X/DigitalMoney.git
   cd DigitalMoney
   ```

2. **Levantar base de datos y microservicios**
   ```bash
   docker-compose up
   ```

3. **Acceder a la documentación de la API**
   - Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  

---

## 🛣️ Endpoints Principales

### 🔑 Auth Service
- `POST /auth/register` → Registrar nuevo usuario  
- `POST /auth/login` → Iniciar sesión  
- `POST /auth/logout` → Cerrar sesión  

### 👤 User Service
- `GET /users/{id}` → Obtener datos del usuario  
- `PUT /users/{id}` → Editar datos personales  

### 💳 Account Service
- `GET /accounts/{id}` → Consultar saldo  
- `GET /accounts/{id}/transactions` → Movimientos de la cuenta  
- `POST /accounts/{id}/cards` → Asociar tarjeta  
- `DELETE /accounts/{id}/cards/{idCard}` → Eliminar tarjeta  

### 🔄 Transaction Service
- `POST /accounts/{id}/transfers` → Realizar transferencia  
- `GET /accounts/{id}/activity` → Listar historial de transacciones  

📌 **Colección de pruebas Postman:**  
[Link a Postman](https://www.postman.com/cryosat-cosmologist-51288854/workspace/moneydigitalhouse/collection/23314152-70434730-fb31-45b6-bb1f-4d56618f7af9?action=share&source=copy-link&creator=23314152)

---

## 🧪 Testing

- **Testing manual:** Se realizaron pruebas exploratorias con Postman.  
- **Testing automatizado:** JUnit y RestAssured (`mvn test`).  
- **Drive con documentación de testing:** [Enlace a Google Drive](https://drive.google.com/drive/folders/1opUoRVye9heD8I8p3zUgNyAjeuttQmXP) _(actualizar)_  

---

## 👥 Créditos

- **Autor:** Gianluca X  
- **Repositorio:** [DigitalMoney](https://github.com/Gianluca-X/DigitalMoney)  

---
