$ErrorActionPreference = 'Stop'

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw 'Khong tim thay Maven (mvn) trong PATH.'
}

$originalPassword = [Environment]::GetEnvironmentVariable('DB_PASSWORD', 'Process')
$originalJwtSecret = [Environment]::GetEnvironmentVariable('JWT_SECRET', 'Process')

try {
    if (-not $originalPassword) {
        $securePassword = Read-Host 'MySQL password (DB_PASSWORD)' -AsSecureString
        $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        try {
            $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
        } finally {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
            $securePassword.Dispose()
        }
    }

    if (-not $originalJwtSecret) {
        $randomBytes = New-Object byte[] 48
        $randomGenerator = [Security.Cryptography.RandomNumberGenerator]::Create()
        try {
            $randomGenerator.GetBytes($randomBytes)
            $env:JWT_SECRET = [Convert]::ToBase64String($randomBytes)
        } finally {
            $randomGenerator.Dispose()
        }
    }

    $backendPath = Join-Path $PSScriptRoot '..\backend'
    Push-Location $backendPath
    try {
        & mvn spring-boot:run
        if ($LASTEXITCODE -ne 0) {
            throw "Backend khong khoi dong duoc (Maven exit code $LASTEXITCODE)."
        }
    } finally {
        Pop-Location
    }
} finally {
    [Environment]::SetEnvironmentVariable('DB_PASSWORD', $originalPassword, 'Process')
    [Environment]::SetEnvironmentVariable('JWT_SECRET', $originalJwtSecret, 'Process')
}
