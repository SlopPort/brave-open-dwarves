# Testy 0.17

Huawei VNS-L21 $DEVICE: 38 kontroli przeszło, w tym nowa kontrola konwersji czasu drzwi (0.05s =3.75 nominalnego kroku). Raport huawei-017-device-tests.txt. Dotychczasowy zapis użytkownika przywrócono.

DoorTest sprawdza75 kroków po1s. EnvironmentTest liczy interwały w1/75s. BossTest sprawdza teraz nominalne750px/s pocisku robaka i300px/s salwy minotaura. Wszystkie dotychczasowe testy przechodzą. Testy potwierdzają zastosowanie konwersji w porcie; ustalenie domyślnego limitu pochodzi z instrukcji EXE opisanych wFORMAT.md.

Zmiana zastępuje robocze60Hz w odtworzonych mechanizmach: drzwi, oddech, podstawowe klatki bohatera, oba bossy i ich pociski. Własne stałe fizyki gracza i zwykłych przeciwników nie zostały przez to potwierdzone. Podgląd ANM ma nadal własne ręcznie zmieniane tempo. Windy pozostają na osobnym kroku10ms.

Oryginał domyślnie wybiera limit75fps, ale używa całkowitoliczbowego progu1000/75=13ms, ma też55/65 i tryb bez limitu. Rzeczywiste tempo zależy od ustawień i komputera. Port stosuje stabilne nominalne75Hz jako punkt odniesienia, nie emuluje niedokładności zegara Windows ani wszystkich możliwych ustawień. Format zapisu nadal5; liczniki zapisane w krokach zachowują znaczenie, lecz upływ czasu jest teraz przeliczany nowym zegarem.
