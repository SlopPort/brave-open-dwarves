# Testy 0.15

APK0.15 zainstalowano na Huawei VNS-L21 $DEVICE. Ponownie przeszły34 kontrole urządzenia, w tym odczyt oryginalnego archiwum z nowo używanymi ramkami zelman_r/zelman_l. Raport huawei-015-device-tests.txt. Zapis użytkownika przywrócono. Nie dodano nowych kontroli do instrumentacji.

BossTest rozszerzono o typ25, prędkość600px/s i budżet zasięgu512px pocisku robaka. Wszystkie czytniki i wcześniejsze testy nadal przechodzą. Pociski robaka nie używają już grafiki crawler_right/left. Boss pozostaje na mapie w oryginalnej klatce DeadLeft/Right po pokonaniu, a pasek życia znika. Kierunek ataku pozostaje stały podczas przygotowania i wystrzału.

Prędkość została przeliczona z10px/aktualizację przy nadal roboczym zegarze60Hz. W oryginalnej tabeli pocisk ma5 jednostek obrażeń; port nadal używa jednego punktu w modelu pięciopunktowego zdrowia, więc obrażenia nie są jeszcze identyczne. Trwałość pocisków w zapisie i pełna faza śmierci/odrodzenia bossa pozostają nieodtworzone. Pozostali czterej bossowie nadal wymagają implementacji.
