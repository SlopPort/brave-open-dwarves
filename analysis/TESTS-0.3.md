# Testy 0.3 — Huawei P9 Lite

Kapitan Pazur (pl.pazur.port) został zatrzymany zgodnie z prośbą użytkownika. Testy wykonano wyłącznie na Huawei VNS-L21, serial $DEVICE.

APK 0.3 zainstalowano z zachowaniem importowanego Gnomes.dat. Urządzenie potwierdza versionCode 3 i versionName 0.3.0. W rozgrywce na Level0001 potwierdzono ruch szkieleta, obrażenia kontaktowe, atak maga, pokonanie jednego szkieleta (licznik 1) i pauzę. Zrzut: huawei-combat-03.png.

Testy Java: odczyt 244 zasobów, 114 PCX i 55 poziomów, 18 odrzuconych uszkodzonych wejść. CombatTest sprawdza atak wręcz, kierunek ataku, odstęp między atakami, pociski i pojedyncze naliczenie pokonanego, obrażenia, nietykalność, śmierć, ograniczenie patrolu ścianami i limit leczenia. Wszystkie przeszły.

Nie porównano modelu walki z oryginalnym EXE. Na telefonie nie sprawdzono pełnego przejścia poziomu ani wszystkich sytuacji walki wojownikiem. Pozostałe typy przeciwników, zachowania broni i pułapek wymagają implementacji. Pocisk maga oraz ślad ataku wręcz są tymczasowo rysowane kodem, nie pochodzą z arkuszy oryginalnej broni.
