# Testy 0.20

Huawei VNS-L21 $DEVICE:42 kontrole przeszły. Nowe sprawdzają rzeczywisty przedmiot40 z mapy2 (pełne20HP i500 kroków ochrony) oraz odtworzenie dokładnie3.25s pozostałej ochrony. Raport huawei-020-device-tests.txt. Prywatne pliki użytkownika przywrócono po teście.

CombatTest kontroluje czas170/75s po trafieniu. ItemTest kontroluje500 kroków przedmiotu40 oraz brak takiej ochrony dla zwykłego leczenia45. RunStateTest sprawdza zapis ochrony i odczyt zapisu6 bez tego pola. Pozostałe testy nadal przechodzą.

Zapis7 dodaje float pozostałej ochrony. Wersje1..6 mają domyślnie0, ponieważ nie zapisywały tego stanu. Odtworzenie nowego zapisu nie przyznaje już sztucznych1.4s ochrony. Poprzednie konwersje jednostek zdrowia nadal działają.

Ograniczenia: port łączy20 kroków stanu trafienia i150 kroków późniejszej ochrony w jeden przedział niewrażliwości170 kroków. Nie odtworzono jeszcze przesunięcia postaci w stanie trafienia, ograniczeń sterowania ani całej animacji tego stanu. Przedmiot40 używa ochrony500 kroków, ale odrębna energetyczna osłona pola+0x3c i jej działania nie są jeszcze obsługiwane. Woda i śmiertelne pola nadal ignorują ten licznik, jak ich odrębne ścieżki w oryginale.
