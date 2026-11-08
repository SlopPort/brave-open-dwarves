# Testy 0.4–0.6 — Huawei P9 Lite

Wszystkie testy urządzenia kierowano wyłącznie do VNS-L21 / $DEVICE, LineageOS Android 10. Kapitan Pazur został wcześniej zatrzymany na prośbę użytkownika.

## Testy lokalne

Czytnik przechodzi wszystkie 244 zasoby, 55 map i 114 obrazów. Układ każdej mapy kończy się dokładnie na ostatnim bajcie. Pozycje wszystkich przeciwników są wewnątrz map. Dekoder PCX porównano z Pillow piksel po pikselu. Testy odrzucają 19 uszkodzonych wejść, w tym nieprawidłową ramkę FRM.

CombatTest obejmuje atak wręcz i jego kierunek, cooldown, pociski i naliczanie pokonanych, obrażenia, odporność czasową, śmierć, ściany, krawędzie, pocisk powstający przy ścianie i jego blokowanie nawet przy dużym kroku czasu, lot nietoperza, wysokość robaka, pocisk pełzacza oraz większe zdrowie kościotrupa. RunStateTest sprawdza zapis i nadpisanie pliku, odzyskanie kopii po przerwaniu, tożsamość danych, nieprawidłowe współrzędne, urwanie pliku i odrzucenie stanu martwej postaci. Wszystkie przeszły.

## Testy ręczne przez ADB

Sprawdzono instalacje 0.4, 0.5 i 0.6 z zachowaniem importu. Na urządzeniu potwierdzono śmierć i przycisk PONÓW, zapis podczas powrotu do menu, odtworzenie pozycji i zdrowia po zatrzymaniu procesu, pauzę po powrocie z ekranu głównego. Zrzuty: huawei-death-04.png, huawei-resume-04.png, huawei-checkpoint-restored.png. Po skorygowaniu tabeli typów wyświetlono właściwe robaki i nietoperze na mapach 1 i 2 (huawei-map-05.png).

## Test integracyjny na urządzeniu, APK 0.6

Oddzielny pakiet org.braveopendwarves.tests z targetSdk 35 i tym samym kluczem podpisu. Pierwsza wersja z targetSdk 28 została odrzucona przez Play Protect; podniesienie targetSdk pozwoliło na normalną instalację. Ochrony systemowej nie wyłączano.

Wynik:

```
PASS original archive remains imported
PASS two-finger movement advances hero
PASS second pointer holds attack
PASS attack fires while moving
PASS releasing fingers clears movement and attack
PASS retry restores health after death
PASS checkpoint restores position
PASS checkpoint restores health
PASS restored game starts paused
```

Test korzysta z prawdziwych zdarzeń dotykowych dwóch palców i procesu aplikacji na Huawei. Dla izolacji testu wejścia usuwa przeciwników z testowej sceny; test ponowienia tworzy nową scenę. Przed uruchomieniem zachowuje prywatny checkpoint, a po zakończeniu odtwarza poprzedni plik. Nie oznacza to przejścia poziomu ani odtworzenia całego silnika.

Nie wykonano długiej sesji, pełnej kampanii, odsłuchu efektów, testów bossów i niezaimplementowanych pułapek ani porównania zachowania walki z oryginalnym programem.
