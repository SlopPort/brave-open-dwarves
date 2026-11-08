param([string]$Sdk="$env:LOCALAPPDATA\Android\Sdk",[string]$Jdk='C:\Program Files\Android\Android Studio\jbr')
$ErrorActionPreference='Stop'
Set-Location $PSScriptRoot
$env:JAVA_HOME=$Jdk
$env:PATH="$Jdk\bin;$env:PATH"
$bt=Join-Path $Sdk 'build-tools\35.0.0'
$platform=Join-Path $Sdk 'platforms\android-35\android.jar'
function Check {if($LASTEXITCODE -ne 0){throw "Test APK build failed: $LASTEXITCODE"}}
New-Item -ItemType Directory -Force build\device-test-classes,build\device-test-dex | Out-Null
& "$Jdk\bin\javac.exe" -encoding UTF-8 -source 8 -target 8 -bootclasspath "$platform;$bt\core-lambda-stubs.jar" -d build\device-test-classes tests\device\org\braveopendwarves\DeviceTestRunner.java
Check
& "$Jdk\bin\jar.exe" cf build\device-tests.jar -C build\device-test-classes .
Check
& "$bt\d8.bat" --lib $platform --min-api 21 --output build\device-test-dex build\device-tests.jar
Check
& "$bt\aapt.exe" package -f -M tests\device\AndroidManifest.xml -I $platform -F build\device-tests-unsigned.apk
Check
$testApk=Join-Path $PSScriptRoot 'build\device-tests-unsigned.apk'
Push-Location build\device-test-dex
try{& "$Jdk\bin\jar.exe" uf $testApk classes.dex;Check}finally{Pop-Location}
& "$bt\zipalign.exe" -f 4 build\device-tests-unsigned.apk build\device-tests-aligned.apk
Check
& "$bt\apksigner.bat" sign --ks build\debug.keystore --ks-pass pass:android --key-pass pass:android --out build\Brave-Open-Dwarves-tests.apk build\device-tests-aligned.apk
Check
