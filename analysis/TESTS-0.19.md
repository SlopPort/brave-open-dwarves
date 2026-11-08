# Testy 0.19

Huawei VNS-L21 $DEVICE:40 kontroli przeszło, w tym dwie nowe: przechwycenie wrogiego pocisku z redukcją siły własnego i5HP obrażeń przy kontakcie z przeciwnikiem. Raport huawei-019-device-tests.txt. Zapis użytkownika przywrócono po testach.

ProjectileTest sprawdza przechwycenie10 kontra5 (pozostaje5), zadanie pozostałych5HP przeciwnikowi, zderzenie równych sił do0, brak obrażeń od strzału0 oraz brak przechwytywania pocisków tej samej strony. Czytniki i dotychczasowe testy nadal przechodzą.

Pociski mają teraz zmienną pozostałą siłę. Ruch i zderzenie ze ścianą poprzedza etap przechwytywania, następnie rozpatrywane są trafienia aktorów. Rozmiary aktywnych rodzajów pochodzą z ramek65x34 (mag/robak),30x12 (pełzacz),38x31/33 (czaszki). Kolejność faz nie emuluje jeszcze dokładnie całej pętli EXE; przy dużym kroku modelu pociski mogą minąć się pomiędzy próbkami, lecz widok rozgrywki używa podkroków<=1/120s.

Ograniczenia: walka wręcz8HP i odporność1.4s nadal robocze. Pełne flagi specjalnych pocisków, osłony, odpychanie i reszta broni nie są odtworzone. Pociski nadal nie są zapisywane; powrót do zapisu czyści aktywne strzały.
