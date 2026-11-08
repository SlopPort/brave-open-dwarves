# Brave Open Dwarves: droga do 1.0

Stan: 0.33 + commity do `4ef7b47`. Testy desktopowe przechodzą. Nowa fizyka, kolizje, cios wojownika i czary nie były jeszcze uruchamiane na Huawei.

## Definicja 1.0

- [ ] Kampania 1–50 przechodzi się ręcznie od początku do końca, obiema postaciami tam, gdzie gra na to pozwala
- [ ] Wszystkie mechaniki użyte w mapach działają; każda różnica wobec oryginału jest wypisana w README
- [ ] Muzyka, dźwięki, oryginalne menu, HUD i ekran końcowy
- [ ] Stabilne 60 FPS na Huawei P9 Lite, 30 minut gry bez awarii
- [ ] Zapis przeżywa aktualizację z 0.33 do 1.0 albo gra jasno informuje, że wymaga nowego startu
- [ ] APK ani repozytorium nie zawierają zasobów oryginału

Legenda: **[PC]** zrobione i sprawdzone na komputerze, **[TEL]** wymaga Huawei.

## 0.34: walidacja dotychczasowych zmian [TEL]

- [x] Uruchomić `DeviceTestRunner`, poprawić asercje zależne od starej fizyki (98/98, `analysis/huawei-033-device-tests.txt`)
- [ ] Sprawdzić drganie kroku 75 Hz na ekranie 60 Hz; w razie potrzeby dodać interpolację rysowania
- [ ] Ocenić skok z przytrzymaniem na dotyku
- [ ] Sprawdzić klatki `torn_1..4` i sople
- [ ] Przejść mapy 1–5 po zmianie kolizji 70×75

## 0.35: dokończenie fizyki [PC]

- [ ] Współrzędne portu przeliczyć na oryginalne: środek x, stopy y (`0x10`, `0x14`) lub udokumentować stałe przesunięcie
- [x] Podłoże: sonda `0x415ce0` (szerokość/8, typ 1 lub flaga 0x20); osłona i flaga 0x400 czekają na czar tarczy
- [ ] Wejście i zejście z drabiny: `0x40a7f1`, `0x40a956`, `0x40ab8b`, zejście `0x415e20`
- [ ] Automatyczne powtarzanie skoku i flaga `0x4c` bit 4 / `0x4b0c80` (`0x40a8fa`)
- [ ] Spadanie i ruch drugiego bohatera: pełny odczyt `0x40cbfb..0x40cc67`
- [ ] Windy: wnętrze kabiny, zgniatanie, `0x414b40`, `0x415e90..0x416046`
- [ ] Odpychanie po trafieniu i ograniczenia sterowania
- [ ] Woda: plusk, utrata osłony (`0x40b84f`)
- [ ] BFS: osiągalność wyjścia i kluczy na 55 mapach przy nowych kolizjach, raport map do ręcznej kontroli

## 0.36: bronie i czary [PC]

- [ ] Czar 5, potrójny ogień: tabele `0x49e388/0x49e3a8`, klatki `mis_*`
- [ ] Czar 6, typ 7: pełna logika
- [ ] Czar 7, głaz, typ 11: ruch, kolizje, `0x40bc7f`, wybuch w `0x405a70`
- [ ] Czar 9, tarcza: `+0x3c=500`, działanie w `0x415dd2`, drugi tryb strzału
- [ ] Czar 10, gwiazdy, typ 13: orbita i naprowadzanie
- [ ] Czar 11, wstrząs: `0x40bdde`, licznik `0x4b0444`
- [ ] Tornado: czy przebija cele
- [ ] Tabela wojownika `0x49e4c8`, wszystkie 12 ataków, `0x40d760` i wykonanie w `0x40dbc0`
- [ ] Zasięg ciosu z typu pocisku 4 zamiast własnego
- [ ] Przedłużanie przerwy (`0x32`, `0x4a6250`, `0x4a626c`) oraz stany rzucania
- [ ] Pociski w locie i timer ataku w zapisie (nowa wersja `RunState` z migracją)
- [ ] Tarcze trafień i efekty pocisków `0x405ce0`, `0x405a70`

## 0.37: przeciwnicy [PC]

- [ ] Typy 2–6: pełne AI z EXE (`0x4090c0`, `0x409730`, `0x409cc0`, `0x40e810`) w miejsce uproszczonych patroli
- [ ] Rodzenie szkieletów (`skel_born`, `0x40def0`) i rzuty (`skel_throw`)
- [ ] Obrażenia kontaktowe i wzajemne kolizje
- [ ] Przegląd wszystkich typów przeciwników z 55 map: każdy obsłużony lub zgłoszony

## 0.38: bossowie [PC, potem TEL]

- [ ] Robak: pozostałe stany
- [ ] Minotaur: przywoływanie, stan 12 (`0x408c27`, `0x408ef0`), grawitacja i czasy
- [ ] Pająk: pozostałe fazy
- [ ] Ognisty boss: stan specjalny (`0x407e60`), ruch
- [ ] Licz: fazy śmierci i odrodzenia, warunek ostatniego wyjścia
- [ ] Pasek życia i muzyka bossa
- [ ] [TEL] Ręczna walka z każdym z 5 bossów obiema postaciami

## 0.39: świat i kampania [PC]

- [ ] Spis wszystkich ID przedmiotów i flag komórek z map; każdy obsłużony lub uzasadniony
- [ ] Pułapki, głazy, ogień, liny, mosty (`Data/Optimizer/*.ini`, `LevelType*.ini`)
- [ ] Wpisy Marker: znaczenie 179 wpisów
- [ ] Przedmiot 34: hasło i punkt kontrolny, zegar (`Clock`, `0x4163f1`)
- [ ] Mapy 1001–1005: warunki wejścia
- [ ] Punktacja końcowa, `hiscore.dat`, tabela wyników
- [ ] Wybór postaci i start kampanii; ukrycie przełączania map w trybie deweloperskim

## 0.40: dźwięk [PC, potem TEL]

- [ ] Menedżer próbek: wszystkie 36 WAV, limit kanałów, pętle `Walk` i `Climb`
- [ ] Każde miejsce odtwarzania z `FORMAT.md` przypięte do zdarzenia; argumenty `0x64`, `0x32` zrozumiane
- [ ] Dźwięk w `GameSession.Host` z testem kolejności zdarzeń
- [ ] Dekoder IT: nagłówek, wzorce, próbki, kompresja IT214
- [ ] Odtwarzacz IT z efektami używanymi przez 7 utworów; render do WAV na komputerze
- [ ] Podpięcie `MusicRules`, ustalenie flagi `0x4b0460`, `happy_end.it`, muzyka menu
- [ ] Głośność w ustawieniach
- [ ] [TEL] Opóźnienie, miksowanie, brak trzasków, pauza w tle

## 0.41: grafika i UI [PC, potem TEL]

- [ ] Fonty `*.fnt`, `fonts.frm`, panele `PANEL.FRM`, `weapanel.frm`
- [ ] Oryginalny HUD w miejsce tekstów
- [ ] Menu z `Menu.pcx`, `selectdwarf.tga`, `Front.tga`; loader TGA
- [ ] Pomoc `Help/*.txt`, zakończenie `HappyEnd`
- [ ] Zdarzenia ANM i skala czasu
- [ ] Pełne animacje bohaterów: walka, czary, trafienie, śmierć
- [ ] Efekty: cząsteczki, plusk, efekt 254, wybuchy
- [ ] Ekran wyboru czarów zamiast dotykania napisu CZAR
- [ ] Usunąć historyczne demo `GameView` i przeniesienie narzędzi za tryb deweloperski
- [ ] [TEL] Układ i rozmiar przycisków, dotyk wielopunktowy, opcjonalna przezroczysta nakładka

## 0.42: niezawodność [PC, potem TEL]

- [ ] Pełne migracje zapisów, uszkodzone pliki, zapis atomowy
- [ ] Kopia i usuwanie zaimportowanych danych, komunikaty błędów importu
- [ ] Ograniczenie alokacji w pętli i pamięci bitmap; profil na najcięższych mapach
- [ ] Wyjątki ze sterowaniem awarią: czytelny komunikat zamiast zamknięcia
- [ ] Headless: 55 map, wiele ziaren, losowe wejścia, brak wyjątków i wyjścia poza mapę
- [ ] Przenieść testy logiki z `DeviceTestRunner` na komputer
- [ ] [TEL] 30 minut gry, temperatura, cykl tło/powrót, obrót, niski poziom baterii

## 0.9: kandydat na wydanie [TEL]

- [ ] Ręczne przejście kampanii 1–50, lista błędów, poprawki blokujące
- [ ] Porównanie z oryginałem przez dgVoodoo na wybranych mapach: tempo, skoki, walka
- [ ] Sprawdzenie na innym urządzeniu lub emulatorze API 21 i 35

## 1.0: wydanie

- [ ] README od nowa, `CHANGELOG.md`, lista znanych różnic
- [ ] Licencja i kontrola zawartości: bez zasobów, bez danych osobistych; ocena publikacji notatek z EXE
- [ ] Klucz podpisu wydania poza repozytorium, `versionCode`, nazwa pakietu
- [ ] Wydanie GitHub z APK, sumą SHA256 i instrukcją importu `Gnomes.dat`
