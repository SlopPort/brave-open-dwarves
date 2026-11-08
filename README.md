# Brave Open Dwarves — Android 0.32

> **SLOP PORT.** Kod powstał w całości z pomocą AI (Claude Code) jako eksperymentalny port silnika. Jest niekompletny, niezgodny z oryginałem w wielu miejscach i nie został przegrany od początku do końca. Nie jest projektem oficjalnym ani powiązanym z twórcami Brave Dwarves. Repozytorium nie zawiera oryginalnych zasobów gry; potrzebny jest własny `Gnomes.dat`.

Poniższe instrukcje `0.6` są historyczne; bieżący stan opisują sekcje aktualizacji na końcu pliku.

Nowy silnik na Androida korzystający z oryginalnego Gnomes.dat dostarczonego przez użytkownika. APK i paczka źródłowa nie zawierają oryginalnych grafik, dźwięków, poziomów ani programów EXE/DLL. Projekt jest grywalnym prototypem; nie odtwarza jeszcze całej logiki Brave Dwarves.

## Uruchomienie

1. Zainstaluj dist/Brave-Open-Dwarves-0.6.apk.
2. Skopiuj własny Gnomes.dat z pierwszej części Brave Dwarves na telefon, np. do Pobranych.
3. W aplikacji wybierz WYBIERZ GNOMES.DAT i wskaż plik w systemowym oknie.
4. Wybierz EKSPLORUJ ORYGINALNE POZIOMY lub KONTYNUUJ ZAPISANĄ ROZGRYWKĘ.

Import sprawdza całe archiwum i kopiuje plik do prywatnej pamięci aplikacji. Nie wymaga dostępu do całej pamięci ani internetu. Dane pozostają po zamknięciu i aktualizacji APK. Sprawdzone wydanie: 4562788 bajtów, 244 wpisy. Inne wydania mogą wymagać rozszerzenia czytnika.

## Działa

- Odczyt 244 zasobów, 55 map i 114 obrazów PCX; wszystkie obrazy sprawdzone niezależnym dekoderem.
- Oryginalne kafelki, dekoracje, postacie, przedmioty i grafika pocisków.
- Ruch, skok, drabiny, kamera, podnoszenie klejnotów, kluczy i serc.
- Pięć podstawowych typów przeciwników: robaki, nietoperze, szkielety, większe kościotrupy i pełzacze.
- Uproszczone patrole, zawracanie przed dziurami, atak dystansowy pełzaczy, atak maga i wojownika, obrażenia, nietykalność, śmierć i ponowienie.
- Ataki i pociski blokowane przez ściany; poprawiony odczyt sekcji przeciwników na wszystkich mapach.
- Automatyczny zapis podczas wyjścia do menu i przejścia aplikacji w tło: pozycja, zdrowie, klucze, zebrane przedmioty i stan przeciwników. Zapis jest przypisany do dokładnych bajtów mapy.
- Powrót z tła i odtworzenie zapisu w pauzie.
- Przeglądarka oryginalnych obrazów PCX i osobne historyczne demo 0.1.

Sterowanie: strzałki, GÓRA/DÓŁ do drabin, SKOK, ATAK, MAG/WOJOWNIK do zmiany postaci. Górne strzałki przełączają pliki poziomów niezależnie od postępu — to narzędzie testowe. MAPA pokazuje cały poziom. Dotyk dwoma palcami pozwala jednocześnie iść i atakować.

## Pozostaje do odtworzenia

Pełna zgodność fizyki i AI z oryginalnym EXE, bossowie, pełny system broni/żywiołów i energii, zachowanie wszystkich przedmiotów i pułapek, ruchome platformy, muzyka IT, pełne animacje i oryginalne menu. Wpisy specjalne map są odczytywane, ale ich zachowanie nie jest jeszcze wykonywane. Zapis pomija pociski w locie i chwilowe timery. Nie wykonano pełnego przejścia kampanii.

Nowe modele ruchu i walki są eksperymentalne. Oryginalna grafika i poprawny odczyt map nie oznaczają jeszcze zgodności zachowania z oryginałem.

## Budowanie i testy

Windows PowerShell, Android SDK platform 35 / build-tools 35.0.0, JDK 21 z Android Studio:

```powershell
.\build.ps1
```

Opcje -Sdk i -Jdk zmieniają ścieżki. Budowanie nie pobiera zależności. APK ma lokalny klucz deweloperski. Minimalny Android 5.0/API 21; bieżący prototyp gry ma targetSdk 28, osobny pakiet testowy 35.

Testy na komputerze, domyślnie JDK 8 i lokalny Gnomes.dat:

```powershell
.\test-readers.ps1
.\test-readers.ps1 -ComparePillow
```

Obejmują czytniki, uszkodzone dane, walkę, ściany, ruch podstawowych przeciwników i zapis rozgrywki. Opcjonalne porównanie pikseli wymaga Pythona i Pillow; nie umieszcza obrazów w paczce źródłowej.

## Huawei P9 Lite

Testowany VNS-L21 z LineageOS Android 10/API 29, serial $DEVICE:

```powershell
adb -s $DEVICE install --no-streaming -r dist\Brave-Open-Dwarves-0.6.apk
adb -s $DEVICE push Gnomes.dat /sdcard/Download/Gnomes.dat
adb -s $DEVICE shell am start -W -n org.braveopendwarves/.MainActivity
```

Opcjonalne testy urządzenia wymagają wcześniejszego importu danych:

```powershell
.\build-device-tests.ps1
adb -s $DEVICE install --no-streaming -r build\Brave-Open-Dwarves-tests.apk
adb -s $DEVICE shell am instrument -w org.braveopendwarves.tests/org.braveopendwarves.DeviceTestRunner
```

Testy uruchamiają poziom, wstrzykują dotyk wielopunktowy oraz sprawdzają śmierć i zapis. Zachowują istniejący punkt kontrolny i odtwarzają go na końcu. Nie zatrzymuj procesu w trakcie testu, jeżeli ma automatycznie przywrócić ten zapis.

Raporty testów: analysis/TESTS-0.6.md i historyczne pliki TESTS. Format danych: analysis/FORMAT.md. Historia ustaleń: analysis/RESEARCH.md.

## REA i pochodzenie

Repozytorium https://github.com/morluto/rea jest lokalnie w rea/, commit 0a2b7af905a226540fbad637c60fe79b15a24549. CLI rea-agents 6.7.0 i Node 24.22.1 są w tools/rea/. REA jest narzędziem badawczym, nie zależnością APK. Raporty REA nie oznaczają odzyskania oryginalnych źródeł. analyze.ps1 ponawia analizę EXE przy użyciu REA i lokalnego objdump. tools/native_probe.py wyszukuje referencje do tekstów w zachowanym raporcie instrukcji.

Punktem odniesienia dla archiwum było https://github.com/ermaccer/BraveDwarves2.DATTool. Nowy czytnik Java napisano osobno i sprawdzono na pierwszej części. Poprawna kolejność sekcji map oraz tabela typów przeciwników zostały potwierdzone instrukcjami oryginalnego EXE. Szczegóły i ograniczenia zawiera FORMAT.md.

## Aktualizacja 0.7

Bieżący instalator: dist/Brave-Open-Dwarves-0.7.apk. Gra ma teraz targetSdk 35. Dodano czytnik sześciu oryginalnych definicji animacji ANM i podgląd animacji z menu (klatki, pętle, pauza, zmiana tempa). Nie oznacza to jeszcze implementacji AI bossów. Zdarzenia ANM i oryginalna skala czasu pozostają nieustalone. Nieistniejące odwołania w oryginalnych plikach LICH/SPIDER są ujawniane, nie zastępowane zgadywanymi klatkami. Aktualizacja przeszła ponownie dziewięć testów integracyjnych na Huawei.

## Aktualizacja 0.8

Instalator: dist/Brave-Open-Dwarves-0.8.apk. Dodano 179 oryginalnych wind obu typów, ruch według torów i ograniczeń odczytanych z EXE, przenoszenie gracza i zapis stanu platform. Na Huawei przeszło 13 testów integracyjnych. Raport: analysis/TESTS-0.8.md. Oryginalne dane nadal importuje użytkownik; nie ma ich w APK ani paczce źródeł. To nadal rozwijany port, z niepełną zgodnością fizyki, walki i bossów.

## Aktualizacja 0.9

Instalator: dist/Brave-Open-Dwarves-0.9.apk. Drzwi mają oryginalną grafikę, sekwencję otwierania i dźwięk, dokładniejszy obszar wejścia oraz zapis postępu animacji. Na Huawei przeszło 16 testów integracyjnych. Szczegóły i ograniczenia: analysis/TESTS-0.9.md. Zegar animacji i zachowanie drzwi na poziomach bossów nadal wymagają pracy.

## Aktualizacja 0.10

Instalator: dist/Brave-Open-Dwarves-0.10.apk. Dodano oryginalną punktację skarbów na bieżącej mapie, zbieranie większej liczby kosztowności i dźwięk skrzyń. Poprawiono błędną interpretację przedmiotu31 oraz identyfikatory i działanie przedmiotów leczących. 20 testów integracyjnych przeszło na Huawei. Raport i ograniczenia: analysis/TESTS-0.10.md. APK i źródła nie zawierają oryginalnych zasobów.

## Aktualizacja 0.11

Instalator: dist/Brave-Open-Dwarves-0.11.apk. Oryginalne flagi poziomów uruchamiają śmiertelne obszary i tonięcie w wodzie. Zapis zachowuje licznik oddechu i częściowe obrażenia. Na Huawei przeszły23 testy integracyjne. Raport: analysis/TESTS-0.11.md. Pozostałe pułapki, pełna fizyka wody, bossowie i bronie są nadal w budowie.

## Aktualizacja 0.12

Instalator: dist/Brave-Open-Dwarves-0.12.apk. Poprawiono dobór klatek kierunku, skoku i wspinania. Postać utrzymuje pozycję na drabinie po zwolnieniu przycisku i może od niej odskoczyć. Pauza zatrzymuje animacje świata. Przeszło27 testów na Huawei. Raport: analysis/TESTS-0.12.md. Pełne animacje walki i śmierci oraz zgodność fizyki nadal wymagają pracy.

## Aktualizacja 0.13

Instalator: dist/Brave-Open-Dwarves-0.13.apk. Przejścia zachowują punkty skarbów i zdrowie. Główna kampania kończy się po50, a nie po dodatkowych mapach1001..1005; stan zakończenia i wynik są zapisywane. Na Huawei przeszło31 kontroli. Raport: analysis/TESTS-0.13.md. Nie jest to jeszcze grywalna od początku do końca kampania: obsługa bossów nadal wymaga pracy.

## Aktualizacja 0.14

Instalator: dist/Brave-Open-Dwarves-0.14.apk. Pierwszy boss (duży robak, mapa10) patroluje, przygotowuje atak i strzela, używając oryginalnych animacji ANM. Można zadawać mu obrażenia; jego pokonanie uruchamia otwieranie wyjścia. Na Huawei przeszły34 kontrole. Raport i istotne uproszczenia walki: analysis/TESTS-0.14.md. Pozostali bossowie nadal wymagają implementacji.

## Aktualizacja 0.15

Instalator: dist/Brave-Open-Dwarves-0.15.apk. Pocisk robaka używa właściwych oryginalnych ramek oraz potwierdzonej prędkości i zasięgu, przeliczonych na roboczy zegar portu. Pokonany boss pozostaje w oryginalnej klatce śmierci. Ponownie przeszły34 kontrole Huawei. Raport: analysis/TESTS-0.15.md. Obrażenia i pełne zachowanie bossów nadal wymagają pracy.

## Aktualizacja 0.16

Instalator: dist/Brave-Open-Dwarves-0.16.apk. Dodano minotaura z mapy20: patrol, oryginalne animacje i salwę ośmiu animowanych czaszek. Pociski poruszają się teraz także pionowo. Pokonanie bossa odblokowuje wyjście. Przeszło37 kontroli Huawei. Raport i ograniczenia: analysis/TESTS-0.16.md. Przywoływanie przeciwników i pozostali bossowie nadal wymagają pracy.

## Aktualizacja 0.17

Instalator: dist/Brave-Open-Dwarves-0.17.apk. Skorygowano wspólny zegar odtworzonych mechanizmów z roboczych60 do nominalnych75 kroków/s, zgodnie z domyślnym limitem oryginału. Na Huawei przeszło38 kontroli. Raport: analysis/TESTS-0.17.md. Tempo oryginału zależało również od ustawień i wydajności; port nie ma jeszcze pełnej zgodności fizyki, broni i bossów.

## Aktualizacja 0.18

Instalator: dist/Brave-Open-Dwarves-0.18.apk. Zdrowie aktorów, leczenie i obrażenia odtworzonych pocisków korzystają teraz z oryginalnych jednostek: bohater20HP, robak500, minotaur1000. Dodano migrację wcześniejszych zapisów. 38 kontroli Huawei przeszło. Raport: analysis/TESTS-0.18.md. Walka wręcz, obrażenia kontaktowe, pełne bronie i pozostali bossowie nadal wymagają pracy.

## Aktualizacja 0.19

Instalator: dist/Brave-Open-Dwarves-0.19.apk. Dodano wzajemne osłabianie i niszczenie przeciwnych pocisków oraz potwierdzone5HP obrażeń kontaktowych. Pozostała siła przechwyconego strzału wpływa na kolejne trafienie. Na Huawei przeszło40 kontroli. Raport: analysis/TESTS-0.19.md. Pełne bronie, osłony i pozostali bossowie nadal wymagają pracy.

## Aktualizacja 0.20

Instalator: dist/Brave-Open-Dwarves-0.20.apk. Czas niewrażliwości po trafieniu i ochrona przedmiotu40 wynikają teraz z oryginalnych liczników. Zapis zachowuje pozostałą ochronę. Na Huawei przeszły42 kontrole. Raport: analysis/TESTS-0.20.md. Odpychanie, pełne animacje trafienia, energetyczne osłony i pozostali bossowie nadal wymagają pracy.

## Aktualizacja 0.21

Instalator: dist/Brave-Open-Dwarves-0.21.apk. Dodano pająka z mapy30 i opadającą pajęczynę, która czasowo unieruchamia bohatera. Działają oryginalne animacje i zapis czasu związania. Na Huawei przeszło46 kontroli. Raport: analysis/TESTS-0.21.md. Pozostałe fazy pająka oraz bossowie40/50 nadal wymagają pracy.

### 0.22
Fire elemental actor on map 40: original animations, 750 health, basic patrol, defeat and exit unlock. Projectile attack and special state remain pending. Original game data must be supplied separately.

### 0.23
Fire elemental now casts three original missiles after nineteen ticks, with original directional frames and two damage points. Cast uses original nominal timing and random delay range. Special behavior and full movement fidelity remain pending.

### 0.24
Fire elemental teleports to original eligible floor cells, with original delay ranges and quick follow-up cast. Teleported position survives checkpoint restore. Particle effects, sounds, exact physics and boss timer persistence remain pending.

### 0.25
Checkpoint now preserves enemy vertical movement, turn delay, prepared attacks and fire elemental teleport countdown/pending state. Older checkpoints remain readable. Active projectiles and hero attack timing are still not saved.

## Wersja 0.26 — poprzedni punkt przerwania pracy

Dodano licza z poziomu50: oryginalne animacje i800 punktow zdrowia, trzy losowane ataki (osiem czaszek, trzy pociski kierunkowe lub cztery sople znad ekranu), teleportacje oraz zapis stanu. Po pokonaniu przeciwnikow na arenie otwiera sie ostatnie wyjscie (dokladny warunek oryginalu nadal wymaga analizy). APK wymaga osobno dostarczonych oryginalnych plikow gry.

Port pozostaje nieukonczony. Dokladna fizyka i fazy bossow, czesc broni/przedmiotow, efekty i pelna zgodnosc kampanii wymagaja dalszej pracy. Starszy zapis poziomu50 sprzed dodania licza wymaga ponownego rozpoczecia tej mapy. Przerwa po0.26 zostala zakonczona na prosbe uzytkownika; rozwoj wznowiono.

## Wersja0.27 — wznowienie
Supaplex zostal wylaczony na Huawei. Dodano oryginalna reakcje licza na trafienie (przyspieszenie teleportacji) oraz odpornosci i podatnosc ognistego bossa na typy pociskow. Nie oznacza to jeszcze obslugi wszystkich broni oryginalu.

## Wersja0.28
Dodano cztery zasoby energii: zbieranie przedmiotow1..6, limit100, koszt podstawowego strzalu maga, licznik MANA oraz zachowanie energii w zapisie i miedzy poziomami. Mag zaczyna z25 jednostkami pierwszego zasobu. Pozostale czary i osobne wyposazenie bohaterow nadal wymagaja pracy.

## Wersja0.29
Poprawiono energie przy przejsciu do nowego rozdzialu: oryginal zeruje zasoby na mapach11/21/31/41 i przywraca25 jednostek poczatkowego kanalu bohatera. Zwykle przejscia zachowuja energie. Wczesniejsze przenoszenie wszystkich zasobow przez te granice w0.28 bylo niezgodne z oryginalem.

## Wersja0.30
Dodano automatyczne odnawianie podstawowej energii bohatera: jedna jednostka co128 nominalnych krokow gry, do25. Nadwyzka z przedmiotow pozostaje zachowana. Zapis pamieta czas do kolejnego odnowienia; pauza zatrzymuje licznik. Pozostale czary i ich odblokowywanie nadal wymagaja analizy.

## Wersja0.31
Kule mocy55..58 zwiekszaja teraz poziomy umiejetnosci. Mag moze odblokowac kule ognia i blyskawice; dotkniecie napisu CZAR w prawym dolnym rogu zmienia dostepny czar. Zapis i zwykle przejscia zachowuja poziomy oraz wybor, a nowy rozdzial je resetuje. Czwarty podstawowy czar i wyzsze poziomy nadal wymagaja odtworzenia.

## Wersja0.32
Dodano czwarty podstawowy czar maga, odblokowywany kula58: oryginalny pocisk, koszt energii i dwie klatki animacji. Wybor CZAR obejmuje teraz cztery podstawowe czary. Poprawiono czestotliwosc ich strzalow wedlug licznikow oryginalu. Wyzsze poziomy czarow i efekty trafienia nadal wymagaja pracy.
