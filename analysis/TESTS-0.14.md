# Testy 0.14

Huawei VNS-L21 $DEVICE, Android10/API29: 34 kontrole przeszły. Nowe sprawdzają pojedynczego bossa typu9 na mapie10, odblokowanie wyjścia po ustawieniu jego stanu pokonania i odtworzenie tego stanu z zapisu. To test integracji stanów, nie ręczne pokonanie bossa. Prywatne pliki użytkownika przywrócono. Raport huawei-014-device-tests.txt.

BossTest sprawdza przygotowanie ataku przy odległości128..384, zatrzymanie podczas ataku, wystrzał, zadawanie obrażeń bronią białą i śmierć, początek animacji otwarcia oraz odczyt rzeczywistego bossa z mapy10. Poprzednie testy nadal przechodzą.

Na ekranie telefonu obejrzano mapę10 z dużym robakiem, oryginalnymi klatkami WalkLeft i paskiem zdrowia (huawei-boss-014.png). Grafika pobierana jest z prywatnego boss_worm.pcx, klatki z boss_worm.anm, bez kopiowania zasobów do APK.

Ograniczenia: zdrowie bossa20 jest roboczym przeliczeniem oryginalnych500 do modelu portu. Stała szerokość kolizji160/wysokość40, pocisk z grafiką pełzacza, prędkość pocisku240 i stały czas oczekiwania1s pozostają uproszczone. Brak oryginalnego odrodzenia/repozycjonowania, pełnej fazy śmierci, zdarzeń i dźwięku. Pozostali czterej bossowie nadal nie są aktywni w rozgrywce. Timery ataku nie są zapisywane (jak u pozostałych przeciwników); zapisane są pozycja, kierunek i zdrowie. Starszy zapis mapy10 bez wpisu bossa wymaga ponownego rozpoczęcia mapy z powodu zmiany liczby przeciwników.
