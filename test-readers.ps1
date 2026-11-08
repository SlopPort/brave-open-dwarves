param([string]$Archive='Gnomes.dat', [string]$Jdk='C:\Program Files\Eclipse Adoptium\jdk-8.0.462.8-hotspot', [switch]$ComparePillow)
$ErrorActionPreference='Stop'
Set-Location $PSScriptRoot
New-Item -ItemType Directory -Force build\reader-tests,analysis | Out-Null
& "$Jdk\bin\javac.exe" -encoding UTF-8 -d build\reader-tests android\src\org\braveopendwarves\data\*.java tests\ReaderTest.java tests\CombatTest.java tests\RunStateTest.java tests\AnimationTest.java tests\LiftTest.java tests\DoorTest.java tests\ItemTest.java tests\EnvironmentTest.java tests\HeroFramesTest.java tests\BossTest.java tests\ProjectileTest.java
if($LASTEXITCODE -ne 0){throw 'Reader compilation failed'}
& "$Jdk\bin\java.exe" -cp build\reader-tests ReaderTest $Archive | Out-File analysis\reader-tests.txt -Encoding utf8
if($LASTEXITCODE -ne 0){throw 'Reader tests failed'}
Get-Content analysis\reader-tests.txt -Tail 1
& "$Jdk\bin\java.exe" -cp build\reader-tests CombatTest
if($LASTEXITCODE -ne 0){throw 'Combat tests failed'}
& "$Jdk\bin\java.exe" -cp build\reader-tests RunStateTest
if($LASTEXITCODE -ne 0){throw 'Checkpoint tests failed'}
& "$Jdk\bin\java.exe" -cp build\reader-tests AnimationTest
if($LASTEXITCODE -ne 0){throw 'Animation tests failed'}
& "$Jdk\bin\java.exe" -cp build\reader-tests LiftTest
if($LASTEXITCODE -ne 0){throw 'Lift tests failed'}
& "$Jdk\bin\java.exe" -cp build\reader-tests DoorTest
if($LASTEXITCODE -ne 0){throw 'Door tests failed'}
& "$Jdk\bin\java.exe" -cp build\reader-tests ItemTest
if($LASTEXITCODE -ne 0){throw 'Item tests failed'}
& "$Jdk\bin\java.exe" -cp build\reader-tests EnvironmentTest
if($LASTEXITCODE -ne 0){throw 'Environment tests failed'}
& "$Jdk\bin\java.exe" -cp build\reader-tests HeroFramesTest
if($LASTEXITCODE -ne 0){throw 'Hero frame tests failed'}
& "$Jdk\bin\java.exe" -cp build\reader-tests BossTest
if($LASTEXITCODE -ne 0){throw 'Boss tests failed'}
& "$Jdk\bin\java.exe" -cp build\reader-tests ProjectileTest
if($LASTEXITCODE -ne 0){throw 'Projectile tests failed'}
if($ComparePillow){
    python tests\compare_pcx.py $Archive analysis\reader-tests.txt
    if($LASTEXITCODE -ne 0){throw 'Pillow comparison failed'}
}





