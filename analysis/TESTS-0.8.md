# Brave Open Dwarves 0.8

APK zainstalowane na Huawei VNS-L21, serial $DEVICE, Android 10/API29. Aktualizacja zachowała zaimportowany Gnomes.dat i poprzedni punkt kontrolny. APK nie zawiera danych gry.

## Wyniki

- Czytniki: 244 wpisy, 114 PCX, 55 map, 19 odrzuconych uszkodzonych próbek.
- Dotychczasowe testy walki, zapisu i ANM przeszły.
- LiftTest: geometria obu typów, krok 10 ms, tor poziomy, postój i zmiana kierunku, pionowa flaga zatrzymania, faza animacji, obsługa błędnego czasu i odtwarzanie stanu. Wszystkie 179 wind z 55 map przeszły symulację 30 sekund bez wyjścia poza mapę.
- Instrumentacja Huawei: 13 PASS. Obejmuje dziewięć wcześniejszych kontroli, trzy windy na mapie 11, przenoszenie postaci poziomo i pionowo, zapis pozycji windy. Raport huawei-08-device-tests.txt. W trakcie przygotowania próba jazdy w górę z górnego przystanku prawidłowo zatrzymała platformę; właściwy test przeprowadzono ze środka szybu.
- Oględziny obrazu na urządzeniu: mapa 11, trzy windy z oryginalnego Animate2.pcx, screenshot huawei-lifts-08.png.
- Podpis APK: v1/v2/v3 poprawne. Zawartość APK kontrolowana przez package_sources.py.

## Ograniczenia

Początkowy postój jest stały (2 s), zamiast losowania oryginału. Przy ruchu pionowym nie odtworzono jeszcze oryginalnej blokady przygniatania. Własna fizyka gracza, AI, obrażenia, przejścia przez drzwi i logika bossów nadal nie stanowią pełnej zgodności. Efekty i dźwięki wind oraz animacja bloku napędowego oczekują na integrację.

Zapis ma teraz wersję 2 i obejmuje położenie, kierunek, fazę i zegar wind. Stare zapisy wersji 1 działają na mapach bez wind; na mapach z windami konieczne jest rozpoczęcie mapy ponownie (jawny komunikat). Testy urządzenia zachowują i przywracają wcześniejsze prywatne pliki zapisu.
