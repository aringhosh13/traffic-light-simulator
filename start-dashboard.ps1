param([int]$Port = 8080)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
        $compiler = Join-Path $env:JAVA_HOME 'bin\javac.exe'
        $runtime = Join-Path $env:JAVA_HOME 'bin\java.exe'
    } else {
        $compiler = (Get-Command javac -ErrorAction Stop).Source
        $runtime = (Get-Command java -ErrorAction Stop).Source
    }
    if ($compiler -like '*\Oracle\Java\javapath\*') {
        # Prefer the installed JDK to the Oracle launcher shim on Windows.
        $installedJdk = Get-ChildItem (Join-Path $env:ProgramFiles 'Java') -Directory -Filter 'jdk*' -ErrorAction SilentlyContinue |
            Where-Object { Test-Path (Join-Path $_.FullName 'bin\javac.exe') } |
            Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if ($installedJdk) {
            $compiler = Join-Path $installedJdk.FullName 'bin\javac.exe'
            $runtime = Join-Path $installedJdk.FullName 'bin\java.exe'
        }
    }
    New-Item -ItemType Directory -Path bin -Force | Out-Null
    & $compiler -d bin src/*.java
    if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed. A JDK 17 or newer is required.' }
    & $runtime -cp bin DashboardServer $Port
    if ($LASTEXITCODE -ne 0) { throw 'The dashboard server could not start. See the Java error above.' }
} finally {
    Pop-Location
}
