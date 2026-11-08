# Badanie Brave Dwarves — 25 października 2026

## Co znaleziono

1. **morluto/rea** — https://github.com/morluto/rea
   Narzędzia do analizy programów i binariów. Nie jest silnikiem gry ani automatycznym konwerterem Windows → Android. W tym projekcie użyto CLI 6.7.0, parsera zasobów PE i GNU objdump za pośrednictwem REA. Nie zmieniono konfiguracji MCP użytkownika. Ghidra/Hopper/IDA nie zostały skonfigurowane do dekompilacji.
2. **ermaccer/BraveDwarves2.DATTool** — https://github.com/ermaccer/BraveDwarves2.DATTool
   Publiczne narzędzie do odczytu/zapisu archiwów z kompresją zlib z drugiej części. To użyteczny punkt odniesienia dla formatu zasobów; nie jest portem ani odtworzeniem silnika. Zgodność z Gnomes.dat pierwszej części nie została sprawdzona. Kodu nie włączono do aplikacji.
3. **ZenHAX — brave dwarves creeping shadows DAT** — https://www.zenhax.com/viewtopic.php%40t%3D1616.html
   Dyskusja o archiwach innej części serii i skrypcie QuickBMS `gaveovergames.bms`, z uwagami o różnicach LZW/zlib. Nie dowodzi zgodności tego skryptu z dostarczoną pierwszą częścią.
4. **Adventure Dwarves** — https://forums.androidcentral.com/threads/game-free-adventure-dwarves.1042797/
   Autor przedstawia własną grę Android, a w komentarzu potwierdza podobieństwo do Brave Dwarves. Nie jest to potwierdzenie portu oryginalnego silnika. Nie sprawdzono obecnej dostępności APK ani źródeł.
5. **Historyczne wydanie mobilne** — https://toucharcade.com/games/brave-dwarves
   Katalog opisuje mobilne Brave Dwarves z datą 19 maja 2009 i sterowaniem dotykowym/akcelerometrem. Nie potwierdza istnienia publicznego portu Android ani otwartych źródeł.

## Reddit i wynik wyszukiwania

Sprawdzono kombinacje `Brave Dwarves` z `android`, `port`, `remake`, `engine`, `reverse engineering`, `open source`, a także GitHub z wariantami `BraveDwarves` i `Gnomes.dat`.

Nie znaleziono potwierdzonego publicznego repozytorium z portem Android ani otwartym silnikiem pierwszej części. To wynik tych wyszukiwań, nie dowód, że taki projekt nie istnieje.

- https://www.reddit.com/r/VitaPiracy/comments/1d3i1cf/removed/ — wzmianka o Brave Dwarves 2 w wątku próśb o porty; nie przedstawia działającego projektu.
- https://www.reddit.com/r/tipofmyjoystick/comments/1b34cul/ — rozpoznawanie gry, bez implementacji silnika.
- https://www.reddit.com/r/tipofmyjoystick/comments/h7sb30/pcearly_2000slate_90s_a_game_about_magical_dwarves/ — również rozpoznawanie gry.

## Fakty z lokalnych plików

- Gnomes.exe: 692224 bajty; SHA-256 `1f040aca4a589705cd95c687401244c34148ff6fc65d8090697d061f698a658a`.
- REA/objdump: PE i386, punkt wejścia `0x0044dbf0`.
- REA parser PE: sześć zasobów; nie są to odzyskane zasoby całej gry.
- Czytelne łańcuchy EXE wskazują na DirectDraw, DirectInput, DirectSound, mpp.dll, Gnomes.dat i `Levels\Level%04d.dat`. Łańcuchy nie dowodzą, że wszystkie te ścieżki są używane w każdej sesji.
- Pierwsze bajty Gnomes.dat: `GaveOverGames' Resource File` (pisownia dokładna). Różni się to od łańcucha `GameOverGames' Resource File` widocznego w EXE. Nie ustalono jeszcze tabeli katalogowej ani kompresji tego pliku.
- Lokalny Readme.htm opisuje wojownika, maga, cztery żywioły i trzy klucze potrzebne do wyjścia z poziomu.

## Co pozostaje do właściwego odtworzenia silnika

1. Zidentyfikować odczyt nagłówka i katalogu Gnomes.dat, opisać format oraz stworzyć testowany czytnik do badań poza APK.
2. Powiązać instrukcje EXE z funkcjami aktualizacji świata, kolizji, sterowania, przeciwników i broni; do czytelniejszego pseudokodu skonfigurować natywny dekompilator obsługiwany przez REA.
3. Odtworzyć zachowanie na podstawie porównawczych pomiarów oryginału. Nie przypisywać prototypowi zgodności bez dowodów.
4. Uzupełnić własne poziomy, animacje, dźwięk, system energii, drabiny i bossów dla wersji samodzielnej.
5. Sprawdzić pełną rozgrywkę, zapis, wielodotyk i wydajność na Huawei; przygotować docelową konfigurację Android i podpis wydania.

APK 0.1 realizuje wyłącznie samodzielny prototyp mechanik. Nie zawiera ani nie odczytuje oryginalnych plików gry.

## Aktualizacja 0.2 — pliki użytkownika

Nowy kierunek: import oryginalnego Gnomes.dat osobno od APK. Rozpoznano wszystkie 244 wpisy zlib, 114 grafik PCX i podstawowe sekcje 55 map. Szczegółowy opis: FORMAT.md. Czytnik sprawdzono na całym archiwum, obrazy porównano z Pillow. APK wyświetla oryginalne mapy, postacie i przedmioty; pozwala na eksplorację z ruchem, drabinami i kamerą. AI, walka i dodatkowe sekcje map nadal wymagają odtworzenia. APK i źródła nie zawierają oryginalnych zasobów; wcześniejszy opis samodzielnego demo dotyczy wersji 0.1.
