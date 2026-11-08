# Testy 0.9

Huawei VNS-L21, $DEVICE, Android 10/API29: APK 0.9 zainstalowano z zachowaniem importu. Instrumentacja przeszła 16 kontroli: poprzednie 13 oraz oczekiwanie na otwarcie drzwi, odtworzenie postępu animacji z zapisu i przejście na mapę 2. Plik huawei-09-device-tests.txt. Istniejący zapis użytkownika przywrócono po teście.

DoorTest sprawdza trzy klucze, granice obszaru 2x2, tablicę klatek i części drzwi, zakończenie animacji oraz wymóg podparcia postaci. RunStateTest obejmuje teraz stan drzwi, odrzucenie NaN i odczyt dawnych formatów zapisu v1/v2. Wszystkie wcześniejsze czytniki i testy walki, ANM oraz wind nadal przechodzą.

Na ekranie telefonu potwierdzono prawidłowe złożenie zamkniętych drzwi z czterech kafelków na mapie 1 (huawei-door-09.png). Oryginalny ExitDoor.wav jest odczytywany z prywatnego archiwum do pamięci podręcznej i odtwarzany przy rozpoczęciu otwierania; nie potwierdzono odsłuchu fizycznego głośnika.

Ograniczenia: tempo 60 kroków/s jest robocze. Nie zaimplementowano jeszcze otwierania drzwi bossów po usunięciu przeciwników. Klucze zachowywane są do zmiany mapy, podczas gdy oryginał zeruje ich flagi po animacji. Format zapisu v3 zachowuje postęp drzwi; starsze zapisy rozpoczynają drzwi od stanu zamkniętego. AI bossów, pełna kampania i zgodność fizyki pozostają nieukończone.
