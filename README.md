# AAAntylogout

AAAntylogout to plugin antylogout dla Paper, ktory karze wyjscie podczas walki, zapisuje historie bitew graczy i blokuje ucieczki przez komendy, itemy, teleportacje oraz regiony.

## Funkcje

- Tagowanie PvP po uderzeniu gracza albo trafieniu pociskiem.
- Kara za logout w walce: smierc, drop itemow i komunikat na serwerze.
- Historia walk w `battle-history.yml`: czas trwania, przeciwnicy i zakonczenie (`timeout`, `death`, `logout`, `admin`, `server_stop`).
- Komenda administracyjna `/aaalo` z aliasami `/antylogout`, `/al`, `/aaalog`, `/alo`.
- Blokada komend w trybie blacklist albo whitelist.
- Blokada itemow, teleportacji, wyrzucania itemow i elytry podczas walki.
- Ochrona regionow z WorldGuard, jezeli jest zainstalowany.
- Wlasne lokalne regiony w configu, gdy nie chcesz lub nie masz WorldGuard.
- Bossbar z pozostajacym czasem walki.
- Gotowy config i metadane pod publikacje na Modrinth.

## Wymagania

- Paper 1.21+
- Java 21
- WorldGuard opcjonalnie

## Instalacja

1. Zbuduj plugin komenda `mvn clean package` w folderze `AAAntilogout`.
2. Wrzuć plik `AAAntylogout-1.0-SNAPSHOT.jar` z folderu `target` do folderu `plugins`.
3. Uruchom serwer, edytuj `plugins/AAAntylogout/config.yml` i wykonaj `/aaalo reload`.

## Komendy

| Komenda | Opis |
| --- | --- |
| `/aaalo help` | Lista komend administracyjnych |
| `/aaalo reload` | Przeladowuje konfiguracje |
| `/aaalo status <gracz>` | Pokazuje aktywna walke gracza |
| `/aaalo history <gracz> [strona]` | Pokazuje historie walk |
| `/aaalo stats <gracz>` | Pokazuje statystyki walk |
| `/aaalo end <gracz>` | Konczy walke gracza i przeciwnikow |

## Uprawnienia

| Permission | Opis |
| --- | --- |
| `aaantylogout.admin` | Dostep do komend administracyjnych |
| `aaantylogout.bypass` | Gracz nie jest tagowany i omija ograniczenia |
| `aaantylogout.bypass.commands` | Gracz moze uzywac blokowanych komend w walce |

## Regiony

W configu ustawiasz liste nazw w `regions.blocked`. Jezeli WorldGuard jest dostepny, plugin sprawdza regiony WorldGuard o tych nazwach. Bez WorldGuard mozesz uzyc `regions.local-regions`, gdzie region ma nazwe, swiat i granice `min` oraz `max`.

## Build

```bash
cd AAAntilogout
mvn clean package
```

## Licencja

MIT. Szczegoly w [LICENSE.md](LICENSE.md).
