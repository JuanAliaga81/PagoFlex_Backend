PAGOFLEX - DATOS DE DEMOSTRACION

1. Copiar las carpetas usuarios-service y cobranza-service dentro de PagoFlex_Backend.
   Son 4 archivos Java nuevos (2 DataLoader y 2 DataLoaderTest). No reemplazar los POM.
2. Iniciar Eureka y MySQL como siempre. Usar exclusivamente una BD de demostracion.
3. En una terminal PowerShell de usuarios-service:
   $env:SPRING_PROFILES_ACTIVE = "demo"
   $env:PAGOFLEX_DEMO_PASSWORD = Read-Host "Define una contrasena demo de 8 o mas caracteres"
   .\mvnw.cmd spring-boot:run
   (Si usas Maven global, mvn spring-boot:run.)
4. Una vez que Usuarios imprima DataLoader usuarios, iniciar Cobranza en otra terminal:
   $env:SPRING_PROFILES_ACTIVE = "demo"
   .\mvnw.cmd spring-boot:run
5. Iniciar Soporte y Gateway normalmente, sin perfil demo. No publicar el entorno demo en Internet.
   Si se usan tuneles, restringir el acceso al entorno de demostracion.
6. Probar login usando los correos listados abajo y la contrasena configurada en el paso 3.
   La contrasena no se guarda en el ZIP. Si ya existen cuentas, se conserva su contrasena actual.
   El RUT de prueba es el identificador estable, aunque se edite el nombre o el correo.
   No usar datos reales: RUT sintetico, correo con TLD reservado .test.
7. Reiniciar en el mismo orden. No se reinician estados, fechas, pagos, roles ni contrasenas existentes.
   Si un RUT coincide con una cuenta de otro rol o inactiva, se detiene la carga. Si hay una asignacion
   a una empresa distinta, se detiene en vez de modificarla. Resolver colisiones manualmente.
   Cada microservicio carga sus datos en una transaccion local. No hay transaccion distribuida.
8. Para desactivar los DataLoader, iniciar servicios sin el perfil demo (los registros ya creados permanecen).
   Desde la terminal de arranque: Remove-Item Env:SPRING_PROFILES_ACTIVE -ErrorAction SilentlyContinue
   Remove-Item Env:PAGOFLEX_DEMO_PASSWORD -ErrorAction SilentlyContinue
9. Ejecutar nuevamente .\Ejecutar-Pruebas.ps1 desde PagoFlex_Backend con los servicios detenidos.
   Las cinco pruebas nuevas usan mocks, no MySQL. Medir cobertura real; no se garantiza el 80% sin ejecutar.
   Se incluyen dos tests, uno por DataLoader. Las clases de produccion nuevas no se excluyen de JaCoCo.

CUENTAS FICTICIAS (RUT sin puntos ni guion; rol interno):
usuario1@pagoflex.example.test | 900000014 | USUARIO_FINAL | Usuario Demo 1
usuario2@pagoflex.example.test | 900000022 | USUARIO_FINAL | Usuario Demo 2
usuario3@pagoflex.example.test | 900000030 | USUARIO_FINAL | Usuario Demo 3
usuario4@pagoflex.example.test | 900000049 | USUARIO_FINAL | Usuario Demo 4
usuario5@pagoflex.example.test | 900000057 | USUARIO_FINAL | Usuario Demo 5
usuario6@pagoflex.example.test | 900000065 | USUARIO_FINAL | Usuario Demo 6
ejecutivo1@pagoflex.example.test | 900000073 | ADMINISTRADOR | Ejecutivo Demo 1
ejecutivo2@pagoflex.example.test | 900000081 | ADMINISTRADOR | Ejecutivo Demo 2
ejecutivo3@pagoflex.example.test | 90000009K | ADMINISTRADOR | Ejecutivo Demo 3
agente1@pagoflex.example.test | 900000103 | AGENTE | Agente Demo 1
agente2@pagoflex.example.test | 900000111 | AGENTE | Agente Demo 2
agente3@pagoflex.example.test | 90000012K | AGENTE | Agente Demo 3

ASOCIACIONES
Ejecutivo 1 - Conecta Telecom - usuarios 1 y 2
Ejecutivo 2 - ElectroSur - usuarios 3 y 4
Ejecutivo 3 - AquaNorte - usuarios 5 y 6
Cada empresa tiene un tercer usuario (4, 6, 2 respectivamente) con una deuda vencida.
Agentes globales, sin relacion empresa en el modelo actual.
12 compromisos: 6 pendientes, 3 vencidos, 3 pagados al cargar por primera vez.
Cada pago se genera por PagoService dentro de la transaccion de carga.
Reportes se crean desde Android para probar el flujo real.
No se modifica el backend existente ni se agregan tablas.
Compilacion no ejecutada: el entorno de generacion no dispone de Maven/dependencias.
