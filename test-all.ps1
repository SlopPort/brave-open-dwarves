param([string]$Archive='Gnomes.dat', [string]$Jdk)
$ErrorActionPreference='Stop'
Set-Location $PSScriptRoot
if(!$Jdk){
    $Jdk=@(Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory -Filter 'jdk-8*' -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty FullName)
    if(!$Jdk){$Jdk='C:\Program Files\Android\Android Studio\jbr'}
}
& "$PSScriptRoot\test-readers.ps1" -Archive $Archive -Jdk $Jdk
Write-Host 'ALL DESKTOP TESTS PASSED'
