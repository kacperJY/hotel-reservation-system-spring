# Hotel Reservation System - Backend

Zaawansowany, produkcyjny silnik backendowy dla systemu rezerwacji hotelowych, zaprojektowany z naciskiem na wysoką wydajność operacji bazodanowych, bezpieczeństwo danych oraz architekturę sterowaną zdarzeniami (Event-Driven). Projekt stanowi doskonałą demonstrację nowoczesnych możliwości języka **Java 25** oraz ekosystemu **Spring Boot 3.x**.

Aplikacja implementuje kompletny proces biznesowy: od dynamicznego zarządzania dostępnością pokoi na rok w przód, przez bezpieczne okno rezerwacji dla Gości, aż po dedykowane operacje menedżerskie i automatyczne procesy czyszczenia zasobów w tle.

## 🚀 Stos Technologiczny

* **Język programowania:** Java 25 (wykorzystanie nowoczesnych konstrukcji, m.in. Pattern Matching dla Switch expressions)
* **Framework:** Spring Boot 3.x (Spring Web, Spring Security, Spring Data JPA)
* **Bezpieczeństwo:** JSON Web Token (JWT), Row-Level Security (SpEL)
* **Baza danych:** PostgreSQL
* **Infrastruktura kontenerowa:** Docker & Docker Compose
* **Zarządzanie zależnościami:** Maven

## 🏗️ Kluczowe Rozwiązania Architektoniczne i Wydajnościowe

1.  **Optymalizacja Pamięci JVM w Zadaniach w Tle (Chunk-Based Cron Job):**
    Proces automatycznego anulowania nieopłaconych rezerwacji realizowany jest o 3:00 w nocy za pomocą adnotacji `@Scheduled`. Aby zapobiec wyciekom pamięci sterty (`OutOfMemoryError`) przy milionach rekordów, wdrożono autorski mechanizm porcjowania danych (*Chunking* przy stałym offsecie zerowym) połączony z ręcznym zarządzaniem pamięcią podręczną pierwszego poziomu (`entityManager.flush()` oraz `entityManager.clear()`). Przetworzone encje są na bieżąco usuwane z L1 Cache, dzięki czemu zużycie RAM-u przez Garbage Collector pozostaje na stałym, minimalnym poziomie.
    
2.  **Zabezpieczenie przed Atakami IDOR (Row-Level Security):**
    Zarządzanie zasobami przez Menedżerów oraz Adminów zostało w pełni odizolowane. Dostęp do operacji na rezerwacjach w danym obiekcie jest weryfikowany na poziomie zapytania bazodanowego oraz deklaratywnej autoryzacji `@PreAuthorize` z użyciem SpEL. System uniemożliwia modyfikację lub podgląd danych hotelu, do którego zalogowany użytkownik nie posiada jawnych uprawnień pracowniczych.
    
3.  **Architektura Sterowana Zdarzeniami (Publish-Subscribe Pattern):**
    Moduł katalogu obiektów (`RoomService`) jest całkowicie odsprzężony od modułu dostępności. Utworzenie nowego pokoju skutkuje wyemitowaniem asynchronicznego zdarzenia systemowego. Komponent nasłuchujący przechwytuje event i automatycznie generuje siatkę dostępności (wiersze w tabeli `RoomAvailabilityEntity`) na 365 dni do przodu.
    
4.  ** Kontrola przepływu stanów rezerwacji (State Machine Flow):**
    Przejścia pomiędzy statusami rezerwacji (`PENDING` -> `CONFIRMED` -> `IN_PROGRESS` -> `COMPLETED`/`CANCELLED`) są rygorystycznie kontrolowane przez wewnętrzny silnik walidacyjny, uniemożliwiając nielogiczne modyfikacje stanu rezerwacji. Każde przejście w stan terminalny (`CANCELLED`) automatycznie i atomowo zwalnia zablokowane dni w kalendarzu przy użyciu mechanizmu *Dirty Checking*.
    
5.  **Zapobieganie Iloczynom Kartezjańskim (JPA Performance Tuning):**
    Pobieranie relacji złożonych z bazy danych zostało zoptymalizowane za pomocą dedykowanych grafów encji (`@EntityGraph`), eliminując problem zapytania N+1 oraz niekontrolowane złączenia wielokrotne generujące iloczyny kartezjańskie na bazie PostgreSQL.
    
6.  **Ustandaryzowana Obsługa Błędów:**
    Aplikacja posiada globalny komponent `@ControllerAdvice`, który przechwytuje wszelkie wyjątki biznesowe oraz błędy walidacji (np. z pakietu `jakarta.validation`) i tłumaczy je na rygorystyczny standard **RFC 7807 Problem Detail**, zwracając czytelne, ujednolicone obiekty JSON dla klienta API.

7. **Bezpieczna Integracja z Zewnętrznym API (Virtual Threads & Cache-Aside):** 
   Moduł wielowalutowy komunikuje się z API NBP za pomocą interfejsu RestClient opartego na Wątkach Wirtualnych, eliminując blokowanie wątków systemowych (OS threads) przy operacjach I/O. Aby uniknąć opóźnień sieciowych i blokad typu Rate Limiting, wdrożono wzorzec Cache-Aside. Kursy synchronizują się asynchronicznie w tle (CRON @Scheduled), a zapytania użytkowników obsługiwane są bezpośrednio z szybkiej pamięci RAM (@Cacheable).



## 📦 Struktura Projektu (Główne Pakiety)

```text
pl.kacper.reservation.hotelReservationSystem
│
├── catalog              # Domena hotelu, pokoi i siatki dostępności (JPA Entities)
├── guest                # Logika biznesowa i endpointy dedykowane dla Gości
├── manager              # Panel menedżerski, statystyki i zarządzanie rezerwacjami obiektu
├── admin                # Operacje administracyjne systemu (tworzenie obiektów, pokoi)
├── user                 # Zarządzanie użytkownikami, uwierzytelnianie i role (ROLE_GUEST, ROLE_MANAGER, ROLE_ADMIN)
├── repositories         # Warstwa dostępu do danych (Spring Data JPA Repositories)
├── schduledServices     # Zadania automatyczne i CRON jobs (czyszczenie bazy, batching)
├── exception            # Globalna obsługa wyjątków i mapowanie do RFC 7807
└── seeder               # Zasilacz danych (DatabaseSeeder) aktywowany profilem "dev"
```

🛠️ Instrukcja Uruchomienia (Deployment & Run)

Projekt został zaprojektowany z myślą o maksymalnej ergonomii pracy inżynierskiej (Developer Experience) oraz gotowości do wdrożeń produkcyjnych. Wykorzystuje wbudowane wsparcie Spring Boota dla Docker Compose, oferując dwa całkowicie odseparowane środowiska uruchomieniowe sterowane profilami Springa.
Wymagania wstępne

Aby uruchomić projekt, upewnij się, że w Twoim systemie zainstalowane są:

    Java 25 (JDK)

    Docker (wraz z uruchomionym demonem / Docker Desktop)

    Maven (opcjonalnie, projekt wspiera skrypt mvnw)

💻 Opcja 1: Środowisko Deweloperskie (Profil dev)

Idealne do aktywnego rozwoju kodu, debugowania w IDE i testowania zapytań. W tym trybie aplikacja uruchamia się natywnie na hoście (np. z poziomu IntelliJ), a Spring Boot automatycznie zarządza cyklem życia bazy danych w tle.

    Sklonuj repozytorium i otwórz projekt w IDE.

    Upewnij się, że demon Dockera działa.

    Uruchom klasę główną aplikacji. * Spring Boot samodzielnie wykryje plik compose.yaml, pobierze obraz PostgreSQL i podniesie kontener z bazą danych na losowym/zmapowanym porcie (Service Connections), automatycznie wstrzykując dane dostępowe do puli HikariCP.

    Zasilanie danymi (Seeder): Profil dev automatycznie uruchamia komponent DatabaseSeeder, który wypełnia pustą bazę danych przykładowymi hotelami, pokojami i rezerwacjami, umożliwiając natychmiastowe testowanie API (np. przez Postmana).

🐳 Opcja 2: Środowisko Kontenerowe / Produkcyjne (Profil prod)

Tryb przeznaczony do wdrożeń i testowania finalnego obrazu aplikacji w wyizolowanej architekturze sieciowej Dockera. Zarówno baza danych, jak i sama aplikacja Java uruchamiane są jako osobne kontenery w zamkniętej sieci main-net.

Krok 1: Konfiguracja zmiennych środowiskowych
W głównym katalogu projektu utwórz plik db.env oraz app.env (zgodnie z przykładowymi plikami z postfix .example). W adresie DB_URL hostem jest nazwa serwisu bazy danych w sieci Dockera (psql)

Krok 2: Zbudowanie i uruchomienie klastra
Uruchom aplikację za pomocą Docker Compose, wskazując profil prod:

```bash
docker compose --profile prod up -d --build
```

Procesy wykonawcze:
 - Demon Dockera zbuduje  obraz aplikacji przy użyciu techniki Multi-stage build (separacja etapu pobierania zależności Mavena, kompilacji środowiska JDK). 
 - Wykorzystany mechanizm depends_on: condition: service_healthy zagwarantuje, że kontener aplikacji (Spring) zostanie uruchomiony dopiero, gdy kontener PostgreSQL będzie w pełni gotowy na przyjmowanie połączeń. 
 - Profil **prod** dezaktywuje DatabaseSeeder, chroniąc środowisko produkcyjne przed nadpisaniem przypadkowymi danymi testowymi.
