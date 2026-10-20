# Pedidos360 · Frontend

Frontend del sistema **Pedidos360**, desarrollado para la asignatura **DSY1107 Desarrollo Cloud Native I**.

## Sobre el proyecto

Pedidos360 es un sistema para gestionar pedidos. Este repositorio es la **página web** que usa el usuario: desde aquí inicia sesión con su cuenta de Microsoft, ve la lista de pedidos y crea pedidos nuevos.

El sistema completo tiene tres partes:

- **Este frontend**, hecho en Angular.
- **Un backend** en Spring Boot, que guarda los pedidos y usa RabbitMQ para hacer tareas en segundo plano (avisos, tickets y comprobantes).
- **Microsoft Entra ID**, que se encarga del inicio de sesión y entrega un token que prueba quién es el usuario.

```
Usuario
  │
  ▼
Frontend (Angular) ──► inicia sesión en Microsoft Entra ID
  │                       y recibe un token
  │
  └── envía el token en cada petición
          │
          ▼
     API Gateway ──► Backend Pedidos360 ──► RabbitMQ
```

## ¿Qué usa?

- **Angular**, para construir la página.
- **MSAL**, la librería de Microsoft para iniciar sesión con **Entra ID**.

## ¿Cómo funciona el inicio de sesión?

1. El usuario entra a la página y presiona **Iniciar sesión**.
2. La página lo lleva a la pantalla de Microsoft, donde escribe su cuenta y contraseña.
3. Microsoft lo devuelve a la página junto con un **token**, que es como una credencial temporal.
4. Desde ese momento, cada vez que la página pide o crea pedidos, MSAL agrega ese token automáticamente. El backend lo revisa antes de responder.
5. Con **Cerrar sesión** se borra la sesión y hay que volver a ingresar.

El inicio de sesión usa el flujo recomendado por Microsoft (**Authorization Code con PKCE**), que evita que el token quede expuesto en la dirección de la página.

La página principal está protegida: si el usuario no ha iniciado sesión, lo manda primero a Microsoft.

## ¿Qué se puede hacer en la página?

- **Ver los pedidos:** al entrar, se muestra la lista de todos los pedidos con su número, cliente, producto, cantidad y estado.
- **Crear un pedido:** se llena el formulario con cliente, producto y cantidad, y se presiona **Crear**. El pedido aparece en la lista.

Cuando se crea un pedido, el backend lo guarda y avisa a RabbitMQ. Así se generan, en segundo plano, la notificación al cliente, el ticket y el comprobante, sin que la página tenga que esperar.

## ¿Cómo lo ejecuto?

Necesitas tener **Node.js** instalado.

```
npm install
npx ng serve
```

Luego abre http://localhost:4200 en el navegador.

El backend tiene que estar encendido en http://localhost:8081 para que la página muestre datos.

## Configuración

Los datos de conexión están en `src/environments/environment.ts`:

- `clientId` y `tenantId`: identifican la aplicación registrada en Entra ID.
- `redirectUri`: la dirección a la que Microsoft devuelve al usuario después del login.
- `protectedResourceScopes`: el permiso que se pide para poder crear pedidos.
- `apiBaseUrl`: la dirección del backend.

## ¿Dónde está cada cosa?

```
src/app/
├── app.component.*        Barra superior con los botones de iniciar y cerrar sesión
├── home/                  Página principal: lista de pedidos y formulario
├── app.routes.ts          Rutas de la página (la principal está protegida)
├── msal-config.ts         Configuración de MSAL con Entra ID
├── msal-guard-config.ts   Protección de la página principal
└── msal-interceptor-config.ts   Agrega el token a las llamadas al backend
```

## Autor

Benjamín Aedo
