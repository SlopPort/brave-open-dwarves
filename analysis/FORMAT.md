# Brave Dwarves 1: zaobserwowane formaty danych

Stan 0.2, 25.10.2026. Opis dotyczy lokalnego Gnomes.dat o SHA-256
`f16ed28c04404a3232aebe1c79a35e4798230d6f786a0bafe33d7e0b1d12e61e`.
Nie gwarantuje zgodności ze wszystkimi wydaniami i drugą częścią gry.
Wszystkie liczby wielobajtowe są little-endian.

## Archiwum

Punktem odniesienia były definicje formatu w projekcie
https://github.com/ermaccer/BraveDwarves2.DATTool, `source/BD2.DATTool.cpp`.
Nowy czytnik Java napisano osobno i sprawdzono na całym archiwum pierwszej części.
Nie włączono kodu ani binariów tego narzędzia do APK.

Nagłówek ma 256 bajtów:

| Offset | Typ | Obserwacja |
| --- | --- | --- |
| 0 | char[32] | `GaveOverGames' Resource File`, zakończone NUL |
| 32 | byte[6] | `03 00 00 01 00 00` |
| 38 | uint32 | Liczba wpisów: 244 |
| 42 | char[214] | Tytuł `Brave Dwarves' Adventure` |

Wpisy są zapisane sekwencyjnie, od offsetu 256:

| Offset we wpisie | Typ | Obserwacja |
| --- | --- | --- |
| 0 | uint32 | Offset nazwy: pozycja wpisu + 22 |
| 4 | uint32 | `0x001420a0` we wszystkich wpisach; znaczenie nieustalone |
| 8 | uint32 | Offset następnego wpisu / końca archiwum |
| 12 | uint32 | Rozmiar po dekompresji |
| 16 | uint32 | Rozmiar spakowanego strumienia |
| 20 | uint16 | Pole zachowane jako zarezerwowane |
| 22 | char[] | Nazwa CP1252 z NUL, separator Windows |
| po nazwie | byte[] | Strumień zlib |
| po strumieniu | byte | Końcowe zero |

Wszystkie 244 strumienie rozpakowano z kontrolą końca zlib i zgodności długości.
Suma rozmiarów po dekompresji: 10282171 bajtów. Następny offset po ostatnim wpisie
jest równy rozmiarowi archiwum: 4562788 bajtów.

## Obrazy PCX

114 obrazów: 109 z paletą 256 kolorów i 5 w 24-bitowym RGB z trzema płaszczyznami.
Wszystkie używają ośmiu bitów na płaszczyznę i RLE. Szerokość wiersza w nagłówku
uwzględnia dopełnienie, którego nie wolno wyświetlać jako pikseli.

Surowe piksele dekodera Java zgadzają się z Pillow dla wszystkich 114 obrazów.
Tryb przezroczystości wybiera najczęściej używany kolor magenta w danym arkuszu.
To reguła nowego renderera, a nie ustalony algorytm oryginalnego silnika.
W paletach występują m.in. tła RGB (255,0,255) i (252,4,252).

Arkusze Blocks/Animate mają kafelki 64×64 i dziesięć kolumn; Items ma
komórki 32×32 i dwadzieścia kolumn; użyte arkusze postaci mają komórki 80×80.

## Poziomy

W archiwum jest 50 plików Level0001–Level0050 oraz 5 plików Level1001–Level1005.
Nie ustalono jeszcze semantyki dodatkowej serii 1001.

| Offset | Typ | Interpretacja używana w 0.2 |
| --- | --- | --- |
| 0, 4 | uint32 | Szerokość i wysokość siatki |
| 8 | uint32 | Pole tekstowe/licznik, nieinterpretowane |
| 12–331 | byte[] | Dane tekstowe, zachowane w pliku źródłowym |
| 332, 336 | uint32 | Początkowa kolumna i wiersz postaci |
| 340 | uint32 | Liczba wpisów przedmiotów |
| 344, 348 | uint32 | Kolumna i wiersz wyjścia |
| 352 | uint32 | Nieustalone |
| 356 | uint32 | Liczba wpisów przeciwników |
| 360 | uint32 | Motyw graficzny 1–5 |
| 364–2047 | byte[] | Nieinterpretowane |
| 2048 | cell[width×height] | Siatka w kolejności wierszy |

Komórka ma 24 bajty / sześć uint32: rodzaj 0/1, podstawowy kafelek Blocks,
nakładka Blocks, nakładka Animate, pole nieustalone, flagi. Indeksy kafelków
są zerowe; wartość zero traktowana jest jako brak warstwy. Rodzaj 1 jest używany
jako pełna kolizja; flaga `0x10` wskazuje drabiny. Inne flagi nie są jeszcze
zaimplementowane. Interpretację wizualną i drabiny sprawdzono na poziomie 1;
nie potwierdzono wszystkich zasad kolizji w oryginalnym programie.

Po siatce: `count` wpisów przedmiotów po 10 bajtów: uint16 typ, uint16 x,
uint16 y, uint32 nieustalone. Identyfikator grafiki przedmiotu jest używany jako
indeks od jedynki. Typy 41–43 odpowiadają dużym diamentom na arkuszu Items.

Dalej: wpisy przeciwników po 8 bajtów: uint16 typ, uint16 flagi, uint16 x,
uint16 y. W eksploratorze typ 2 pokazuje nieruchomą grafikę szkieletu.

W 37 z 55 map po tych sekcjach pozostają dodatkowe dane, długości podzielnej
przez 6. Czytnik zachowuje je w `trailer`; nie uznaje ich za znane obiekty.
Pełna semantyka wind, pułapek, ruchomych platform, wody i bram pozostaje otwarta.

## Granice zgodności

Odtworzony format i zgodne piksele nie oznaczają odtworzenia logiki oryginalnego
EXE. Prędkość, grawitacja, zasięg podnoszenia, przechodzenie po drabinach i
warunek przejścia są na razie własną implementacją eksploratora.
REA jest używane do analizy programu Windows; raporty instrukcji i zasobów
są oddzielone od nowego kodu. Nie wykonano jeszcze dowodowego powiązania
każdej interpretacji formatu z funkcjami EXE.

## Korekta formatu na podstawie instrukcji EXE — 0.4

Poprzedni opis danych końcowych został skorygowany. Instrukcje z raportu REA, funkcja w zakresie VA 0x412ee2–0x41320b, potwierdzają kolejność:

1. Nagłówek 0x800 bajtów: kopiowanie 0x200 dword przy 0x412f40.
2. Siatka width×height, krok 0x18 przy 0x412f9f.
3. Przedmioty, licznik pod adresem nagłówka +340 i krok 10 przy 0x41304e.
4. Wpisy specjalne, licznik nagłówka +352 i krok 6 przy 0x413154.
5. Przeciwnicy, licznik nagłówka +356 i krok 8 przy 0x4131b5.

Sześciobajtowe wpisy **nie są końcowym trailerem**. W wersji 0.3 i starszej na 37 mapach przesuwały odczyt przeciwników. Czytnik 0.4 odczytuje je osobno jako Marker(type,x,y) przed sekcją Enemy. W całym archiwum jest 179 takich wpisów: 35 typu 1 i 144 typu 2. Znaczenie funkcjonalne nie zostało jeszcze w pełni odtworzone. Nowy układ zużywa dokładnie wszystkie bajty każdego z 55 poziomów. Test dodatkowo wymaga zerowej długości pozostałych danych i pozycji przeciwników wewnątrz mapy.

Wybrany fragment dowodowy z zachowaniem identyfikatora źródła REA: level-loader-reference.json. Skrypt tools/native_probe.py znajduje odwołania do literałów w zachowanym raporcie, nie uruchamia oryginału.

## Tekstowe ramki FRM — 0.4

Pliki SHOTS.FRM, PANEL.FRM, weapanel.frm i fonts.frm zawierają sekcje Sprite, nazwy i prostokąty. Nowy czytnik SpriteFrames odczytuje rect jako lewy/górny/prawy/dolny brzeg, a rectsx jako x/y/szerokość/wysokość. Kontroluje zakres prostokąta względem arkusza. Pocisk maga w 0.4 korzysta z ramek bt_right/bt_left z oryginalnego SHOTS.FRM i Shots.pcx. Nie jest to jeszcze odtworzony kompletny system broni.

## Tabela typów przeciwników potwierdzona w EXE

Oryginalna funkcja inicjalizacji w VA 0x4152e0 odczytuje nazwę z tablicy przy 0x49ea70 (instrukcja przy 0x415397), indeksowanej typem wpisu przeciwnika. Tablica zawiera: 0 brak, 1 gnome1, 2 worm, 3 bat, 4 skel, 5 minoskel, 6 crawler, 7 gnome2_hm, 8 boss_spider, 9 boss_worm, 10 boss_mino, 11 fire_elemental, 12 lich. W 55 mapach jest 281 robaków, 635 nietoperzy, 237 szkieletów, 211 większych kościotrupów, 357 pełzaczy oraz po jednym wpisie typów 8–12.

Wersje 0.2–0.4 używały zastępczej grafiki szkieletu dla typu 2; nie był to potwierdzony identyfikator szkieleta. Od 0.5 skorygowano tę interpretację; 0.6 obsługuje pięć podstawowych typów. Model AI, obrażeń i rozmiarów kolizji nadal jest własnym prototypem, nie rekonstruowanym zachowaniem EXE. Arkusze podstawowych przeciwników mają różne wymiary klatek, m.in. Worm 64×25, Crawler 64×32, Bat 44×54, Minoskel 96×90 i Skel 90×105. Te wymiary wynikają z układu arkuszy, a nie ze wspólnego kafelka 80×80 postaci gracza.

## Animacje ANM — 0.7

Czytnik odczytuje sekcję ALL_FRAMES (nazwa, x,y,szerokość,wysokość), nazwane sekwencje ANIMATION, odwołania FRAME z czasem i LOOP. W archiwum są: boss_mino 21 klatek/13 sekwencji, boss_spider 14/8, boss_worm 10/6, fire_elemental 22/12, LICH 13/8, SPIDER 5/2. Brak czasu oznacza na potrzeby podglądu jeden krok. Przeliczenie kroków na czas rzeczywisty (domyślnie 20 na sekundę) jest robocze; oryginalny zegar nie został jeszcze potwierdzony. Zdarzenia ANM nie są wykonywane.

W oryginalnych danych LICH sekwencja kickright wskazuje nieistniejącą klatkę right_kick. SPIDER zawiera walk3 przy definicji wolk3 oraz born_1/born_2 przy definicjach born1/born2. Czytnik zgłasza nieistniejące odwołania; podgląd nie podmienia ich bez dowodu. Na Huawei pokazano oryginalną sekwencję WalkLeft boss_spider.

## Windy — 0.8

Znaczniki sześciobajtowe potwierdzono jako windy: typ 1 poziomy (35), typ 2 pionowy (144). Inicjalizacja VA 0x414990–0x414a9f używa struktur 0x26 bajtów: typ+0, kolumna+4, wiersz+8, x+12, y+16, poprzednie x/y+20/+24, kierunek u16+28, postój u16+30, faza u16+32, termin u32+34. Środek x=kolumna*64+32. Y poziomej=(wiersz+1)*64-1, pionowej=(wiersz+3)*64.

Aktualizacja VA 0x414b40–0x414f90: krok jednego piksela co 10 ms, kierunki 0=-1 i 1=+1, postój na końcu 2000 ms. Pozioma sprawdza flagę 0x4000 komórki na przednim końcu; pionowa wymaga typu komórki 0 i braku flagi 0x20. Oryginalny dodatkowy test 0x417aa0 chroniący przed kolizją z graczem nie jest jeszcze odtworzony. Kierunki pochodzą z tablic 0x49ed24 i 0x49ed0c. Blok silnika pionowej windy zmienia nakładkę według tablicy 0x49ed14: 42,41,40,43 (na razie nie stosowane w widoku). Początkowy termin jest losowany przez oryginał; nowy model stosuje stałe 2 s.

Renderowanie VA 0x4202a7–0x420740: pozioma ma prostokąty Animate (384,128)-(480,256) oraz (480,128)-(576,256), lewy górny punkt x-48,y-90; pionowa (256,256)-(320,368) oraz (352,256)-(416,368), punkt x-32,y-112. Lina pionowej pobierana z pasa x576..640,y128+faza i ciągnie się od (wiersz+1)*64 do y-102. Dwie warstwy otaczają postać w nowym widoku; dokładna kolejność innych obiektów pozostaje robocza.

Kolizja VA 0x415e90–0x416046: szerokość podparcia wynosi 96 lub 64, podłoga to odpowiednio y+32 lub y. Oryginał sprawdza bliskość stóp <4 px i przenosi postać poziomo o różnicę x. Własny model kolizji gracza używa tych płaszczyzn i przenosi także pionowo; pełna logika wnętrza kabiny wymaga dalszej analizy.

## Drzwi wyjściowe — 0.9

Funkcja 0x416fc0–0x4171e2 używa globalnego stanu 0x4b0474: 0 zamknięte, 2 otwieranie, 1 otwarte. Licznik 0x4b0470 zaczyna od 0x95=149 i maleje raz na wywołanie. Warunek normalnego otwarcia: trzy bajty kluczy w strukturze 0x4a89f8 +0x28..0x2a niezerowe, współrzędne gracza/64 w obszarze [exitX,exitX+2) x [exitY,exitY+2). Po otwarciu wyjście wymaga dodatkowo pola gracza+0x24 równego zero; nowy silnik interpretuje to jako wymaganie podparcia, bez pełnego dowodu semantyki pola. Oryginał zeruje flagi kluczy po animacji. Istnieje także odrębna ścieżka co dziesięć aktualizacji, gdy 0x417e50 zwróci licznik <=0; nie włączono jej, dopóki nie ma pełnej obsługi bossów.

Renderowanie 0x41f411–0x41f4d7 sprawdza flagę komórki 0x800. Bity 12..13 wyznaczają ćwiartkę drzwi. Pomocnik 0x41eb40 dodaje do indeksu kafelka tablicę 0x49edfc: 0,1,10,11 i pobiera kwadrat 64x64 z Animate, 10 kolumn. Stan zamknięty: baza 0. Otwarte: baza 24 lub 42, przełączana co cztery kroki. Otwieranie: 19-floor(licznik*20/150) indeksuje 0x49ee10: 40,2,40,2,40,2,40,2,40,2,40,2,40,2,4,6,8,20,22,24.

Dźwięk Sound/ExitDoor.wav jest ładowany przy 0x422e54 do uchwytu 0x4a672c i uruchamiany przy rozpoczęciu otwierania (0x41701c,0x417101). Nowy silnik stosuje oryginalne prostokąty i tablicę, lecz roboczo 60 kroków/s. Postęp animacji zapisuje jako float w prywatnym formacie v3. Własny bohater ma inne współrzędne kolizji; próba wejścia używa x+16,y+49, ponieważ dotychczasowa fizyka ustawia dolną granicę postaci dokładnie na linii podłogi.

## Przedmioty i punktacja — 0.10

Funkcja zbierania 0x4162b0 wywołuje 0x42eba0, która zwraca wartość tablicy int32 przy VA0x49f258 indeksowaną typem przedmiotu; wynik jest dodawany do pola+4 struktury0x4a89f8. Tabela dla0..68 została przepisana do ItemRules. Dyspozytor efektów używa tablic przy0x416684 i0x416640 dla typów1..68. Typ31 prowadzi do domyślnej gałęzi0x4165c4, zatem daje1300 punktów i nie leczy. Wcześniejszy opis31 jako serca był błędny.

Typy44/45/46 przy0x416405 wybierają5/10/2 i wywołują0x416d80, która dodaje do pola zdrowia gracza+0x20 oraz ogranicza wynik tabelą maksymalnego zdrowia0x49ebd8. Typ40 dodaje100 i ustawia timer+0x36 na500. Port przelicza wartości leczenia na swoją roboczą skalę0..5 (2/3/1 i pełne zdrowie); efekt dodatkowej ochrony40 nie jest jeszcze zaimplementowany.

Typy1/3/5/6 dodają5 do jednego z czterech pól int16 energii+0x10..0x16,2/4 po3 do dwóch pól; każdy kanał ograniczany do100 w pętli0x4165c4. Typy7/8/59/60/67/68 uruchamiają GoldChest.wav (uchwyt0x4a670c);41..43 zapisują flagi kluczy i uruchamiają KeyPickup.wav. Typy9..11 zapisują wybór broni+0x1b;52..54 wybór+0x1a;55..58 inkrementują liczniki;51 zwiększa pole+0. Ich pełne działanie pozostaje do odtworzenia. Nowy port rozdziela potwierdzone skarby i leczenie od nadal nieobsługiwanych typów wyposażenia.

## Środowisko: woda i śmiertelne komórki — 0.11

Funkcja0x40b6f0 pobiera komórkę przy kolumnie actor.x/64 i wierszu(actor.y-1)/64. Bit7 flag komórki (0x80) zwiększa licznik aktora+0x40 raz na aktualizację. Gdy osiągnie interwał+0x44, licznik wraca do0, interwał maleje o8 (jeżeli nie jest zerowy), zdrowie+0x20 maleje o1. Poza wodą licznik=0 i interwał=128 (0x40b8cc). Gracz typu1 dodatkowo traci flagę osłony+0x3c w wodzie (0x40b84f). Kod sprawdza poprzednią komórkę gracza do efektu plusku; efektu tego port jeszcze nie odtwarza.

Bit10 flag komórki (0x400) przy0x40b8e6 ustawia zdrowie na-1 i uruchamia dźwięk uchwytu0x4a6758. Nie zależy to od chwilowej odporności po trafieniu. Przykładowe rzeczywiste dane: mapa11, komórki(8..11,20), typ komórki0 i flaga0x400; mapa2, komórki(1..4,4), typ0 i flaga0x80.

EnvironmentWorld stosuje te flagi i interwały, roboczo60 aktualizacji/s. Ponieważ port nadal używa pięciu punktów zdrowia, cztery jednostki obrażeń wody składają się na jeden punkt; reszta jest zapisywana. Śmiertelna flaga ustawia0, zgodnie z konwencją śmierci portu. Próbka położenia jest przeliczona z własnej geometrii postaci na x+16,y+49. Zapis prywatny4 przechowuje ułamkowy zegar, licznik, interwał i resztę obrażeń. Nie jest to jeszcze pełne odtworzenie fizyki wody lub pozostałych zagrożeń.

## Podstawowe klatki bohatera — 0.12

Gnome1.pcx ma640x480, komórki80x80 i po cztery kolumny dla kierunku. Funkcja0x40aff0 oblicza prostokąty renderowania. Przy0x40b150 odczytuje tablicę0x49e4b8:0,1,2,3; dla kierunku6 stosuje3-faza, a początkowe przesunięcie x=0x140=320 dodaje cztery kolumny. Wspinanie przy0x40b41e używa0x49e4a8:0,1,2,1, wiersza y=160 i komórek80x80. Stan1 wybiera bazową pozę1 (dla kierunku6:2); stan3 bazową2 (dla kierunku6:1), co odpowiada pozom wznoszenia i opadania widocznym w arkuszu. Zależnie od dodatkowego bitu1 flag+0x4c oryginał zmienia też wiersze0/80/240/320. Znaczenie tego wariantu nie zostało jeszcze odtworzone.

Nowy HeroFrames stosuje wiersz0 dla podstawowego wariantu ruchu, właściwe grupy kierunków i wiersz2 dla drabiny. Własne stany ruchu portu nie odpowiadają jeszcze jeden-do-jednego oryginalnym stanom aktora. Zegar animacji jest teraz zatrzymany przez pauzę i podgląd mapy. Po zwolnieniu sterowania na drabinie postać zachowuje położenie; skok rozłącza ją z drabiną.

## Główna kampania i wynik — 0.13

Przejście w oryginale0x4156dc dodaje wartość0x4b0440 do bieżącego numeru0x4b046c. Porównanie z0x33=51 przy0x415752 kieruje do zakończenia zamiast ładowania kolejnej mapy. W głównej sekwencji jest50 map; bossowie występują w10/20/30/40/50. Oddzielne1001..1005 nie następują po50. Ich szczegółowe warunki wejścia nadal wymagają analizy.

W oryginalnej strukturze gracza wynik to pole+4 struktury0x4a89f8. Kopie stanu przy0x412d47 i odtwarzanie po śmierci0x415735–0x41574c obejmują zachowanie wyniku. Port przechowuje sumę skarbów z poprzednich map osobno od flag przedmiotów bieżącej mapy; zapis5 dodaje int32 wyniku wcześniejszych map oraz bool zakończenia. Starsze zapisy domyślnie mają0/false. To nie obejmuje jeszcze punktów za przeciwników i premii oraz oryginalnego systemu żyć.

## Boss robak — 0.14

Typ9 to boss_worm, rzeczywisty wpis mapy10 ma(type9,flags6,x992,y575). Tablica funkcji0x49eab8, krok24, wpis typu9:0x409b10,0x409730,0x409630,0x4096b0,0x409b50,0x409c30. Pomocniki409630/4096b0 zwracają szerokość/wysokość aktualnej ramki ANM przez401e60. Nowy port tymczasowo używa stałego prostokąta160x40.

Aktualizacja409730: stan0 patroluje o3px/wywołanie (przykład lewo40993b–409947). Gdy gracz z przodu jest w odległości>=128 i<384, przechodzi do stanu9 z licznikiem79 (40998d). Stan9 wybiera AttackLeft/Right, dekrementuje licznik i przy60 tworzy pocisk typu25 (409a2c–409a87) przy połowie aktualnej szerokości oraz y-15. Przy0 wraca do stanu0 i ustawia kolejną możliwość ataku na globalny czas+50+losowa wartość. Nazwy animacji Dead/Walk/Attack wynikają z literałów w tej funkcji oraz oryginalnego ANM. Stan4 i6 obejmują dodatkową fazę śmierci i repozycjonowania, której port jeszcze nie odtwarza.

Port odtwarza patrol, zasięg, przygotowanie i moment strzału przy roboczych60 krokach/s. Obrażenia, wytrzymałość i pocisk należą nadal do własnego modelu CombatWorld. Wymiary trafienia uwzględniają szerokość bossa w ataku wręcz i pociskach. Otwieranie drzwi bez kluczy następuje po pokonaniu wszystkich aktywnych przeciwników na mapie z robakiem, zgodnie z oddzielną ścieżką licznika przeciwników opisaną przy drzwiach.

## Pocisk robaka i klatka pokonania — 0.15

Tabela parametrów pocisków0x49e0e8 ma rekordy24 bajty. Typ25: pierwszy dword5 (obrażenia w dolnym int16), kolejny512 (budżet drogi w int16), float10 pod+8 (prędkość na aktualizację), flaga1 pod+12, pozostałe0. Konstruktor402a30 kopiuje te pola odpowiednio do+0x18,+2,+0x1a pocisku. Dyspozytor konstruktora40319c, wpis25 prowadzi do403106: vy=0, vx=±prędkość według kierunku2/6.

Aktualizacja403210 dodaje vx/vy do pozycji raz na wywołanie. Przy40333a–403358 zmniejsza budżet zasięgu o wartość prędkości (minimum1). Dyspozytor405457, wpis25 prowadzi do40347e; grafiki odczytuje z uchwytów4a60c0/c4. Te uchwyty ładowane są przy4028d8/4028ec z literałów48e6bc/48e6c8: zelman_r i zelman_l, ramki wSHOTS.FRM. Od0.15 port wybiera te grafiki i600px/s przy roboczych60Hz, z czasem życia512/600s.

Trafienie bossa409b50 odejmuje obrażenia pocisku od zdrowia bez dodatkowej reakcji nieśmiertelności. Gdy zdrowie<=0, ustawia stan4 i timer; funkcja aktualizacji409730 wybiera DeadLeft/Right. Port pokazuje teraz te klatki po pokonaniu, lecz nie rekonstruuje całego długiego timera ani odrodzenia na innych mapach.

## Minotaur — 0.16

Typ10 to boss_mino (mapa20). Tablica funkcji:408cf0,408820,408710,4087a0,408d30,408e10. Aktualizacja408820 w stanie0 przemieszcza postać o3px/wywołanie (40898b lub408a1a), stan3 wybiera Fall i przesuwa y o4px/wywołanie. Atak uruchamia pomocnik408e70 po odliczeniu globalnego timera4a624c; ustawia stan9 i licznik79, kolejny odstęp losuje100..500 kroków. Stan9 kieruje postać ku graczowi i wybiera KickLeft/Right. Przy liczniku60 pętla408b68–408ba6 tworzy8 pocisków typu27 pod kątami12+45*i, i=0..7, z pozycji przed postacią i y-15.

Typ27 w tabeli49e0e8 ma obrażenia3, zasięg512 i float prędkości4. Konstruktor402b69 wylicza vx/vy z kąta; port stosuje wektor cos/sin*240 przy roboczych60Hz. Dyspozytor aktualizacji405457 prowadzi dla27 do4033bf. Dwie klatki wybiera co16 kroków; uchwyty4a60a8/ac i4a60b0/b4 ładowane są jako skull_r1/r2 oraz skull_l1/l2 przy402914..402950. Nowy model renderuje je z oryginalnego SHOTS.FRM.

Stan12 oryginału obejmuje dodatkową sekwencję przywołania (408c27 i408ef0), niezaimplementowaną w porcie. Własny model stosuje uproszczoną grawitację i deterministyczne3s oczekiwania, kolizję90x118 oraz40HP. ANM dostarcza Walk, Kick, Fall i Dead. Otwieranie drzwi po pokonaniu rozszerzono na mapy z typem10.

## Korekta nominalnego zegara — 0.17

Wcześniejsze60Hz było założeniem, nie ustaleniem z EXE. Pętla4236e0 pobiera czas przez422a70, zapisuje go pod4b247c. Przy42370b–423721 porównuje czas od poprzedniej aktualizacji z1000/tablica49ef50[ustawienie4b2450]; tablica zawiera55,65,75,1000. Wartość ustawienia3 omija ograniczenie. Inicjalizacja4238da wpisuje domyślnie2, czyli75. Funkcja ekranowa41ecb0 wywołuje415510 (41ece5), która zwiększa licznik kroków4b2438 (415655..41565e).

Zatem aktualizacja oryginału jest powiązana z ograniczaniem klatek, a nie potwierdzonym stałym zegarem60Hz. Ponieważ dzielenie jest całkowitoliczbowe, dla75 próg wynosi13ms; wydajność i ustawienia mogą zmieniać rzeczywiste tempo. Nowy OriginalClock przyjmuje stabilne nominalne75Hz jako punkt odniesienia. Nie należy interpretować tego jako emulacji wszystkich wariantów zegara oryginału.

Od0.17 przeliczenia: bossy3px/krok=225px/s; robak10px/krok=750px/s; minotaur4px/krok=300px/s; przygotowanie79/75s, wystrzał po19/75s; drzwi149/75s; klatki czaszek zmieniają się co16 kroków, bohatera co8. Licznik oddechu także używa75 kroków/s. Zegar wind1px/10ms pozostaje niezależny. Pozostałe własne wartości fizyki i czasów oczekiwania nie zyskały przez tę korektę dowodu zgodności.

## Oryginalna skala zdrowia — 0.18

Tabela int32 przy49ebd8 dla typów0..12:0,20,5,10,50,75,15,25,1000,500,1000,750,800. VitalStats stosuje ją bez przeskalowania dla aktywnych aktorów. Gracz ma20HP. EnvironmentWorld usuwa jeden punkt przy każdym oryginalnym interwale wody, zamiast zbierać ćwiartki punktów. ItemRules leczy oryginalnymi wartościami5/10/2 oraz pełnym zdrowiem dla40.

Typ1 pocisku rysowany jest przy405014 przez uchwytybt_right/left; tabela49e0e8 daje obrażenia10, drogę256 i prędkość8/krok. Typ3 przy4050c1 używa uchwytów4a61ac/b0, załadowanych jako crawler_right/left; parametry4,512,10. Obie wartości zostały włączone do CombatWorld. Typy25/27 używają już obrażeń5/3 w oryginalnych jednostkach. Własne obrażenia kontaktowe4 i wręcz8 nie mają jeszcze dowodu zgodności.

Zapis6 zawiera ten sam układ pól co5, lecz liczby HP są w nowej skali. Wersje1..5 konwertowane są jednorazowo podczas walidacji: HP gracza*4-reszta wody, HP przeciwnika=ceil(stareHP*noweMax/stareMax). Stare maksima wynosiły2 dla podstawowych typów,4 dla minoskel,20 dla robaka i40 dla minotaura. Przed przeliczeniem sprawdzane są zakresy. Pozostałe pola, w tym flagi przedmiotów i wynik, nie są skalowane.

## Kontakt i przechwytywanie pocisków — 0.19

Gałąź416d04–416d23, przy braku osłony aktora+0x3c, przekazuje stałe5 do funkcji obrażeń40b5b0 (typ gracza1) lub40d610 (drugi bohater). Wbrew początkowemu przypuszczeniu podstawowe obrażenia kontaktowe tej ścieżki są wspólne, nie zależne od typu przeciwnika. Port poprawiono z4 na5. Pełna osłona i stan trafienia nie są jeszcze odtworzone.

Funkcja4054d0 porównuje prostokąty pocisków różnych stron na podstawie flagi bit1 pola+0x46. Przy4055e4..405626 zachowuje obie pierwotne siły int16(+0x18) i odejmuje je wzajemnie. Flaga bit2 może chronić jedną stronę przed odjęciem; aktywne w porcie typy1/3/25/27 nie mają tej flagi w tabeli. Pocisk o sile<0 jest niszczony (40562a i405673); równe siły mogą pozostawić dwa strzały0. Nowy model zachowuje ten warunek i nie zadaje obrażeń strzałem0.

CombatWorld najpierw przemieszcza pociski i usuwa ich ważność po ścianie, następnie porównuje pary przeciwnych stron, a potem rozpatruje aktorów. To adaptacja do podkroków portu, nie identyczna kolejność pierwotnej pętli. Pozostała siła jest używana przy trafieniu zamiast ponownego przyznawania pełnych10HP pociskowi maga.

## Ochrona i stan trafienia — 0.20

Kontakt40b5b0 ustawia stan gracza+0x24=5 i licznik+0x28=20. Aktualizacja40b620 dekrementuje go, a po wyzerowaniu ustawia stan3 oraz uint16+0x36=150 (40b646–40b659). Odpowiednik drugiego bohatera ma tę samą wartość przy40d573. Timer+0x36 maleje raz na aktualizację (40a3eb–40a401 oraz40c209–40c21f). Funkcja kontaktu416ac0 odrzuca kolizję dla stanu5, niezerowego+0x36 lub stanu4 (416ae5..416b04).

Przedmiot40 przy4163c8 dodaje100HP z ograniczeniem do maksimum i ustawia+0x36=500. To inny mechanizm niż energetyczna osłona+0x3c. Port stosuje 170 nominalnych kroków niewrażliwości po trafieniu jako sumę obu faz oraz500 kroków po przedmiocie40. Nie odtwarza jeszcze odpychania i ograniczeń sterowania fazy20 kroków. Wspólne pole CombatWorld.immunity opisuje pozostałe sekundy; zapis7 dodaje float tego stanu po polu zakończenia. Starsze zapisy domyślnie mają0 i nadal podlegają migracji zdrowia dla wersji<6.

## Pająk i pajęczyna — 0.21

Typ8, mapa30: funkcje409510,4090c0,408fc0,409040,409550,409600. Aktualizacja4090c0 patroluje o4px/wywołanie (40926e i40933c). Gdy cel jest przed postacią w odległości32..384 i jego pole+0x48 jest0, uruchamia stan9 z licznikiem29 (40939e). Przy liczniku1 tworzy pocisk26 (409436..409491), po0 wraca do patrolu i ustawia kolejną możliwość ataku za10 kroków. Stan9 wybiera ShotRight/Left; stan4 DeadSpider. Niepełne fazy odrodzenia są pominięte w porcie.

Pocisk26 ma w tabeli49e0e8 siłę1, drogę1280 i prędkość10/krok. Render403429 używa uchwytu4a60b8 załadowanego z nazwy Web (48e6d4), ramka120x106. Co drugi krok, o ile siła!=0, dodaje1.0 do vy (40343c..40345a). Port przybliża to przyspieszeniem0.5*75*75px/s². Trafienie40b4f0..40b540 ustawia licznik gracza+0x48=100, zeruje prędkość i siłę pajęczyny, przywiązuje jej położenie do gracza; nie odejmuje zatem1HP. Licznik gracza maleje przy40a408..40a41a, a warunki40a549,40a55c i40af2a blokują odpowiednie aktualizacje sterowania/akcji.

Nowy model przechowuje CombatWorld.webbed w sekundach, blokuje ruch, skok i atak, lecz aktualizuje pozostały świat. Ochrona przed trafieniami może zapobiec związaniu. Pocisk nie jest dalej aktualizowany jako osobna przywiązana instancja; efekt graficzny rysuje się na graczu przez czas licznika. Zapis8 dodaje pozostały czas po float ochrony; wersje1..7 domyślnie0.

## Fire elemental actor (0.22)
Type 11 update 0x407570, health 750, frame 80x80, patrol 3 pixels per tick. Port includes actor, original animation, damage, checkpoint and door unlock. Projectile and special state 12 remain unimplemented. Confirmed pending attack: 0x407c00 enters state 9 with 79 ticks, random delay 100..500. At remaining 60, creates three type-9 shots (indices 0,1,2), damage 5 halved to 2. Constructor 0x4030c1 uses tables 0x49e388/0x49e3a8, x=(0,1,1.4), y=(-1.4,-1,0), multiplied by speed 4. Range512. Special state12 calls0x407c80.

## Fire elemental cast (0.23)
Implemented original 79-tick cast, firing at remaining60, aim-facing during cast, three type9 shots from center x / feet-25. Constructor4030c1 uses indices0,1,2 with no facing transform: velocities (0,-5.6),(4,-4),(5.6,0) pixels per tick. Original 4a614c frame cache initialized4023c9 onward resolves mis_8(12x30),mis_9(25x23),mis_6(30x12). Original damage5 is halved to2 by4078fa..407909. Random cast delay100..500 based407bd4/407c4a (Java upper bound exclusive); original RNG sequence is not reproduced. Range512 is represented by lifetime512/(4*75), consistent with scalar-speed budget but not yet verified against original range update. Special state12, flame particles, sounds and exact patrol/falling remain incomplete. Attack timers/projectiles are not checkpointed, same as other bosses.

Pending teleport evidence: 0x407c80 selects random column in [1,width-1), floor row in [2,height-1), repeats until selected cell type1 and cell above type0; then original center x=col*64+32, feet y=(row-1)*64+63. 0x407ba0 initializes special delay random100..300. State12 resets special delay100..1300 and attack delay5. Port has not yet implemented teleport.

## Fire elemental teleport (0.24)
Implemented pending special state from407697..40769a, next update relocation per407c80..407d35. Uniformly selects col1..width-2,row2..height-2 with cell type1 and above type0; original center=col*64+32,feet=row*64-1. Reservoir selection gives same eligible-cell distribution as original rejection loop and terminates when custom maps have no candidate (original would loop). Initial cooldown100..299 patrol ticks from407bc0; reset100..1299 from4079d5; next cast cooldown4 ticks after reset5 and helper decrement. Timer is paused while casting/airborne; original state boundaries approximated by grounded check. Does not reproduce original RNG sequence, particles, sounds or exact physics. Timer/pending state not yet checkpointed; position is.

## Checkpoint version9 (0.25)
Appends six float32 values per existing enemy after version8 fields: vertical velocity, turn cooldown, nonnegative attack cooldown, attack time, teleport cooldown, teleport pending as0/1. Captures all supported enemy types. Version1..8 remain readable and keep constructor timing defaults; new saves restore exact captured timing and motion. Validates finite values and timing ranges before applying states. This is port checkpoint behavior, not an original game format. Active projectiles and hero motion/attack timers remain unsaved.

## Lich (0.26)
Type12 dispatch at49eab8+11*24: update407e60, frame geometry407d50/407de0; original ANM92x105 and health800. Patrol3 pixels per tick. Cast helper4085c0 uses79 ticks and random100..499 delay; fire at remaining60 per40815c. At firing4081a8 randomly chooses3 patterns: eight type27 skulls angle12+45*i, origin center +/-frame width/2,feet-15; three type9 missiles same fixed directions and halved damage2 as fire elemental; four type14 icicles from viewport top at random x in viewportX+10 ..viewportX+width-11, y=viewportY, horizontal speed0, downward7..10 pixels/tick. Constructor402e12..402e65, table14 damage15/range640/base speed7, frame cache4a6138 from4024cd string icicle, FRM15x36. Port uses current960-pixel viewport; range represented by scalar lifetime640/(7*75), original depletion still not fully traced.
Teleport408640 mirrors fire elemental floor selection; initial100..299 ticks and subsequent100..1299, quick next cast4 ticks after helper. Existing version9 checkpoint now permits type12 teleport fields. Old checkpoints for map50 with zero recorded foes are incompatible with newly instantiated boss and require restarting that map. Original lich ANM has missing right_kick frame, and EXE requests FallLeft/Right absent from ANM; port uses Walk during falls rather than inventing frames. Exact fall/patrol, respawn/death phase, particles/sounds and active projectile checkpoint remain incomplete.

Map50 contains one lich plus eight bats. Current prototype exit logic waits until all active foes in the boss arena are defeated, not just the lich. Original final-exit timing is not yet traced.

## Original boss hit handlers (0.27)
Lich408440 subtracts projectile damage, then40846f..40847f reduces special cooldown to5 ticks if larger. Repeated hits cannot postpone an earlier teleport. Fire elemental407a80 rejects projectile kinds9,22,5,20 (407aa1..407acf), doubles kind24 damage (407ae4..407aec), subtracts otherwise, and does NOT reduce teleport cooldown. Earlier general wording about hit-triggered boss teleports must not be read as applying to both bosses. Port now routes melee/projection damage through one hit handler; prototype melee retains damage8/kind0. Only supported projectile semantics are changed; unavailable weapons are not claimed playable. Death/respawn/particles and exact original melee remain incomplete.

## Four energy reserves and base wizard cost (0.28)
Original initializer411ed8 zeroes player structure;411fa2..411fbe gives wizard25 in channel0, selected spell0. Warrior initialization gives25 in channel1, spell1 (not yet a separate port inventory). Item dispatch416684 byte indices /416640 targets: item1->4164b2(+5 channel2),2->416573(+3 channels1/3),3->41647b(+5 channel1),4->416520(+3 channels0/2),5->4164ea(+5 channel3),6->416445(+5 channel0). Loop4165c4 caps each at100. Spell table49e400 records14 bytes; spell0 costs(1,0,0,0).40b98c checks all costs before subtracting40b9c2..40b9fc. Port implements base spell0 energy cost and pickups, HUD, campaign carry, checkpoint10 appended4 int32. Versions1..9 default to25,0,0,0; already collected old pickups are not re-awarded. Character toggle remains provisional shared inventory. Other spell costs/selection are not yet implemented. Melee remains provisional and does not consume energy. HUD channel order is internal original order; elemental names/colors not yet verified.

## Chapter energy reset (0.29), correction to0.28 carry
Loader412c70 checks unsigned levelNumber modulo10==1 at412ca2..412cb3, clears four ability-level bytes and energy words412cce..412ceb, sets wizard channel0 to25 and selected spell0 (412ced..412d09), or warrior channel1 to25 and selected spell1 (412d11..412d24). Therefore original energy does not carry unchanged from10->11,20->21,30->31,40->41. Port now applies this energy reset after copying campaign state for a new chapter; normal maps retain all reserves. Same numeric rule includes1001. Current port does not yet have ability-level bytes/selection or separate hero inventory, so only verified energy portion is implemented. Manual map preview still initializes trial state independently; checkpoint restore does not reset saved energy.
Spell-selection helper417690 checks ability level by channel and tier before selecting. Item34 handler417ef0 creates original password/checkpoint information, not a spell unlock. How normal progression grants ability levels remains unresolved; do not invent pickup unlocks. Original spell table49e400 has12 records of14 bytes; first4 costs(1,0,0,0),(0,2,0,0),(0,0,2,0),(0,0,0,1).

## Energy regeneration (0.30)
Wizard update40a382..40a3bb tests global actor tick4b2438 modulo128==0 and, when channel0<25, increments it by1. Warrior40c198..40c1d1 does the same for channel1. Port now accumulates nominal75Hz ticks while live game updates, regenerates active hero channel every128 ticks, caps automatic refill at25 without reducing pickups above25. Stops when dead and freezes under pause/overview/completion. Actor-global tick phase is approximated by per-map energy clock; renderer/init delay is not exactly reproduced. New checkpoint11 appends float32 phase[0,128), old versions default0. Regeneration phase resets with per-map trial world construction; campaign energy transfer retains amounts except original chapter resets.
Ability unlock research: no verified normal grant mechanism yet. Disassembly contains data decoded as instructions (for example40fe91 and42fe81 are stack-check metadata, not ability increment code); ignore those false positives. Initial level bytes, selection gating and password restoration are verified, but unlock pickups must not be inferred without further evidence.

## Power orbs and first additional spells (0.31), correction to earlier research
Original Help/help1.txt explicitly describes Power Orbs and three levels per orb. Item55..58 handler4163ae computes player+itemType-43, so increments byte offsets12..15: these are ability levels, NOT consumable counters. Earlier descriptions calling55..58 consumables were wrong. Port now collects all4 orbs, increments matching level as uint8, preserves levels/selection, and resets on original chapter boundary. Original selection417690 requires level[channel]>=tier+1. First3 supported spell indices0/1/2 select projectile1/5/8; costs from49e400 are1/2/2 in channels0/1/2. Unsupported spell indices3..11 are not offered yet even if level exists.
Projectile5: damage13,range384,speed8, constructor403106 horizontal; render404fa1 reads fbr_1..3/fbl_1..3 every4 ticks (original global tick, port age). Frame58x56 except right frame3 width57. Projectile8: damage12,range384,speed8, constructor403106; render404d3f uses3 frames per4 age ticks via4a61f8(right) /4a6228(left), nameslightning6_1..3/lightning4_1..3; original FRM rectangles identical62x30. Spawn offsets and cadence still use prototype player attack, not complete original casting state.
Checkpoint12 appends4 uint8 ability levels and int32 selected spell after phase. Older saves default initial known spell for saved character. Already-passed uncollected orbs remain in original maps; no retroactive awards. Shared character inventory, fourth channel attack, advanced tiers, original spell-selection grid and active projectile persistence remain incomplete. Footer CZAR cycles currently supported unlocked spells when tapped.

## Fourth basic wizard spell (0.32)
Spell index3 at40bb28..40bb72 creates one projectile6, horizontal constructor403106, x offset half actor frame width, y=feet-20-random(20); spell cost49e400 index3 is(0,0,0,1). Type6 table damage7,range448,speed10, flags1; callback405ce0 produces type254 visual effect, not extra damage. Port implements projectile, original randomized y offset relative to hero feet, damage/range/speed/cost, unlock via orb58 and selection. Spawn x retains prototype24 offset. Impact particles from413d90 are NOT yet implemented.
Render404f2c chooses original bte_right/bte_left then bte_r2/bte_l2 every4 ticks. Dimensions63x17 first,64x17 second right,64x16 second left. Original uses global tick; port uses projectile age.
Original wizard attack timer40af52..40afd5: idle requires timer>spell table delay, attack phase ends after timer>15. Spell2 sets timer6 at40baf1, spell3 sets12 at40bb2b. Nominal repeat periods implemented: spell0/1=25 ticks,spell2=19,spell3=6. Hero attack pose/state itself remains provisional. Checkpoint12 layout unchanged; app now accepts selected spell3. Advanced spell indices4..11 remain unavailable.

## Dźwięk i muzyka (ustalenia z EXE)

Ładowanie 36 próbek: VA 0x422d50..0x422fa1, uchwyty 0x4a66f8..0x4a676c. Tylko Walk i Climb mają drugi argument 1 (0x422f1a, 0x422f2e). Odtwarzanie przez 0x4447d0.

| Funkcja | Próbki |
|---|---|
| 0x4162b0 (przedmioty) | GoldChest, Clock, KeyPickup, DiamodPickup |
| 0x416fc0 (drzwi) | ExitDoor |
| 0x414b40 (windy) | LiftCreak, LiftCreakRev |
| 0x40b6f0 (woda) | WaterSplash, fireball |
| 0x40a5d0, 0x40c170 | Walk |
| 0x40ab50, 0x40ca00 | Climb |
| 0x40b940 | BoulderBlow/Start, charge, Icicle, StarShield, Tornado, bt_magic, fireball, waterarrow |
| 0x40d760 | FireSickle, Thunder, axe |
| 0x40dbc0 | Burn, Hammer, SeekStar, Whip, charge |
| 0x407e60, 0x408820 | Teleport, crawler_fire |
| 0x4090c0, 0x409730, 0x409cc0 | crawler_fire (0x4090c0 także skel_born) |
| 0x40def0, 0x40e810 | skel_born, skel_throw |

Powiązanie funkcji z dokładnym zdarzeniem wymaga analizy każdego miejsca; opisy w nawiasach pochodzą z wcześniejszych ustaleń.

Muzyka poziomu (0x413367): numer mapy podzielny przez 10 daje boss_theme.it, pozostałe wybierają przez tabelę 0x41360f według motywu z offsetu 360 (globalna 0x4b05e0, wartość 0 zastępowana 1): 1=GNOM3, 2=GNOM2, 3=GNOM7, 4=GNOM9, 5=GNOM8. Blok pomijany, gdy 0x4b0460 jest niezerowe; znaczenie tej flagi nieustalone. happy_end.it: 0x427b37; gnom2.it także przy 0x42ed11. Argument odtwarzacza ustawia częstotliwość 22050 Hz lub 44100 Hz (0x4241ab, 0x4241cf).
## Ruch bohatera (0x40a5d0..0x40af03)

Pola aktora: +0xc kierunek, +0x10 x środka, +0x14 y stóp, +0x24 stan (0 ziemia, 1 skok, 2 drabina, 3 spadanie, 4 śmierć), +0x28 pozostały budżet skoku, +0x40 licznik wody. Oryginał nie ma grawitacji ani przyspieszeń, ruch to stałe piksele na krok: chód 3, w wodzie 2 (0x40a6ed); wznoszenie 4, w wodzie 2, budżet 120 (0x40a916, 0x40aa56), puszczenie skoku przechodzi w spadanie (0x40ab30); spadanie 4, w wodzie 2 (0x40ae12); drabina 2, z wyrównaniem x do środka kolumny (0x40a87d). W powietrzu sterowanie jak na ziemi. Lądowanie ustawia stopy na row*64-1 (0x415e20). Prawo ma pierwszeństwo przed lewem. Port stosuje te prędkości; kolizje nadal używają własnego prostokąta portu zamiast szerokości z 0x414fa0/0x414ff0. Druga postać ma wspólne wartości 0x78 i wspinanie 2 (0x40c7a6, 0x40c818), lecz fragment 0x40c95e..0x40c9c4 nie jest jeszcze w pełni odczytany.
Rozmiar kolizji bohatera: szerokość 70, wysokość 75 (0x40a300, 0x40a330). Sonda boczna (0x415980/0x415ae0): kolumna środek + przesunięcie + 3/8 szerokości, wiersze stopy, połowa, głowa; każda komórka musi mieć typ 0. Sonda sufitu (0x415c40): środek, stopy - wysokość. Drugi bohater podczas wznoszenia porusza się w poziomie o 4 px na krok, w wodzie 3 (0x40c95e). Podłoże portu nadal używa własnej szerokości.

## Czary i ataki bohaterów (0x40b940, 0x40d760)

Tabela 0x49e400: 12 rekordów po 14 bajtów (koszty czterech kanałów, opóźnienie, flaga), tabela drugiego bohatera 0x49e4c8. Bloki 0x40b940 w kolejności kodu tworzą typy pocisków: 1, 5, 8, 6, 10, 9×3, 7, 11, 14×7, tarcza 500 kroków lub pocisk 9, 13×5 po okręgu, wstrząs. Kolejność przypadków wynika z układu kodu; tabela skoków 0x40c0da nie została odczytana. Koszty 4..11: (5,0,0,0), (0,3,0,0), (0,0,5,0), (0,0,0,5), (7,0,0,0), (0,3,0,0), (0,0,7,0), (0,0,0,7). Podstawowy atak drugiego bohatera (indeks 1, 0x40d928): bez kosztu energii, typ pocisku 4 (35 obrażeń, zasięg 16, tabela 0x49e0e8), przerwa 10 kroków, dźwięk axe. Port stosuje 35 obrażeń i 10 kroków; zasięg ciosu pozostaje własny.
Wybór czaru (0x417690): identyfikator = kanał + poziom*4; dostępny, gdy poziom kanału >= poziom+1 (0x417768). Tabela skoków 0x40c0da potwierdza kolejność czarów. Port ma czary 0..4; czar 4 to tornado (typ 10, koszt 5, obrażenia 1, zasięg 1920, prędkość 6, klatki torn_1..4). Czy tornado przebija cele, nie ustalono; przerwa powtórzenia przyjęta jak dla czaru 0, bo tabela podaje tę samą wartość 8.

Czar 8 (sople, 0x40bcaf, konstruktor typu 14 przy 0x402e12): 7 pocisków, x = lewa krawędź ekranu + 10 + losowo(szerokość - 20), y = góra ekranu, prędkość pionowa 7 + losowo(0..3), obrażenia 15, zasięg 640. Typ 13 krąży po promieniu, typ 9 używa tabel kierunków 0x49e388/0x49e3a8, a tarcza ustawia +0x3c=500; te czary nie są jeszcze w porcie.

Podłoże bohatera (0x415ce0): dwa punkty środek ± szerokość/8 przy stopy+1; komórka poza mapą, typ 1 lub flaga 0x20 podtrzymuje. Z tarczą także flaga 0x400. Trzymany skok startuje ponownie po wylądowaniu (0x40a8b0); blokada przy bicie 0x10 pola +0x4c i globalnej 0x4b0c80 nie jest jeszcze przeniesiona.
