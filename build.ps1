param([string]$Sdk = "$env:LOCALAPPDATA\Android\Sdk", [string]$Jdk = 'C:\Program Files\Android\Android Studio\jbr', [string]$Version = '0.33')
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$env:JAVA_HOME = $Jdk
$env:PATH = "$Jdk\bin;$env:PATH"
$bt = Join-Path $Sdk 'build-tools\35.0.0'
$platform = Join-Path $Sdk 'platforms\android-35\android.jar'
function Check { if ($LASTEXITCODE -ne 0) { throw "Build failed: $LASTEXITCODE" } }
New-Item -ItemType Directory -Force build\classes,build\dex,dist | Out-Null
& "$bt\aapt.exe" package -f -M android\AndroidManifest.xml -I $platform -F build\base.apk
Check
$sources = Get-ChildItem android\src -Recurse -Filter *.java | ForEach-Object FullName
& "$Jdk\bin\javac.exe" -encoding UTF-8 -source 8 -target 8 -bootclasspath "$platform;$bt\core-lambda-stubs.jar" -d build\classes $sources
Check
& "$Jdk\bin\jar.exe" cf build\classes.jar -C build\classes .
Check
& "$bt\d8.bat" --lib $platform --min-api 21 --output build\dex build\classes.jar
Check
Copy-Item build\base.apk build\unsigned.apk -Force
$apk = Join-Path $PSScriptRoot 'build\unsigned.apk'
Push-Location build\dex
try { & "$Jdk\bin\jar.exe" uf $apk classes.dex; Check } finally { Pop-Location }
& "$bt\zipalign.exe" -f 4 build\unsigned.apk build\aligned.apk
Check
if (!(Test-Path build\debug.keystore)) {
    & "$Jdk\bin\keytool.exe" -genkeypair -keystore build\debug.keystore -storepass android -keypass android -alias androiddebugkey -dname 'CN=Brave Open Dwarves Development' -keyalg RSA -keysize 2048 -validity 10000
    Check
}
& "$bt\apksigner.bat" sign --ks build\debug.keystore --ks-pass pass:android --key-pass pass:android --out "dist\Brave-Open-Dwarves-$Version.apk" build\aligned.apk
Check
& "$bt\apksigner.bat" verify --verbose "dist\Brave-Open-Dwarves-$Version.apk"
Check
Get-FileHash "dist\Brave-Open-Dwarves-$Version.apk" -Algorithm SHA256 | Format-List



















