# Testy 0.16

Huawei VNS-L21 $DEVICE: 37 kontroli przeszło. Trzy nowe sprawdzają instancję minotaura na mapie20, odblokowanie wyjścia po ustawieniu wszystkich przeciwników jako pokonanych i zapis tego stanu. Raport huawei-016-device-tests.txt. Testy nie rozgrywają całej walki; wcześniejszy prywatny zapis przywrócono.

BossTest sprawdza przygotowanie niezależnej od odległości salwy minotaura, osiem pocisków typu27, prędkość wektorową240px/s, kierunki pionowe obu znaków i odczyt bossa z mapy20. Dotychczasowe testy nadal przechodzą. Pociski obsługują teraz ruch w dwóch osiach i badanie ścian na całym odcinku ruchu.

Na telefonie obejrzano oryginalny arkusz i animację minotaura w mapie20, z prawidłowym położeniem nad platformą (huawei-mino-016.png). Zasoby boss_mino.pcx/anm i skull_r1/r2/l1/l2 są odczytywane z oryginalnego archiwum użytkownika.

Ograniczenia: brak przywoływania przeciwników, oryginalnej losowości i pełnych przejść stanów. Własny model używa40HP zamiast oryginalnych1000, stałej kolizji90x118 i stałego odstępu3s między atakami. Strzały zadają obrażenia według modelu portu. Animacje wybierane są z oryginalnego ANM, ale tempo60Hz jest robocze. Starszy zapis mapy20 bez bossa wymaga ponownego rozpoczęcia mapy. Pozostali trzej bossowie nie są jeszcze aktywni.
