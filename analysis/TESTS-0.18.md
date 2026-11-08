# Testy 0.18

Huawei VNS-L21 $DEVICE: 38 kontroli przeszło po zmianie skali zdrowia. Powtórzono je po korekcie pocisku pełzacza. Raport huawei-018-device-tests.txt. Sprawdzono m.in. odnowienie20HP po śmierci, leczenie przedmiotem45 o10 i ograniczenie do20. Dotychczasowy prywatny zapis przywrócono po testach.

Lokalne testy walki pracują teraz na oryginalnych maksymalnych HP aktorów. Zmieniono sprawdzenia obrażeń i leczenia zamiast pozostawiania dawnych oczekiwań skali0..5. RunStateTest dodaje migrację zapisu5:4 dawne HP z resztą2 stają się14/20, szkielet1/2 staje się25/50, a ponowna walidacja nie przelicza drugi raz. Zachowano migracje1..4. Pozostałe testy nadal przechodzą.

Zapis6 używa oryginalnych jednostek zdrowia. Dawne zapisy przeliczane są podczas walidacji: gracz*4 minus reszta obrażeń od wody, przeciwnicy proporcjonalnie do nowego maksimum z zaokrągleniem w górę. Zera nie stają się dodatnie. Nieprawidłowe stare zakresy są odrzucane przed mnożeniem. Ograniczenia starszych zapisów map10/20 bez wpisu bossa pozostają aktualne.

Wdrożone parametry: gracz20; worm5, bat10, skel50, minoskel75, crawler15, boss_worm500, boss_mino1000. Leczenie44/45/46=5/10/2,40 do pełnego zdrowia. Woda zabiera1HP na interwał. Pocisk maga typ1: obrażenia10, droga256, prędkość8/krok; pełzacza typ3:4/512/10; robaka typ25:5/512/10; minotaura typ27:3/512/4. Tempo nominalne75Hz.

Ograniczenia: kontakt z przeciwnikiem nadal roboczo4HP, atak wręcz8HP. Nie odtworzono pełnej odporności, broni, energii i stanów trafienia. Wymiana wartości zdrowia nie dowodzi pełnej zgodności walki. Pozostali trzej bossowie nadal wymagają implementacji.
