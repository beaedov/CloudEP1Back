# Pedidos360 — Backend (microservicio de Pedidos)

API REST en Spring Boot para el proyecto Pedidos360 (Cloud Native I, DSY1107).
Sigue el flujo: **Angular + MSAL → Entra ID → Access Token JWT → este backend
(Resource Server) → API Gateway**.

## 1. Requisitos

- Java 17+
- Maven (o usa el wrapper `./mvnw` si lo agregas con `mvn -N wrapper:wrapper`)

## 2. Levantar el backend AHORA MISMO (sin Entra ID todavía)

El proyecto trae H2 (base en memoria) activada por defecto, así que **compila y
arranca de inmediato**:

```bash
mvn spring-boot:run
```

Verás la app en `http://localhost:8081`. La consola de la base de datos está en
`http://localhost:8081/h2-console` (JDBC URL: `jdbc:h2:mem:pedidos360db`, usuario `sa`, sin password).

⚠️ Como `SecurityConfig` exige `anyRequest().authenticated()`, **todos los
endpoints piden un JWT válido**, incluso en este modo. Es intencional: así
verificas que la seguridad realmente bloquea accesos sin token (Prueba A del
punto 13 de la guía del curso).

## 3. Endpoints disponibles

| Método | Ruta                | Requiere                                  |
|--------|---------------------|--------------------------------------------|
| GET    | `/api/pedidos`       | Token válido (cualquier usuario autenticado) |
| GET    | `/api/pedidos/{id}`  | Token válido |
| POST   | `/api/pedidos`       | Token válido **+ scope `Pedidos.Create`** |
| PUT    | `/api/pedidos/{id}`  | Token válido |
| DELETE | `/api/pedidos/{id}`  | Token válido |

Ejemplo de body para `POST`/`PUT`:

```json
{
  "cliente": "Juan Pérez",
  "producto": "Notebook Lenovo",
  "cantidad": 2,
  "estado": "PENDIENTE"
}
```

## 4. Conectar con tu tenant real de Microsoft Entra ID

1. En el portal de Entra ID, ve a **Registros de aplicaciones** y selecciona
   (o crea) el registro correspondiente a este backend/API.
2. Anota: **Directory (tenant) ID**, **Application (client) ID** y define el
   **Application ID URI** (por ejemplo `api://pedidos360`).
3. En **Exponer una API**, agrega el ámbito `Pedidos.Create` (nombre visible:
   "Crear pedido", descripción: "Permite crear pedidos").
4. Reemplaza en `src/main/resources/application.properties`:
   - `TU_TENANT_ID`
   - `TU_API_CLIENT_ID`
   - el `app-id-uri` si usaste uno distinto a `api://pedidos360`
5. Reinicia el backend.

Desde el frontend Angular (MSAL), solicita el scope
`api://pedidos360/Pedidos.Create` para poder llamar al `POST`. Los `GET` y
`PUT`/`DELETE` sólo requieren que el usuario esté autenticado (sin scope
específico); ajústalo con más `@PreAuthorize` si tu pauta pide roles/scopes
adicionales por operación.

## 5. Probar la seguridad con curl

```bash
# Sin token -> debe responder 401
curl -i http://localhost:8081/api/pedidos

# Con token -> debe responder 200
curl -i http://localhost:8081/api/pedidos \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

# Crear pedido -> requiere ademas el scope Pedidos.Create -> si falta, 403
curl -i -X POST http://localhost:8081/api/pedidos \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"cliente":"Ana","producto":"Mouse","cantidad":1}'
```

Guarda estas capturas/respuestas (200, 401, 403): la pauta de evaluación pide
**evidencia del funcionamiento de cada ruta**, con y sin token.

## 6. Conectar a una base de datos cloud (para el despliegue en EC2)

Cuando tengas tu instancia (RDS/PostgreSQL u otra), abre
`application.properties`, comenta el bloque de H2 y descomenta/completa el
bloque "Opción B" con tu endpoint, usuario y password reales. No subas
credenciales reales al repositorio: usa variables de entorno o un
`application-local.properties` (ya está en `.gitignore`).

## 7. Estructura del proyecto

```
src/main/java/com/pedidos360/backend/
├── BackendApplication.java     # clase principal
├── config/SecurityConfig.java  # Resource Server + JWT + CORS
├── controller/PedidoController.java
├── model/Pedido.java           # entidad JPA
├── model/PedidoRequest.java    # DTO con validaciones
├── repository/PedidoRepository.java
├── service/PedidoService.java
└── exception/                  # manejo de errores en JSON
```

## 8. Siguientes pasos sugeridos para tu entrega

- [ ] Reemplazar los valores de Entra ID en `application.properties`.
- [ ] Desplegar en EC2 (o probar local) y registrar la ruta en AWS API Gateway.
- [ ] Configurar CORS en el API Manager (además del CORS ya configurado aquí).
- [ ] Conectar el frontend Angular con MSAL y consumir estos endpoints.
- [ ] Tomar evidencia (capturas o Postman) de cada ruta con y sin token.
