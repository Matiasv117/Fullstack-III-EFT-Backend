# Loads variables from config/local-insforge.env into the current PowerShell session.
# Usage (from repo backend root):
#   . .\scripts\load-insforge-env.ps1
# Then: cd ms-gestionpacientes; .\mvnw.cmd spring-boot:run
#
param(
    [switch]$SilentIfMissing
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$envFile = Join-Path $root 'config\local-insforge.env'

if (-not (Test-Path $envFile)) {
    if ($SilentIfMissing) {
        Write-Host '[Insforge] Missing config\local-insforge.env - microservices will use default H2 (Insforge stays empty).' -ForegroundColor DarkYellow
        return
    }
    Write-Host "Missing file: $envFile" -ForegroundColor Red
    Write-Host 'Copy config\local-insforge.env.example to config\local-insforge.env and set DB_PASSWORD.' -ForegroundColor Yellow
    exit 1
}

Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq '' -or $line.StartsWith('#')) { return }
    if ($line -match '^([^#=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $val = $matches[2].Trim()
        Set-Item -Path "Env:$name" -Value $val
    }
}

Write-Host 'Insforge env vars loaded (SPRING_PROFILES_ACTIVE, DB_URL, DB_USERNAME, DB_PASSWORD).' -ForegroundColor Green
