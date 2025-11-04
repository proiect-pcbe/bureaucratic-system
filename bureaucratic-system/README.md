# Sistem Birocractic - Simulator

Aplicatie Java multi-threaded care simuleaza un sistem birocractic in care clientii trebuie sa obtina diverse documente de la diferite institutii publice.

## 📋 Despre Proiect

Acest proiect simuleaza procesul birocractic, unde:
- **Clientii** trebuie sa obtina documente specifice (ex: Pasaport, Permis de conducere)
- **Documentele** au dependente intre ele (ex: pentru a obtine un Pasaport, trebuie mai intai sa ai Carte de identitate, Cazier judiciar si Certificat fiscal)
- **Institutiile** emit anumite tipuri de documente si au mai multe ghisee de lucru
- **Ghiseele** servesc clientii in ordine, avand diferite statusuri (DISPONIBIL, PAUZA, INCHIS)

Simularea foloseste **multi-threading** pentru a gestiona cereri concurente de la clienti si operatiuni ale institutiilor, demonstrand concepte precum sincronizarea thread-urilor, rezolvarea dependentelor si procesarea bazata pe grafuri.

## 🎯 Ce Face Aplicatia

Aplicatia simuleaza un scenariu real:
1. Se creeaza **clienti** care vor sa obtina diverse documente
2. Fiecare client primeste un document tinta (ex: Pasaport)
3. Sistemul calculeaza automat **calea de dependente** - ordinea documentelor necesare
4. Clientii merg la institutiile corespunzatoare, in ordinea corecta
5. La fiecare institutie, clientii stau la **coada** pana sunt serviti la un ghiseu liber
6. Ghiseele proceseaza clientii (simuland timpul de lucru real)
7. Dupa ce obtin documentul, clientii merg la urmatoarea institutie
8. Procesul continua pana cand toti clientii obtin documentul dorit

## 🏗️ Structura Proiectului

```
src/main/java/org/example/
├── Main.java                   
├── config/
│   └── ConfigLoader.java        
├── graph/
│   └── DependencyGraph.java     
└── model/
    ├── Client.java              
    ├── Config.java              
    ├── Counter.java             
    ├── CounterStatus.java       
    ├── DocumentType.java        
    └── Office.java              
```

## 🔄 Cum Functioneaza

### Rezolvarea Dependentelor

Clasa `DependencyGraph` (Graf de Dependente):
1. Construieste un graf orientat cu dependentele dintre documente
2. Detecteaza dependente circulare (care ar bloca sistemul)
3. Genereaza ordinea corecta de obtinere a documentelor folosind parcurgere topologica

### Fluxul unui Client

1. Clientul primeste un document tinta (ex: Pasaport)
2. Sistemul calculeaza automat calea: Certificat Nastere → Viza Rezidenta → Carte Identitate → Cazier → Certificat Fiscal → Pasaport
3. Clientul viziteaza fiecare institutie in ordine
4. La fiecare institutie, clientul intra in coada
5. Un ghiseu disponibil il serveste pe client
6. Clientul primeste documentul si merge la urmatoarea institutie
7. Procesul se repeta pana obtine documentul final

### Functionarea Ghiseelor

Fiecare ghiseu ruleaza ca un thread separat cu statusuri:
- **OPEN** (Deschis): Pregatit sa serveasca clienti
- **COFFEE_BREAK** (Pauza de cafea): Temporar indisponibil (25% sansa random)
- **CLOSED** (Inchis): Institutia se inchide

## ⚠️ PROBLEME DE CONCURENTA GASITE SI REZOLVATE

### 1. **Race Condition la Accesul Documentelor Obtinute**
**Problema**: Cand un client primeste un document, multiple thread-uri (ghisee) ar putea incerca sa modifice lista de documente obtinute simultan.

**Solutie**: 
- In `Client.java`: folosire `ConcurrentHashMap.newKeySet()` pentru `obtainedDocuments`
- Metoda `receiveDocument()` este `synchronized` pentru a preveni accesul concurent

```java
private final Set<String> obtainedDocuments = ConcurrentHashMap.newKeySet();

public synchronized void receiveDocument(String documentName) {
    obtainedDocuments.add(documentName);
    notifyAll();  // Trezeste thread-ul clientului care asteapta
}
```

### 2. **Deadlock Potential la Asteptarea Documentelor**
**Problema**: Un client asteapta un document, dar daca notificarea se pierde, clientul ar ramane blocat forever.

**Solutie**: Pattern wait/notify cu verificare in bucla
```java
synchronized (this) {
    while (!obtainedDocuments.contains(docName)) {
        wait();  // Asteapta notificare
    }
}
```

### 3. **Thread-Safety la Coada de Clienti**
**Problema**: Multiple ghisee incearca sa ia clienti din aceeasi coada simultan.

**Solutie**: 
- In `Office.java`: folosire `BlockingQueue<Client>` (LinkedBlockingQueue)
- Operatii `put()` si `take()` sunt thread-safe by design

```java
private final BlockingQueue<Client> globalWaitingQueue = new LinkedBlockingQueue<>();

public void assignClient(Client client) throws InterruptedException {
    globalWaitingQueue.put(client);  // Thread-safe
}

Client takeNextClient() throws InterruptedException {
    return globalWaitingQueue.take();  // Thread-safe, blocheaza daca nu sunt clienti
}
```

### 4. **Visibility Problem la Statusul Ghiseului**
**Problema**: Modificarile la statusul ghiseului (`OPEN`, `COFFEE_BREAK`) trebuie vazute de alte thread-uri imediat.

**Solutie**: 
- In `Counter.java`: folosire keyword `volatile` pentru variabile partajate
```java
private volatile CounterStatus status = CounterStatus.OPEN;
private volatile boolean running = true;
```

### 5. **Race Condition la Generarea ID-urilor**
**Problema**: Clienti si ghisee au nevoie de ID-uri unice, dar multiple thread-uri se creeaza simultan.

**Solutie**: 
- Folosire `AtomicInteger` pentru generare thread-safe de ID-uri
```java
private static final AtomicInteger clientIdGenerator = new AtomicInteger(1);
private final int id = clientIdGenerator.getAndIncrement();  // Atomic operation
```

### 6. **InterruptedException Handling**
**Problema**: Cand programul se inchide, thread-urile trebuie oprite gracefully.

**Solutie**: 
- Verificare flag `running` in toate loop-urile
- Proper handling al `InterruptedException`
```java
try {
    Thread.sleep(1000);
} catch (InterruptedException ie) {
    if (!running) break;
    Thread.currentThread().interrupt();  // Restore interrupt status
}
```

### 7. **Shutdown Coordination**
**Problema**: Toate thread-urile (clienti si ghisee) trebuie sa se termine in ordine corecta.

**Solutie**: 
- In `Main.java`: asteptare ca toti clientii sa termine (`join()`)
- Apoi inchidere ghisee cu `shutdown()` si `interrupt()`
- Timeout pentru `join()` ca sa evite blocare infinita

```java
// Asteapta ca toti clientii sa termine
for (Thread clientThread : clientThreads) {
    clientThread.join();
}

// Inchide ghiseele
for (Office office : offices.values()) {
    office.shutdownCounters();
}
```

## 🎯 Concepte Demonstrate

Acest proiect demonstreaza:

- **Multi-threading**: Gestionarea thread-urilor concurente pentru clienti si ghisee
- **Sincronizare**: Operatiuni thread-safe si pattern-uri wait/notify
- **Algoritmi pe Grafuri**: Rezolvarea dependentelor si detectarea ciclurilor
- **Design Patterns**: Producer-Consumer (ghisee-clienti), State pattern (statusuri ghisee)
- **Configurare Externa**: Folosirea fisierelor YAML
- **Simulare Real-World**: Modelarea proceselor birocratice complexe

