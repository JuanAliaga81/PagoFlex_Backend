param([string]$Backend = $PSScriptRoot)
$ErrorActionPreference = 'Stop'
$servicios = @('usuarios-service','cobranza-service','soporte-service')
$resultados = @()
foreach ($servicio in $servicios) {
    $ruta = Join-Path $Backend $servicio
    $wrapper = Join-Path $ruta 'mvnw.cmd'
    $pom = Join-Path $ruta 'pom.xml'
    if (!(Test-Path $wrapper) -or !(Test-Path $pom)) { throw "No se encuentra el proyecto Maven: $ruta" }
    if (!(Select-String -Path $pom -Pattern 'jacoco-maven-plugin' -Quiet)) { throw "Configura JaCoCo primero en $pom" }
    # Solo las clases nuevas terminadas en Test.java, no los contextLoads generados (Tests.java).
    $archivos = Get-ChildItem -Path (Join-Path $ruta 'src/test/java') -Recurse -Filter '*Test.java'
    $nombres = @($archivos | ForEach-Object { $_.BaseName } | Sort-Object -Unique)
    if ($nombres.Count -eq 0) { throw "No hay pruebas en $ruta" }
    Write-Host "`n=== $servicio ===" -ForegroundColor Cyan
    Push-Location $ruta
    try {
        $seleccion = '-Dtest=' + ($nombres -join ',')
        & .\mvnw.cmd clean test $seleccion
        if ($LASTEXITCODE -ne 0) {
            $resultados += [pscustomobject]@{ Servicio=$servicio; Lineas='N/D'; Ramas='N/D'; Instrucciones='N/D'; Estado='FALLO DE PRUEBAS O COMPILACION' }
            continue
        }
        $informe = Join-Path $ruta 'target/site/jacoco/jacoco.xml'
        if (!(Test-Path $informe)) {
            & .\mvnw.cmd jacoco:report
            if ($LASTEXITCODE -ne 0) { throw "No se pudo generar el informe de $servicio" }
        }
        if (!(Test-Path $informe)) { throw "No se genero $informe" }
        [xml]$xml = Get-Content $informe -Raw
        function Obtener-Cobertura([string]$tipo) {
            $contador = $xml.report.counter | Where-Object { $_.type -eq $tipo }
            if (!$contador) { return $null }
            $cubierto = [double]$contador.covered
            $total = $cubierto + [double]$contador.missed
            if ($total -eq 0) { return $null }
            return ($cubierto / $total * 100)
        }
        $lineas = Obtener-Cobertura 'LINE'
        $ramas = Obtener-Cobertura 'BRANCH'
        $instrucciones = Obtener-Cobertura 'INSTRUCTION'
        $estado = if ($null -ne $lineas -and $lineas -ge 80) { 'META DE LINEAS CUMPLIDA' } else { 'POR DEBAJO DEL 80% DE LINEAS' }
        $resultados += [pscustomobject]@{Servicio=$servicio; Lineas=('{0:N2}%' -f $lineas); Ramas=('{0:N2}%' -f $ramas); Instrucciones=('{0:N2}%' -f $instrucciones); Estado=$estado}
        Write-Host "HTML: $ruta\target\site\jacoco\index.html"
    } finally { Pop-Location }
}
Write-Host "`n=== COBERTURA REAL JACOCO ===" -ForegroundColor Cyan
$resultados | Format-Table -AutoSize
if (@($resultados | Where-Object { $_.Estado -ne 'META DE LINEAS CUMPLIDA' }).Count -gt 0) { exit 1 }
