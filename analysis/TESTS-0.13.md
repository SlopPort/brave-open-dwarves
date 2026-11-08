# Testy 0.13

Huawei VNS-L21 $DEVICE, Android10/API29: 31 kontroli urządzenia przeszło. Cztery nowe: zachowanie wyniku i zdrowia pomiędzy mapami, zapis wyniku, zakończenie na mapie50 bez wejścia do1001 i zapis zakończenia. Raport huawei-013-device-tests.txt. Testy wywołują mechanizm przejścia bez rozgrywania bossów; nie stanowią dowodu możliwości ukończenia gry. Wcześniejszy prywatny zapis przywrócono.

RunStateTest sprawdza wynik i zakończenie w formacie5 oraz domyślne pola przy odczycie formatu4; migracje1/2/3 zachowano. Pozostałe czytniki i testy mechanizmów nadal przechodzą.

Port zachowuje zdrowie i wybraną postać na przejściu. Wynik zbieranych przedmiotów jest sumą poprzednich map i flag zabranych przedmiotów na obecnej mapie. Przejście automatycznie zapisuje nowy poziom. Ręczne strzałki wyboru mapy rozpoczynają osobną próbę z wynikiem0. Mapy1001..1005 nadal dostępne są przez wybór map, ale nie są dopisywane do końca głównej kampanii.

Ograniczenia: brak AI bossów i oryginalnych zasad przejść bonusowych, premii końcowych, tabeli rekordów, liczby żyć oraz pełnego oryginalnego ekranu zakończenia. Starsze zapisy nie zawierają sumy wcześniejszych map, więc zaczynają z wynikiem poprzednich map0. Punktacja wrogów również nie została jeszcze wdrożona.
