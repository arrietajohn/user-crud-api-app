# Arquitectura y contrato de la API

## Alcance de esta entrega

Domain y Adapter implementados. El modulo `:domain` es Java 11 puro, sin Android,
HTTP, JSON, almacenamiento ni frameworks de inyeccion. `:adapter` implementa sus
puertos y `:app` depende de ambos. Application y Entrypoint quedan para las
siguientes entregas. Ver [detalle de Adapter](adapter.md).

Contrato consultado el 26 de octubre de 2026:
https://users-management-api-orb0.onrender.com/v3/api-docs

Swagger:
https://users-management-api-orb0.onrender.com/swagger-ui/index.html

## Operaciones verificadas

| Operacion | Endpoint | Autenticacion | Resultado |
| --- | --- | --- | --- |
| Login | POST /api/auth/login | Publica | accessToken, tokenType, expiresIn |
| Crear | POST /api/users | Publica | Usuario; 201 nuevo, 200 reintento identico |
| Consultar | GET /api/users/{id} | Bearer JWT | Usuario |
| Listar | GET /api/users | Bearer JWT | Lista completa ordenada por nombre |
| Actualizar | PUT /api/users/{id} | Bearer JWT | Usuario |
| Eliminar | DELETE /api/users/{id} | Bearer JWT | 204 sin cuerpo |
| Disponibilidad | GET /health | Publica | name, version |

Usuario: id, name, email, role, status. Nunca incluye password.
Crear requiere id, name (minimo 3), email, password (minimo 8), role.
Actualizar requiere name, email, role, status; password es opcional.
Login requiere email y password (minimo 8).
Los errores exponen status y message; se documentan 400, 401, 404, 409, 500, 503.
No se ejecutaron escrituras ni se crearon usuarios en el servicio remoto.

## Decisiones de dominio

- Un puerto por operacion de autenticacion remota y CRUDL, sin repositorio generico.
- SaveSessionPort, LoadSessionPort y ClearSessionPort separan la persistencia de sesion.
  Un Optional vacio en carga representa ausencia; los fallos de almacenamiento se reportan.
- Objetos de valor inmutables para ID, nombre, correo, contrasena, token, rol y estado.
- OpenAPI no declara valores permitidos de role/status. No se inventan enums cerrados.
  FailureReason es el enum de errores independiente del transporte.
- UserRegistration separa credenciales de User. UserUpdate permite Optional.empty()
  para conservar la contrasena; el mapper futuro debe omitir ese campo en JSON.
- No se cambia capitalizacion ni se recortan credenciales o identificadores.
- Password y AccessToken ocultan su contenido en toString(). Sus getters son para
  serializacion/persistencia controlada; no deben registrarse en logs.
- AuthSession utiliza una duracion explicita en segundos y una fecha de recepcion.
  OpenAPI no indica la unidad de expiresIn; se confirmo en el codigo local del
  backend: AuthRestController devuelve JwtTokenService.expirationSeconds().
  Adapter usa segundos y captura el reloj al iniciar login de forma conservadora.
- Los puertos son sincronos. La aplicacion debe ejecutarlos fuera del hilo principal;
  cancelacion, timeouts y ciclo de vida se resolveran al integrar Android.
- DomainException comunica FailureReason; ValidationException identifica el campo.
  Los adaptadores traduciran errores HTTP, red y almacenamiento a estas excepciones.
  No se toma un codigo HTTP como una regla de negocio del dominio.

## Dependencias e inyeccion

Domain no necesita DI ni Lombok. Se usa inyeccion por constructor al implementar
servicios. La eleccion entre composicion manual y Dagger/Hilt se cerrara en la
integracion, considerando que este proyecto usa Java. No se agrega un procesador
de anotaciones antes de necesitarlo.

Android minSdk 24 requiere desugaring de java.time al integrar AuthSession en la app.
Ya se configuro en `:adapter` y `:app`. El modulo Java no necesita Android.

## Verificacion

`gradlew.bat :domain:test`

Pruebas de validacion, secretos, actualizacion sin contrasena y limites de expiracion.
