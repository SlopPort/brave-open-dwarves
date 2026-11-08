# Testy Android 0.1 — 25.10.2026

Urządzenie użytkownika: Huawei P9 Lite/VNS-L21, serial $DEVICE, Android 10/API 29, LineageOS. Redmi Note 8 był również podłączony, ale nie instalowano na nim gry. Identyfikacja przez REA: `huawei.rea.json`.

## Sprawdzone

- Kompilacja Java → DEX → APK, wyrównanie ZIP i weryfikacja podpisów v1/v2/v3.
- Instalacja końcowego APK przez ADB `--no-streaming`: Success.
- Zimny start końcowego APK: Status ok, TotalTime 528 ms (pojedynczy pomiar, nie benchmark).
- Nazwa pakietu `org.braveopendwarves`, versionCode 1, versionName 0.1.0, minSdk 21, targetSdk 28 potwierdzone przez urządzenie.
- Menu, rozpoczęcie nowej gry, ruch dotykiem, zbieranie zwykłych klejnotów, skok przyciskiem SKOK, lądowanie na platformie i zebranie zielonego klucza (wynik 110 i zapalony zielony klucz: `huawei-key.png`).
- Pauza i wznowienie; wywołanie zmiany postaci i ataku; przejście do ekranu głównego Androida i powrót do aplikacji.
- Brak wpisów błędu AndroidRuntime w pobranym logu podczas sprawdzonych sesji. Nie jest to gwarancja braku wszystkich błędów.
- Pakiet nie zawiera oryginalnych danych gry ani żądanych uprawnień.

## Ograniczenia testów

Nie wykonano pełnego przejścia poziomu, długiej sesji, pomiarów FPS/zużycia baterii, niezależnej weryfikacji wielodotyku, klawiatury/pada, pełnego zapisu i wszystkich sytuacji walki. Nie porównano fizyki z oryginalną grą. Zapis jest zapisem numeru jaskini, nie pełnego stanu rozgrywki.

Pierwsza instalacja strumieniowa przez starszy klient ADB została odrzucona komunikatem o nieprawidłowym ZIP. Ten sam APK zainstalował się poprawnie po przesłaniu pliku (`--no-streaming`).

Zrzuty z wcześniejszych iteracji pozostawiono do porównania; końcowy test klucza to `huawei-key.png`.
