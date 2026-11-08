# Testy 0.11

APK0.11 zainstalowano na Huawei VNS-L21, serial $DEVICE, Android10/API29. 23 kontrole urządzenia przeszły: poprzednie20 oraz śmiertelna komórka na mapie11 pomimo odporności, rozpoczęcie licznika oddechu w wodzie mapy2 i odtworzenie licznika/interwału/częściowych obrażeń z zapisu. Raport huawei-011-device-tests.txt. Istniejący zapis użytkownika jest przywracany na końcu testów.

EnvironmentTest sprawdza śmierć od flagi0x400, pierwszy interwał pod wodą128 kroków, zmniejszanie kolejnych interwałów o8, przeliczenie czterech oryginalnych jednostek zdrowia na jeden punkt portu, reset oddechu na powietrzu, dalsze tonięcie, odtworzenie stanu i odrzucenie NaN. RunStateTest obejmuje migracje z formatów1/2/3. Czytniki, walka, ANM, windy, drzwi i przedmioty nadal przechodzą testy.

Format zapisu4 dodaje cztery liczby stanu oddechu. Dawne zapisy domyślnie zaczynają z odnowionym oddechem. Starsze ograniczenie odczytu zapisów1 na mapach z windami pozostaje aktualne.

Ograniczenia: 60 kroków/s to roboczy zegar, a zdrowie portu nadal ma skalę0..5 zamiast oryginalnej0..20. Częściowe obrażenia od wody są zachowywane jako reszta0..3 i zerowane przy leczeniu. Nie odtworzono odpychania, spowolnienia, animacji i efektów wody, wszystkich ruchomych pułapek ani AI bossów. To nie jest jeszcze weryfikacja przejścia całej kampanii.
