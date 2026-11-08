# Testy Android 0.2 — 25.10.2026

Czytniki: wszystkie 244 wpisy archiwum rozpakowane z kontrolą długości i końca
strumienia; razem 10282171 bajtów. Wszystkie 114 obrazów PCX zgodne piksel po
pikselu z niezależnym Pillow (surowe RGBA). Wszystkie 55 map przeszło odczyt
podstawowych sekcji. Dodatkowe sekcje 37 map zachowano jako trailer. Osobno
sprawdzono przezroczystość narożnika arkuszy Items i Gnome1.

Testy odrzucają błędną sygnaturę, wersję, licznik, offset, rozmiar, urwany katalog,
ścieżkę z `..`, powtórzoną nazwę, zły separator końcowy i uszkodzony zlib.
Sprawdzają też nieobsługiwany/urwany PCX i nieprawidłowe nagłówki poziomu.
Wynik: 18 odrzuconych przypadków, wszystkie pozytywne próby przeszły.

Na Huawei P9 Lite / VNS-L21, Android 10 LineageOS sprawdzono:

- Instalację APK 0.2 przez `--no-streaming` i aktualizację z zachowaniem importu.
- Import Gnomes.dat przez systemowy wybór pliku: widoczny tytuł i 244 zasoby
  (`import-success.png`).
- Ponowne uruchomienie z zaimportowanym plikiem.
- Wyświetlenie pierwszej mapy, postaci, dekoracji i przedmiotów z poprawioną
  przezroczystością (`huawei-original-final.png`).
- Dotykowy ruch, zejście po drabinie przez podłoże, przewijanie pionowe i pauzę
  (`huawei-original-ladder.png`).
- Podgląd całej mapy i przełączanie poziomów do drugiego motywu graficznego
  (`huawei-original-theme2.png`).
- Brak błędów AndroidRuntime w logu filtrowanym dla działającego procesu gry.
  W logu ogólnym wystąpił oddzielny błąd narzędzia systemowego uiautomator;
  nie był to proces gry.
- Weryfikację podpisów APK v1/v2/v3; brak oryginalnych danych i uprawnień
  dostępu do całej pamięci lub internetu w pakiecie.

Nie sprawdzono pełnego przejścia poziomu, jednoczesnego wielodotyku na sprzęcie,
długiej sesji ani wydajności wszystkich 55 map. Efekty WAV są ładowane do
SoundPool; nie wykonano odsłuchu potwierdzającego ich działanie. Nie potwierdzono
zgodności fizyki z oryginałem.
