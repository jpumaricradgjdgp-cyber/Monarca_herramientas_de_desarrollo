[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$Participante
)

$ErrorActionPreference = 'Stop'
if (-not (Get-Command git -ErrorAction SilentlyContinue)) { throw 'Instala Git antes de ejecutar esta práctica.' }
$carpetaEjercicio = Join-Path ([System.IO.Path]::GetTempPath()) ('Monarca-conflicto-' + [guid]::NewGuid().ToString('N'))
$laboratorio = Join-Path $carpetaEjercicio 'repositorio'
$evidencias = Join-Path $carpetaEjercicio 'evidencias'
New-Item -ItemType Directory -Path $laboratorio -Force | Out-Null
New-Item -ItemType Directory -Path $evidencias -Force | Out-Null
$utf8 = New-Object System.Text.UTF8Encoding($false)
$mensajePath = Join-Path $laboratorio 'mensaje.txt'

function Invoke-GitLab {
    # Windows PowerShell 5.1 convierte avisos de stderr en ErrorRecord.
    # Evaluar el codigo de Git, sin confundir un aviso con un fallo.
    $preferenciaAnterior = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $salida = & git -C $laboratorio @args 2>&1
        $codigo = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $preferenciaAnterior
    }
    Write-Host ('git ' + ($args -join ' '))
    $salida | ForEach-Object { Write-Host $_ }
    if ($codigo -ne 0) { throw ('Git terminó con código ' + $codigo) }
}

Start-Transcript -LiteralPath (Join-Path $evidencias 'transcripcion.txt') | Out-Null
try {
    Write-Host 'EJERCICIO CONTROLADO. Repositorio temporal separado del proyecto Monarca.'
    Write-Host ('Participante declarado: ' + $Participante)
    Invoke-GitLab init -b main
    Invoke-GitLab config user.name $Participante
    Invoke-GitLab config user.email 'practica-local@example.test'
    Invoke-GitLab config commit.gpgsign false
    Invoke-GitLab config core.autocrlf false
    [System.IO.File]::WriteAllText($mensajePath, "Hola Mundo`n", $utf8)
    Invoke-GitLab add mensaje.txt
    Invoke-GitLab commit -m 'Laboratorio: versión inicial'
    Invoke-GitLab switch -c feature/saludo
    [System.IO.File]::WriteAllText($mensajePath, "Hola desde Feature`n", $utf8)
    Invoke-GitLab add mensaje.txt
    Invoke-GitLab commit -m 'Laboratorio: saludo de feature'
    Invoke-GitLab switch main
    [System.IO.File]::WriteAllText($mensajePath, "Hola desde Main`n", $utf8)
    Invoke-GitLab add mensaje.txt
    Invoke-GitLab commit -m 'Laboratorio: saludo de main'

    Write-Host 'git merge feature/saludo (se espera un conflicto)'
    $salidaMerge = & git -C $laboratorio merge feature/saludo 2>&1
    $codigoMerge = $LASTEXITCODE
    $salidaMerge | ForEach-Object { Write-Host $_ }
    if ($codigoMerge -ne 1) { throw 'No se obtuvo el conflicto esperado.' }
    $sinResolver = @(& git -C $laboratorio diff --name-only --diff-filter=U)
    if ($LASTEXITCODE -ne 0 -or $sinResolver.Count -ne 1 -or $sinResolver[0] -ne 'mensaje.txt') {
        throw 'El archivo sin resolver no coincide con el ejercicio.'
    }
    $contenidoConflicto = Get-Content -LiteralPath $mensajePath -Raw
    if ($contenidoConflicto -notmatch '<<<<<<<' -or $contenidoConflicto -notmatch '=======') {
        throw 'No se encontraron los marcadores esperados.'
    }
    Copy-Item -LiteralPath $mensajePath -Destination (Join-Path $evidencias 'conflicto.txt')
    Write-Host $contenidoConflicto
    Write-Host 'Resolución didáctica: conservar ambos saludos y eliminar los marcadores.'
    [System.IO.File]::WriteAllText($mensajePath, "Hola desde Main y Feature`n", $utf8)
    Invoke-GitLab add mensaje.txt
    Invoke-GitLab commit -m 'Laboratorio: integra ambos saludos y resuelve el conflicto'
    $estado = @(& git -C $laboratorio status --porcelain)
    if ($LASTEXITCODE -ne 0 -or $estado.Count -ne 0) { throw 'El laboratorio no terminó limpio.' }
    $commitResolucion = & git -C $laboratorio rev-parse HEAD
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo leer el commit final.' }
    $historial = & git -C $laboratorio log --graph --oneline --all
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo leer el historial.' }
    [System.IO.File]::WriteAllLines((Join-Path $evidencias 'historial.txt'), [string[]]$historial, $utf8)
    Copy-Item -LiteralPath $mensajePath -Destination (Join-Path $evidencias 'resolucion.txt')
    $resultado = [pscustomobject]@{
        tipo = 'Ejercicio controlado, separado de Monarca'
        fechaUtc = [DateTime]::UtcNow.ToString('o')
        participanteDeclarado = $Participante
        correoDeLaboratorio = 'practica-local@example.test'
        resolucionAutomatica = $true
        archivo = 'mensaje.txt'
        causa = 'Cambios incompatibles en la misma línea desde main y feature/saludo'
        resolucion = 'Conservar ambos saludos'
        commitResolucion = $commitResolucion
        repositorioLimpio = $true
    }
    [System.IO.File]::WriteAllText((Join-Path $evidencias 'resultado.json'), ($resultado | ConvertTo-Json), $utf8)
    Write-Host ($historial -join [Environment]::NewLine)
    Write-Host ('Práctica completada. Evidencias: ' + $evidencias)
    Write-Host 'Revisa los archivos y responde las preguntas del manual antes de presentarlos.'
} finally {
    Stop-Transcript | Out-Null
}
