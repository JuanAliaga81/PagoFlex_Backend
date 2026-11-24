PAGOFLEX - PRUEBAS UNITARIAS BACKEND
Referencia: PagoFlex_Backend(3).zip y los dos archivos nuevos
EjecutivoUsuariosService.java y EjecutivoUsuariosController.java del chat.

Contenido: 29 clases Java (157 metodos @Test), organizadas por microservicio.
Usuarios: 8 clases / 46 pruebas. Cobranza: 15 / 74. Soporte: 6 / 37.
Incluye pruebas de servicios, controladores y clientes. Los controladores se
prueban mediante invocacion directa, no como pruebas HTTP de extremo a extremo.
Mocks de repositorios y HTTP; almacenamiento de fotografias en un directorio temporal.
No incluye codigo de produccion ni archivos de configuracion con credenciales.

INSTALACION
1. Extraer el ZIP. Copiar las tres carpetas a la raiz de PagoFlex_Backend,
   fusionando carpetas (NO sustituir los proyectos completos).
2. Conservar los pom.xml con JaCoCo configurado en el paso 13.15.2.
3. Confirmar que los dos archivos nuevos del Ejecutivo estan en src/main/java.
4. Copiar Ejecutar-Pruebas.ps1 a la raiz de PagoFlex_Backend.
5. Desde esa raiz en PowerShell ejecutar: .\Ejecutar-Pruebas.ps1
   Alternativa si se ejecuta desde otra carpeta:
   .\Ejecutar-Pruebas.ps1 -Backend 'C:\ruta\PagoFlex_Backend'

El script ejecuta pruebas *Test.java, excluyendo los *Tests.java de contexto
predeterminados. JaCoCo mide todas las clases de produccion de cada servicio;
no se excluyen clases para aumentar artificialmente la cobertura.
La meta de 80% se evalua sobre LINE (lineas), y tambien se muestran BRANCH
(ramas) e INSTRUCTION (instrucciones), que son metricas distintas.
Los informes HTML se generan en cada servicio: target/site/jacoco/index.html.
Si hay fallos, revisar el log Maven y target/surefire-reports; no interpretar
un fallo de compilacion como una cobertura obtenida.

ESTADO
Pruebas preparadas a partir del codigo fuente adjunto y las clases del chat.
No ejecutadas con Maven ni verificadas contra sus dependencias en el entorno
que genero este ZIP. El 80% es una meta, NO un resultado medido. Ejecutar,
revisar y completar casos adicionales a partir del informe real de JaCoCo.
