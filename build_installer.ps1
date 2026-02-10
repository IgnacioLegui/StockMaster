# Build StockMaster Installer

$APP_NAME = "StockMaster"
$APP_VERSION = "1.0.0"
$MAIN_JAR = "StockMaster-1.0-SNAPSHOT.jar"
$MAIN_CLASS = "com.stockmaster.Launcher"
$INPUT_DIR = "target/libs"
$ICON_PATH = "src/main/resources/images/icon.ico"

# 1. Clean and Build with Maven
Write-Host "Intentando construir con Maven..." -ForegroundColor Cyan
try {
    cmd /c mvn clean package
    if ($LASTEXITCODE -ne 0) { throw "Build failed" }
} catch {
    Write-Warning "No se pudo ejecutar 'mvn'. Verificando si el proyecto ya fue construido..."
    
    # Check if artifacts exist
    if ((Test-Path "target/libs") -and (Test-Path "target/$MAIN_JAR")) {
        Write-Host "Archivos de construcción encontrados. Continuando con el empaquetado..." -ForegroundColor Green
    } else {
        Write-Error "Maven no encontrado y no hay archivos de construcción."
        Write-Error "POR FAVOR: Ejecuta 'clean package' o 'install' desde tu IDE (IntelliJ/Eclipse) primero,"
        Write-Error "y luego vuelve a ejecutar este script."
        exit 1
    }
}

# 2. Check for Icon
if (-not (Test-Path $ICON_PATH)) {
    Write-Warning "No se encontró el icono en $ICON_PATH. Se usará el icono predeterminado."
    $ICON_ARG = ""
} else {
    $ICON_ARG = "--icon `"$ICON_PATH`""
}

# 3. Request logic to find jpackage
$JPACKAGE_EXE = "jpackage"
if (-not (Get-Command "jpackage" -ErrorAction SilentlyContinue)) {
    Write-Warning "'jpackage' no está en el PATH global."
    
    # Try JAVA_HOME
    if ($env:JAVA_HOME) {
        $candidate = Join-Path $env:JAVA_HOME "bin\jpackage.exe"
        if (Test-Path $candidate) {
            $JPACKAGE_EXE = $candidate
            Write-Host "Usando jpackage de JAVA_HOME: $JPACKAGE_EXE" -ForegroundColor Green
        }
    }
}

# If still not found, try common paths
if ($JPACKAGE_EXE -eq "jpackage" -and -not (Get-Command "jpackage" -ErrorAction SilentlyContinue)) {
    $commonPaths = @(
        "C:\Program Files\Java\jdk*\bin\jpackage.exe",
        "C:\Program Files (x86)\Java\jdk*\bin\jpackage.exe",
        "$env:LOCALAPPDATA\Programs\Common\Microsoft\OpenJDK\*\bin\jpackage.exe"
    )
    
    foreach ($path in $commonPaths) {
        $found = Get-Item $path -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) {
            $JPACKAGE_EXE = $found.FullName
            Write-Host "jpackage encontrado en: $JPACKAGE_EXE" -ForegroundColor Green
            break
        }
    }
}

if ($JPACKAGE_EXE -eq "jpackage" -and -not (Get-Command "jpackage" -ErrorAction SilentlyContinue)) {
    Write-Error "CRÍTICO: No se pudo encontrar 'jpackage.exe'."
    Write-Error "Asegúrate de tener instalado Java Development Kit (JDK) 14 o superior."
    Write-Error "Si ya lo tienes, configura la variable de entorno JAVA_HOME o agrega el bin del JDK al PATH."
    exit 1
}

# 4. Run jpackage
Write-Host "Generando instalador con jpackage..." -ForegroundColor Cyan

# Create output directory if not exists
if (-not (Test-Path "installer")) {
    New-Item -ItemType Directory -Force -Path "installer"
}

Copy-Item "target/$MAIN_JAR" -Destination "target/libs"

$jpackageArgs = @(
  "--type", "exe",
  "--dest", "installer",
  "--input", "target/libs",
  "--name", $APP_NAME,
  "--main-jar", $MAIN_JAR,
  "--main-class", $MAIN_CLASS,
  "--win-shortcut",
  "--win-menu",
  "--win-dir-chooser",
  "--app-version", $APP_VERSION
)

if (-not [string]::IsNullOrEmpty($ICON_ARG)) {
    # Extract path cleanly if quotes exist
    $cleanIconPath = $ICON_ARG -replace '--icon ', '' -replace '"', ''
    $jpackageArgs += "--icon"
    $jpackageArgs += $cleanIconPath
}

Write-Host "Ejecutando jpackage con argumentos..." -ForegroundColor Cyan
& $JPACKAGE_EXE @jpackageArgs

if ($LASTEXITCODE -eq 0) {
    Write-Host "¡Instalador creado exitosamente en la carpeta 'installer'!" -ForegroundColor Green
} else {
    Write-Error "Error al crear el instalador."
}
