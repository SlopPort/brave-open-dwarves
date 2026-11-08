param([string]$Objdump = 'C:\msys64\ucrt64\bin\objdump.exe')
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$reaNode = Join-Path $PSScriptRoot 'tools\rea\node_modules\node\bin\node.exe'
$reaCli = Join-Path $PSScriptRoot 'tools\rea\node_modules\rea-agents\scripts\rea.mjs'
if (!(Test-Path $reaNode) -or !(Test-Path $reaCli)) {
    throw 'Najpierw: npm.cmd install --prefix tools/rea --no-audit --no-fund rea-agents@6.7.0 node@24.22.1'
}
$env:REA_OBJDUMP_COMMAND = $Objdump
New-Item -ItemType Directory -Force analysis | Out-Null
foreach ($operation in @('file_headers','section_headers','disassemble')) {
    & $reaNode $reaCli inspect-with-objdump (Join-Path $PSScriptRoot 'Gnomes.exe') $operation --format json | Out-File "analysis\gnomes-$operation.rea.json" -Encoding utf8
    if ($LASTEXITCODE -ne 0) { throw "REA failed: $operation" }
}
& $reaNode $reaCli inspect-pe-resources (Join-Path $PSScriptRoot 'Gnomes.exe') --format json | Out-File analysis\gnomes-resources.rea.json -Encoding utf8
if ($LASTEXITCODE -ne 0) { throw 'REA failed: resources' }
