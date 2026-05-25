# RilevamentoDati

App Android per salvare localmente le perizie quando non c'e' connessione.

## Prima versione

La schermata permette di salvare:

- data perizia, generata automaticamente dalla data/ora del dispositivo;
- targa;
- telaio;
- modello.
- una o piu' foto associate alla perizia, scattate al momento o scelte dalla galleria.

I dati vengono salvati in un database locale Room/SQLite. Ogni record nasce con stato `DA_INVIARE`, cosi' in una fase successiva potremo aggiungere l'invio verso un server che scrivera' su SQL Server.

## Navigazione

Dopo il login l'app mostra un menu laterale con:

- `Perizie`, con lista compatta e pagina dedicata per creare, modificare, salvare o cancellare una perizia.
- `Trasferimento dati`, con il riepilogo degli stati e il comando per trasferire le perizie da inviare.
- `Chiudi app`, per uscire dall'applicazione.

## Login configurato

Questa versione richiede l'accesso operatore prima della schermata perizie.

- Codice: `C001`
- Password: `Alesi`
- Operatore: Fabio Vezina

## Come aprire il progetto

1. Apri Android Studio.
2. Seleziona `Open`.
3. Apri questa cartella:

   `D:\Codex\CourseManager-master\RilevamentoDati`

4. Lascia completare la sincronizzazione Gradle.
5. Avvia l'app su emulatore Android o telefono fisico.

Se Android Studio chiede quale JDK usare, scegli quello incluso in Android Studio.

## Nota

In questa macchina non risultano configurati Gradle e Android SDK da riga di comando, quindi la compilazione va eseguita da Android Studio.
