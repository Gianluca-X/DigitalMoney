# 📘 Guía de Desarrollo - Digital Money House

Este documento describe los **sprints, historias de usuario, arquitectura y pruebas** realizadas en el desarrollo del proyecto **Digital Money House**.

---

## 🏗️ Introducción

La aplicación implementa una **billetera digital** con funcionalidades de:  
- Registro e inicio de sesión.  
- Gestión de usuarios y cuentas digitales.  
- Transferencias de dinero y movimientos.  
- Manejo de tarjetas asociadas.  

La arquitectura está basada en **microservicios**, lo que permite escalabilidad y mantenibilidad.

---

## 📐 Arquitectura de Microservicios

- **Auth Service:** Registro, login y autenticación con JWT.  
- **User Service:** Gestión de perfil y usuarios.  
- **Account Service:** Manejo de cuentas digitales, saldos, alias y tarjetas.  
- **Eureka Server:** Descubrimiento de servicios.  
- **API Gateway:** Enrutamiento de peticiones.

---

## 📅 Desarrollo por Sprints

### ✅ Sprint 1
- Registro de usuario (`POST /users/register`)  
- Login de usuario (`POST /auth/login`)  
- Logout de usuario (`POST /auth/logout`)  

### ✅ Sprint 2
- Consulta de cuentas (`GET /accounts/{id}`)  
- Listado de movimientos (`GET /accounts/{id}/transactions`)  
- CRUD de tarjetas (`POST/GET/DELETE /accounts/{id}/cards`)  

### ✅ Sprint 3
- Ingreso de dinero desde tarjeta a cuenta.  
- Consulta de actividad de la cuenta.  

### ✅ Sprint 4
- Transferencias entre cuentas (`POST /accounts/{id}/transfers`).  
- Validación de fondos insuficientes.  

---

## 🧪 Testing

- **Pruebas exploratorias:** realizadas en Postman.  
- **Pruebas manuales:** checklist por cada historia de usuario.  
- **Pruebas automatizadas:** con JUnit y RestAssured.  

📌 **Colección de Postman:**  
[Link aquí](https://www.postman.com/cryosat-cosmologist-51288854/workspace/moneydigitalhouse/collection/23314152-70434730-fb31-45b6-bb1f-4d56618f7af9?action=share&source=copy-link&creator=23314152)

📌 **Drive con plan de pruebas + casos:**  
[Enlace al Drive](https://drive.google.com/) _(actualizar con link real)_

---

## ⚙️ Decisiones Técnicas

- Se eligió **JWT** sobre Keycloak para simplificar la gestión de tokens.  
- Base de datos: **MySQL**, centralizada para fines didácticos.  
- Despliegue con **Docker Compose** para orquestar microservicios.  
- Documentación automática con **Swagger**.  

---

## 👥 Créditos

- **Autor:** Gianluca X  
- **Repositorio:** [DigitalMoney](https://github.com/Gianluca-X/DigitalMoney)  

---
