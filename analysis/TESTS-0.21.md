# Testy 0.21

Huawei VNS-L21 $DEVICE:46 kontroli przeszło. Nowe: pająk na mapie30, odblokowanie wyjścia po pokonaniu, blokada ruchu przez pajęczynę i zapis pozostałego czasu. Raport huawei-021-device-tests.txt. Dotychczasowe pliki zapisu przywrócono.

BossTest sprawdza krótki atak pająka, wystrzelenie typu26, unieruchomienie bez utraty HP, brak ataku podczas związania i wygaśnięcie efektu. RunStateTest obejmuje odczyt formatu7 z domyślnym brakiem związania. Poprzednie testy nadal przechodzą.

Pająk używa oryginalnych boss_spider.pcx/anm, Walk, Shot i DeadSpider. Maksymalne zdrowie1000 pochodzi z tabeli. Pocisk Web pochodzi z SHOTS.FRM (120x106); po związaniu rysowany jest na graczu. Zapis8 dodaje float pozostałego czasu pajęczyny. Wcześniejsze zapisy mają0. Stare zapisy mapy30 bez bossa wymagają ponownego rozpoczęcia mapy ze względu na nowy licznik przeciwników.

Ograniczenia: stała kolizja pająka186x122, własna grawitacja i uproszczone pozostałe stany. Opadanie pocisku jest ciągłym przybliżeniem impulsu co drugi krok. Brak odradzania i wszystkich wariantów animacji. Pierwotna pozycja pocisku odnosi się do dynamicznej ramki; port używa stałego przesunięcia93px. Pozostali dwaj bossowie nadal nie są aktywni.
