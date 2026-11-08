# Brave Open Dwarves — TODO bez telefonu

Stan wyjściowy: wersja 0.32. Testy desktopowe (`.\test-readers.ps1`) przechodzą, 98 asercji na Huawei przeszło przy 0.32.
Każdy punkt robimy i weryfikujemy na komputerze: analiza `Gnomes.exe` przez REA, implementacja w klasach `data/`, test w `tests/`.
Rzeczy wymagające ekranu lub dotyku trafiają na listę „Do sprawdzenia na Huawei” na końcu.

Zasada: mechaniki bierzemy z instrukcji EXE z podanym adresem VA. Czego nie da się potwierdzić, oznaczamy jako nieustalone i niczego nie zgadujemy.

## 0. Porządki (najpierw)

- [ ] `git init` i pierwszy commit (sprawdzić, czy `.gitignore` wyklucza `Gnomes.dat`, EXE/DLL, zapisy i `build/`)
- [ ] Przenieść `dist/` 0.1–0.31 poza drzewo projektu albo usunąć po potwierdzeniu
- [ ] README: nagłówek i instrukcje nadal mówią o 0.6, uzupełnić o bieżącą wersję; historię przenieść do `CHANGELOG.md`
- [ ] Parametr wersji w `build.ps1` zsynchronizować z `versionName` w manifeście
- [ ] Jeden skrypt `test-all.ps1` z podsumowaniem; `test-readers.ps1` ma na sztywno ścieżkę JDK 8

## 1. Refaktor pod testowalność

- [ ] Wydzielić logikę gry z `OriginalGameView.java` (ok. 25 kB) do klasy `GameSession` bez zależności od Androida
- [ ] Stały krok symulacji 75 Hz niezależny od klatek renderowania
- [ ] Headless runner: skrypt wejść → przebieg → stan, deterministyczne ziarno RNG
- [ ] Przeniesienie testów logiki z `DeviceTestRunner` na desktop tam, gdzie nie dotyczą dotyku/UI

## 2. Fizyka i ruch (największa luka zgodności)

- [ ] Odtworzyć z EXE: prędkości chodu, skok, grawitacja, opadanie
- [ ] Kolizja gracza z kafelkami według oryginalnych hitboxów
- [ ] Drabiny i windy: logika wnętrza kabiny (0x415e90 i dalej)
- [ ] Fizyka wody poza licznikiem oddechu; flaga osłony w wodzie (0x40b84f)
- [ ] Odpychanie po trafieniu

## 3. Walka i postacie

- [ ] Wyższe poziomy czarów (tabela 0x49e400 ma 12 rekordów, działają 4 podstawowe)
- [ ] Czary/umiejętności wojownika
- [ ] Ustalić, jak normalna progresja nadaje poziomy umiejętności (obecnie nierozstrzygnięte)
- [ ] Ładowanie maksymalnego zasięgu pocisków i energetyczne osłony
- [ ] Zapis pocisków w locie i timera ataku bohatera w checkpoincie
- [ ] Przedmiot 34: oryginalne informacje o haśle/checkpoincie

## 4. AI przeciwników i bossowie

- [ ] Zastąpić uproszczone patrole AI podstawowych typów logiką z EXE
- [ ] Robak: pozostałe stany walki
- [ ] Minotaur: przywoływanie w stanie 12 (0x408c27, 0x408ef0), oryginalna grawitacja i czasy
- [ ] Pająk: pozostałe fazy
- [ ] Ognisty boss: stan specjalny, dokładny ruch
- [ ] Licz: fazy śmierci/odrodzenia, dokładny warunek ostatniego wyjścia
- [ ] Wpisy Marker: dokończyć znaczenie funkcjonalne (179 wpisów)

## 5. Świat i kampania

- [ ] Pozostałe pułapki i przedmioty bez zachowania (przegląd wszystkich ID z map)
- [ ] Warunki wejścia na mapy 1001–1005
- [ ] Headless: czy każda z 50 map ma osiągalne wyjście i klucze (BFS na kolizji; to nie zastępuje przejścia ręcznego)
- [ ] Ekran końca gry i tabela wyników (format `hiscore.dat`, 240 bajtów)
- [ ] Hasła poziomów, jeżeli potwierdzi je analiza przedmiotu 34

## 6. Dźwięk i muzyka

- [ ] Inwentaryzacja WAV w archiwum wraz z miejscami użycia w EXE (podpięte są tylko 4: diament, klucz, drzwi, skrzynia)
- [ ] Menedżer dźwięków z mapowaniem zdarzenie → próbka, limit strumieni
- [ ] Odtwarzacz modułów IT (MIT lub własny, bez zasobów w APK); desktopowy render do WAV jako test
- [ ] Przypisanie utworów do rozdziałów według EXE

## 7. Grafika i UI

- [ ] Efekty cząsteczkowe (np. efekt 254 po czarze typu 6, plusk w wodzie)
- [ ] Pełne animacje walki, rzucania czarów, trafienia i śmierci
- [ ] Oryginalne menu z `Menu.pcx` oraz HUD w miejsce napisów
- [ ] Rozpoznać zdarzenia ANM i ich skalę czasu
- [ ] Ukryć narzędzia testowe (przełączanie map, MAPA) za trybem deweloperskim
- [ ] Sterowanie: układ przycisków i rozmiary w konfiguracji

## 8. Niezawodność i wydanie

- [ ] Testy odporności zapisu: migracja wszystkich wersji, uszkodzony plik
- [ ] Test obciążeniowy pętli logiki: alokacje na klatkę istotne dla P9 Lite
- [ ] Zapis atomowy (plik tymczasowy + rename)
- [ ] Prawa: sprawdzić `LICENSE` i brak zasobów oryginału w paczce źródeł

## Do sprawdzenia na Huawei (po podłączeniu adb)

Serial: `$DEVICE`, LineageOS Android 10.

- [ ] Instalacja bieżącego APK, pełny zestaw `DeviceTestRunner`
- [ ] FPS i temperatura na mapach z największą liczbą aktorów
- [ ] Opóźnienie i miksowanie dźwięku, muzyka w tle
- [ ] Odczucie sterowania, układ przycisków, dotyk wielopunktowy
- [ ] Wygląd efektów, animacji, oryginalnego menu
- [ ] Powrót z tła i zapis w trakcie walki z bossem
- [ ] Ręczne przejście kampanii 1–50 (dotąd niewykonane)

## Proponowana kolejność

0 → 1 → 2 → 6 → 3 → 4 → 5 → 7 → 8. Refaktor jest pierwszy, bo daje weryfikację bez telefonu; fizyka wpływa na wszystko dalej, a dźwięk jest najbardziej widoczną brakującą funkcją przy niskim ryzyku.
