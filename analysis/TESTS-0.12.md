# Testy 0.12

Huawei VNS-L21 $DEVICE, Android10/API29: APK0.12 zainstalowano, import i wcześniejszy zapis zachowano. Przeszło27 sprawdzeń: poprzednie23 plus utrzymanie na drabinie po zwolnieniu sterowania, odtworzenie tego położenia, zatrzymanie zegara animacji podczas pauzy i odskok od drabiny. Raport huawei-012-device-tests.txt. Prywatne pliki zapisu przywrócono po teście.

HeroFramesTest sprawdza klatki postoju, cztery klatki na kierunek, odwróconą kolejność ruchu w lewo, pozy wznoszenia/opadania i zakres trzech klatek wspinania. Pozostałe testy czytników, walki, zapisu, ANM, wind, drzwi, przedmiotów i środowiska nadal przechodzą.

Obejrzano lokalny oryginalny arkusz Gnome1.pcx (640x480). Poprzednia implementacja dodawała8 do indeksu przy ruchu w lewo, co przełączało wiersz, zamiast właściwej grupy czterech kolumn. HeroFrames rozdziela kierunek i fazę. Część wyboru stanu dopasowano do własnej fizyki portu, więc nie stanowi to pełnej zgodności oryginalnego systemu animacji.

Ograniczenia: bazowy wariant animacji używa wiersza0; oryginał ma dodatkowe warianty zależne od flag broni/aktora. Brak pełnych animacji ataku, śmierci i pływania. Tempo8 klatek/s jest robocze. Trzymanie drabiny jest poprawką własnej fizyki, a odtworzenie na drabinie wynika z położenia w komórce drabiny (format zapisu nadal4), nie z zachowania pełnej maszyny stanów EXE.
