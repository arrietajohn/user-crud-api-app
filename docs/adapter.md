# Capa Adapter

Implementada en `:adapter`, una biblioteca Android que depende de `:domain`.
La app referencia ambos modulos; todavia no invoca casos de uso ni cambia su GUI.

## Organizacion

| Paquete | Responsabilidad |
| --- | --- |
| dto/request | LoginRequest, CreateUserRequest, UpdateUserRequest |
| dto/response | LoginResponse, UserResponse, ApiErrorResponse |
| dto/cache | Representacion persistida de la sesion |
| mapper | Traduccion entre DTOs y modelos del dominio |
| http | Contrato Retrofit, conversion JSON, ejecucion y autorizacion |
| remote | Un adaptador por operacion remota |
| cache | Cifrado AES-GCM, Android Keystore y archivo atomico |
| local | Un adaptador por puerto de persistencia de sesion |
| config | Configuracion HTTP y composicion por constructor |

## Puertos implementados

| Puerto de Domain | Adaptador |
| --- | --- |
| LoginPort | LoginAdapter |
| CreateUserPort | CreateUserAdapter |
| GetUserByIdPort | GetUserByIdAdapter |
| ListUsersPort | ListUsersAdapter |
| UpdateUserPort | UpdateUserAdapter |
| DeleteUserPort | DeleteUserAdapter |
| SaveSessionPort | SaveSessionAdapter |
| LoadSessionPort | LoadSessionAdapter |
| ClearSessionPort | ClearSessionAdapter |

`AdapterFactory` construye e interconecta una instancia de cada adaptador.
Debe crearse una vez desde la composicion Android y compartirse con los futuros
servicios de aplicacion. Expone los puertos, sin estado global ni service locator
estatico. Las dependencias tambien se pueden suministrar directamente por
constructor, como hacen las pruebas.

## HTTP y contrato

- Base URL fija HTTPS: https://users-management-api-orb0.onrender.com/
- Retrofit 3.0.0, OkHttp 4.12.0 y Gson 2.13.2; versiones en el catalogo Gradle.
- Login y creacion son publicos y no reciben Authorization.
- Consultar, listar, actualizar y eliminar cargan la sesion y agregan Bearer.
- Una sesion ausente o vencida produce UNAUTHENTICATED antes de llamar a la API.
- Crear acepta tanto 201 como 200 (reintento identico); eliminar requiere 204.
- La contrasena ausente en UserUpdate se omite del JSON; no se envia null ni cadena vacia.
- El servicio documenta lista completa, sin parametros de paginacion.
- Se desactivan redirecciones y reintentos de conexion automaticos.
- Sin interceptores de logging, cache HTTP ni credenciales hardcodeadas.
- Timeouts: conexion 20 s, lectura 90 s, escritura 30 s, llamada total 100 s.
- Un 401 remoto se comunica a aplicacion; no se borra automaticamente la sesion
  para evitar eliminar una sesion nueva ante una respuesta concurrente antigua.
- No hay refresh remoto porque OpenAPI no expone una operacion de refresh.

Los errores HTTP se traducen a FailureReason. Se reconocen pistas de correo y
contrasena en errores de validacion, sin mostrar el mensaje remoto ni adjuntar
excepciones de JSON que puedan incluir secretos. Una descarga fallida es NETWORK;
un cuerpo completo con JSON o datos incompatibles es UNEXPECTED.

Se volvio a consultar OpenAPI el 26 de octubre de 2026. El codigo local del backend
en AuthRestController usa JwtTokenService.expirationSeconds(), lo que confirma la
unidad prevista de expiresIn. El reloj se captura al iniciar login: la latencia
de red reduce conservadoramente la vigencia local, en vez de prolongarla.

## Sesion local

- AES-256-GCM con IV aleatorio por escritura y etiqueta de autenticacion de 128 bits.
- Clave no exportable generada por Android Keystore; el token no se guarda en preferencias.
- Archivo versionado en applicationContext.noBackupFilesDir, fuera de copias y transferencias.
- AtomicFile y sincronizacion del store para evitar escrituras parciales.
- Se conserva solo token, tipo, duracion y fecha de referencia; nunca contrasena.
- Ausencia devuelve Optional.empty(); corrupcion, clave inaccesible y fallos de IO
  se comunican como SERVICE_UNAVAILABLE, sin ocultarlos como ausencia.
- ClearSessionAdapter borra solo el archivo de sesion. No modifica usuarios remotos.
- Los DTOs tienen reglas consumer de conservacion para serializacion en builds optimizados.

La configuracion de desugaring en `:adapter` y `:app` permite java.time con minSdk 24.
El manifiesto de la biblioteca aporta INTERNET y desactiva trafico HTTP sin cifrar.

## Inyeccion y siguientes capas

Se mantiene inyeccion por constructor, sin Lombok ni procesadores de DI.
El grafo actual cabe en una factoria pequena. Application y Entrypoint podran
consumir los mismos puertos si mas adelante se adopta Dagger/Hilt.

Los adaptadores son sincronos. Application/Entrypoint deberan coordinar ejecucion
fuera del hilo principal, ciclo de vida y presentacion de errores.
LoginAdapter devuelve la sesion; guardar la sesion y cerrar sesion son decisiones
de orquestacion para Application, no efectos ocultos del cliente HTTP.

## Verificacion

```powershell
.\gradlew.bat :domain:test :adapter:testDebugUnitTest :app:assembleDebug :adapter:lintDebug --console=plain
```

24 pruebas de Adapter: 14 HTTP con MockWebServer y 10 de cache/cifrado.
Cubren todas las operaciones, headers, codificacion de IDs, password opcional,
errores, JSON incompatible, redirecciones, fallos de red, expiracion, persistencia
entre instancias, IV aleatorio, manipulacion del cifrado y errores de almacenamiento.
Se conservan las 8 pruebas de Domain.

Las pruebas de cifrado usan AES-GCM real del JDK con una clave de prueba y un
almacenamiento en memoria. La integracion Android Keystore/AtomicFile se compila
y pasa lint, pero no se ha ejecutado en un dispositivo: no hay ninguno conectado.
No se realizaron escrituras en el servicio remoto.

Referencias:
- https://users-management-api-orb0.onrender.com/v3/api-docs
- https://developer.android.com/privacy-and-security/keystore
- https://developer.android.com/studio/write/java8-support
