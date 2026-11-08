# Testy 0.7

APK 0.7 z targetSdk 35 zainstalowano na Huawei VNS-L21 z zachowaniem oryginalnych danych i checkpointu. Ponowiono test urządzenia po zmianie targetSdk i dodaniu podglądu animacji: wszystkie dziewięć sprawdzeń przeszło (oryginalny import, dwa palce: ruch + atak, zwolnienie przycisków, ponowienie po śmierci, odtworzenie pozycji/zdrowia/pauzy). Poprzedni checkpoint przywrócono.

Lokalne testy czytników, walki i checkpointów przeszły. Dekoder wszystkich 114 PCX nadal zgadza się z Pillow. AnimationTest sprawdza geometrię klatek, czas trwania, pętle, zatrzymanie na końcu, niepoprawne zakresy i nieistniejące odwołania. Odczytano sześć oryginalnych ANM, z jawnym wykazem ostrzeżeń dla LICH i SPIDER w reader-tests.txt.

Ręcznie na Huawei otwarto listę animacji, wybrano boss_spider i przełączono na WalkLeft. Podgląd odtwarza oryginalną grafikę z przezroczystością (huawei-animation-preview-07.png). Log AndroidRuntime bieżącego procesu nie zawierał błędów. Nie testowano pełnej kampanii, wydajności długiej sesji, AI bossów ani zgodności zegara animacji z EXE.
