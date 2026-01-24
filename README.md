<div align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&height=120&section=header&animation=fadeIn" />
</div>

<h1 align="center">🛒 TodoTechShop - Backend E-commerce</h1>

<h3 align="center">🚀 Backend Spring Boot para plataforma de tecnología con arquitectura por capas</h3>

<p align="center">
  Sistema backend completo para e-commerce especializado en productos tecnológicos.<br>
  Implementa arquitectura por capas, autenticación JWT, carrito de compras y gestión de órdenes.
</p>

---

## 📋 **Descripción del Proyecto**

**TodoTechShop Backend** es una API REST desarrollada en **Spring Boot** que sirve como backend completo para una plataforma de e-commerce especializada en productos tecnológicos. El sistema implementa todas las funcionalidades necesarias para un e-commerce moderno, incluyendo gestión de productos, carrito de compras, procesamiento de pedidos, autenticación de usuarios y pasarela de pagos.

---

## 🏗️ **Arquitectura por Capas**

---

## 🧩 **Entidades Principales**

| Entidad | Descripción |
|---------|-------------|
| **Producto** | Productos tecnológicos con categorías |
| **Categoria** | Clasificación de productos |
| **Cliente** | Información de clientes registrados |
| **Usuario** | Usuarios del sistema con autenticación |
| **Orden** | Pedidos completos del sistema |
| **DetalleOrden** | Items específicos dentro de una orden |
| **Pago** | Transacciones de pagos procesadas |
| **MetodoPago** | Métodos de pago disponibles |
| **Inventario** | Gestión de stock de productos |
| **Despacho** | Información de envíos |

---

## 🔧 **Tech Stack**

### **Backend & Framework**
<div align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img width="8" />
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img width="8" />
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white" />
</div>

### **Base de Datos**
<div align="center">
  <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img width="8" />
  <img src="https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white" />
</div>

### **Infraestructura & Despliegue**
<div align="center">
  <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white" />
  <img width="8" />
  <img src="https://img.shields.io/badge/Render-46E3B7?style=for-the-badge&logo=render&logoColor=white" />
  <img width="8" />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
</div>

---

## 🚀 **Endpoints Principales**

### **🔐 Autenticación & Usuarios**
- `POST /api/auth/register` - Registro de nuevos usuarios
- `POST /api/auth/login` - Inicio de sesión con JWT
- `GET /api/usuarios/perfil` - Perfil de usuario

### **🛍️ Productos & Catálogo**
- `GET /api/productos` - Listar todos los productos
- `GET /api/productos/{id}` - Obtener producto por ID
- `GET /api/productos/categoria/{categoriaId}` - Productos por categoría
- `POST /api/productos` - Crear nuevo producto (Admin)

### **🛒 Carrito & Órdenes**
- `POST /api/carrito/agregar` - Agregar producto al carrito
- `GET /api/carrito` - Ver carrito actual
- `POST /api/ordenes/crear` - Crear orden desde carrito
- `GET /api/ordenes/usuario` - Historial de órdenes del usuario

### **💳 Pagos & Transacciones**
- `POST /api/pagos/procesar` - Procesar pago de orden
- `GET /api/pagos/metodos` - Métodos de pago disponibles

---

## ☁️ **Despliegue**

| Componente | Plataforma | URL/Detalles |
|------------|------------|--------------|
| **Backend API** | AWS Elastic Beanstalk/EC2 | API REST principal |
| **Base de Datos** | Render (PostgreSQL) | Base de datos alojada |
| **Documentación** | Swagger UI | `/swagger-ui.html` |
| **Monitorización** | Spring Boot Actuator | `/actuator/health` |

---

## ⚙️ **Configuración Local**

### **Requisitos**
- Java 17+
- Maven 3.6+
- PostgreSQL 14+



👨‍💻 Autor
<div align="center">
Santiago Arbelaez Contreras

Junior Full Stack Developer

Estudiante de Ingeniería de Sistemas – Universidad del Quindío

<br> <a href="https://github.com/santiagoarbelaezc"> <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white" /> </a> <img width="10" /> <a href="https://www.linkedin.com/in/santiago-arbelaez-contreras-9830b5290/"> <img src="https://img.shields.io/badge/LinkedIn-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white" /> </a> <img width="10" /> <a href="mailto:arbelaezz.c11@gmail.com"> <img src="https://img.shields.io/badge/Email-EA4335?style=for-the-badge&logo=gmail&logoColor=white" /> </a></div><div align="center"> <img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&height=90&section=footer&animation=fadeIn" /> </div>
