# Agentdriven utveckling med Gnomish Mines

I de här övningarna låter du en AI-agent göra det mesta av kodandet i ett litet Java-projekt,
textäventyret **Gnomish Mines**. Målet är inte att bli klar så fort som möjligt. Målet är att öva
på det som avgör om agentdriven utveckling blir bra eller dålig:

- ge agenten rätt **kontext**,
- **planera** innan den skriver kod,
- låta **tester och verktyg** kontrollera resultatet, inte agentens egna försäkringar,
- **granska** allt som agenten ändrar,
- **styra** agenten med instruktionsfiler.

Du är ansvarig för koden som hamnar i ditt repo, oavsett vem som skrev den.

**Tid:** ca 2 timmar. **Förkunskaper:** Java, Maven och grundläggande Git.

| Steg | Innehåll                                  | Tid    |
| ---- | ----------------------------------------- | ------ |
| 0    | Förberedelser                             | 10 min |
| 1    | Låt agenten orientera dig                 | 15 min |
| 2    | Planera först, koda sedan                 | 40 min |
| 3    | Felsök utifrån en buggrapport             | 30 min |
| 4    | Sätt gränser och förbättra instruktionerna | 20 min |
| –    | Avslutande reflektion                     | 5 min  |

## Redovisning

Steg 2, 3 och 4 slutar var och en med en pull request i **ditt eget** repo. Svara på stegets
reflektionsfrågor i PR-beskrivningen. Svaren från steg 1 skriver du i PR:en för steg 2, och den
avslutande reflektionen i PR:en för steg 4. Lämna in länken till ditt repo.

Svara kort och ärligt. "Agenten hittade på en metod som inte finns, och jag märkte det först när
bygget föll" är ett mycket bättre svar än "det gick bra".

---

## Steg 0 – Förberedelser (10 min)

1. Gå till <https://github.com/yrgohrm/gnomishmines> och välj **Use this template → Create a new
   repository**. Kryssa i **Include all branches**, annars saknas grenen du behöver i steg 3.
2. Klona ditt nya repo och kontrollera att bygget går igenom. Du behöver JDK 25 och Maven 3.9
   eller senare.

   ```
   mvn verify
   ```

3. Öppna projektet i en editor med en AI-agent som kan läsa och ändra filer och köra kommandon.
   Vi rekommenderar **VS Code med GitHub Copilot i agentläge**, där du loggar in med ditt
   GitHub-konto. Andra agenter fungerar lika bra.
4. Läs [`AGENTS.md`](AGENTS.md) i repot. Det är agentens instruktioner för just det här projektet.
   De flesta agenter läser filen automatiskt. Om din inte gör det, ta reda på hur du pekar ut den,
   eller be agenten läsa den i början av varje session.

> **Tips:** Starta en ny chattsession för varje steg. En lång session fylls med gammal kontext som
> distraherar agenten, och du lär dig mer om vad agenten faktiskt vet från början.

> **Om du glömde "Include all branches":** hämta grenen från originalrepot och lägg den i ditt eget:
>
> ```
> git fetch https://github.com/yrgohrm/gnomishmines.git exercise-bug-report
> git switch -c exercise-bug-report FETCH_HEAD
> git push -u origin exercise-bug-report
> git switch main
> ```

---

## Steg 1 – Låt agenten orientera dig (15 min)

**Fokus:** att använda agenten för att förstå okänd kod, och att inte lita blint på svaren.

1. Starta en ny session. Be agenten förklara projektet: vad det är, hur paketen hänger ihop och
   vad som händer från att spelaren skriver `take rope` tills svaret visas. Be den hänvisa till
   filer och metoder.
2. Välj **ett** konkret påstående i svaret och kontrollera det själv i koden. Stämde det?
3. Fråga: *"Vad måste vara uppfyllt innan en ändring räknas som klar i det här repot?"* Svarar
   agenten utifrån `AGENTS.md`, eller ger den ett allmänt svar som skulle passa vilket projekt som
   helst?
4. Ställ en fråga som bygger på något som inte stämmer, till exempel: *"Var i koden sparas
   highscore-listan till disk?"* Rättar agenten dig, eller hittar den på ett svar?
5. Spela spelet själv i ett par minuter så att du vet vad det är du ska ändra i.

   ```
   java -jar target/gnomish-mines-1.0.0-SNAPSHOT.jar --seed 42
   ```

**Reflektion (skriv i PR:en för steg 2):**

- Vilket påstående kontrollerade du, och stämde det?
- Visste agenten vad som gäller för "klart" i det här repot? Hur märktes det?
- Hur hanterade agenten frågan om highscore-listan?

---

## Steg 2 – Planera först, koda sedan (40 min)

**Fokus:** tydliga uppdrag, planering, små steg och att verifiera med verktyg.

Välj **en** av funktionerna:

- **A. `again`**: kommandot `again` (eller `g`) upprepar spelarens senaste kommando.
- **B. Stegräknare**: spelet räknar dina drag, kommandot `moves` visar antalet, och
  vinstmeddelandet berättar hur många drag du behövde.
- **C. En ny plats**: ett nytt rum med ett nytt föremål eller en ny invånare, inkopplat i kartan.

Gör så här:

1. Skriv ett uppdrag till agenten. Beskriv **vad** funktionen ska göra och **varför**, och ange
   hur du kommer att avgöra att den är klar. Tänk igenom minst ett kantfall själv innan du skriver,
   till exempel vad `again` ska göra om det är spelets första kommando, eller om räknaren ska räkna
   kommandon som inte förstods.
2. Be agenten att **bara ta fram en plan** och inte ändra några filer än. Använd agentens planläge
   om den har ett (i Copilot: *Plan* i agentväljaren), annars säger du det uttryckligen.
3. Granska planen mot `AGENTS.md`, under *Adding a command* eller *Changing the world*. Saknas
   något? Be agenten justera planen innan den får börja.
4. Låt agenten genomföra planen. Följ med i vad den gör, och avbryt om den börjar göra något annat
   än det ni kom överens om. Enligt `AGENTS.md` ska agenten följa GitHub Flow, alltså själv skapa
   en gren från en uppdaterad `main` innan den ändrar något. Säg inget om det i förväg, utan se om
   den gör det.
5. Kontrollera resultatet själv. Lita inte på att agenten säger att det fungerar:
   - Kör `mvn verify` själv.
   - Läs hela diffen (`git diff`, eller editorns diffvy). Du ska kunna förklara varje ändrad rad.
   - Spela och prova funktionen, inklusive ditt kantfall.
6. Be agenten committa, pusha och öppna en PR mot `main` i ditt repo. Om agenten inte kan öppna
   PR:er (den behöver till exempel GitHub CLI, `gh`), öppnar du den själv på GitHub. Granska PR:en
   och merga den sedan själv (squash and merge).

**Reflektion (skriv i PR-beskrivningen):**

- Skapade agenten en egen gren utan att du bad om det? Om inte, varför tror du att den inte
  gjorde det?
- Vad saknades i agentens första plan, om något?
- Behövde du rätta agenten under arbetet? Vad hände?
- Hittade du något i diffen som du inte hade bett om, eller som du inte förstod?
- Gick `mvn verify` igenom på första försöket? Om inte: vad stoppade det, och vem upptäckte det?

---

## Steg 3 – Felsök utifrån en buggrapport (30 min)

**Fokus:** att låta agenten reproducera ett fel med ett test innan den rättar det.

En spelare har skickat in den här buggrapporten:

> Hej! Jag har spelat Gnomish Mines en hel del. README säger att man gör 1–4 i skada med bara
> nävarna, men jag har slagits säkert femtio gånger och aldrig fått mer än 3. Spindeln verkar
> heller aldrig göra mer än 2 i skada, och den ska kunna göra 3. Gör jag något fel?

Felet finns på grenen `exercise-bug-report` i ditt repo.

1. Byt till grenen och kör `mvn verify`. Bygget går igenom, trots buggen.

   ```
   git switch exercise-bug-report
   ```

2. Starta en ny session. Ge agenten **buggrapporten**, inte din egen gissning om orsaken. Berätta
   att felet finns på `exercise-bug-report`, och att rättelsen ska utgå från den grenen och inte
   från `main`, eftersom `AGENTS.md` annars säger åt agenten att börja från `main`. Be den att,
   innan den rättar något:
   1. förklara sin hypotes om orsaken,
   2. skriva ett test som misslyckas och därmed visar buggen.
3. Kör testerna själv och kontrollera att det nya testet verkligen misslyckas, och att det
   misslyckas av rätt anledning.
4. Låt agenten rätta felet. Nu ska det nya testet gå igenom, och `mvn verify` ska vara grönt.
5. Fråga agenten varför de befintliga testerna inte upptäckte felet. Kontrollera att förklaringen
   stämmer.
6. Låt agenten öppna en PR mot **`exercise-bug-report`**, inte mot `main`. Grenar som skapats från
   en template har ingen gemensam historik, så GitHub kan inte göra en PR mellan dem. Kontrollera
   att PR:en har rätt målgren.

**Reflektion (skriv i PR-beskrivningen):**

- Vad var orsaken, och stämde agentens första hypotes?
- Varför fångade inte de befintliga testerna felet?
- Vad vinner man på att skriva ett misslyckat test innan man rättar? Vad hade kunnat gå fel om
  agenten rättade direkt?

---

## Steg 4 – Sätt gränser och förbättra instruktionerna (20 min)

**Fokus:** hur instruktionsfiler styr agenten, och hur man testar att de fungerar.

1. Gå tillbaka till `main` (`git switch main`, `git pull`) och starta en ny session. Be agenten om något som bryter mot
   reglerna i `AGENTS.md`, till exempel:
   - *"Låt trollet missa ungefär var tredje attack. Använd `Math.random()`, så går det snabbt."*
   - *"Committa ändringen direkt på `main`."*

   Säger agenten ifrån, föreslår den något bättre, eller gör den som du säger? Om den ändrade
   något, kasta ändringarna med `git restore .` eller `git reset --hard`.
2. Tänk tillbaka på steg 2 och 3. Vad fick du påminna agenten om, eller vad missade den? Lägg till
   en ny, konkret regel i `AGENTS.md`, eller förtydliga en befintlig. En bra regel är kort, går att
   kontrollera och förklarar varför. Exempel på vad en regel kan handla om:
   - en fil som alltid ska uppdateras när man ändrar något visst,
   - ett kommando agenten alltid ska köra,
   - något agenten aldrig ska göra utan att fråga.
3. Testa regeln. Starta en **helt ny** session och ge agenten en uppgift där regeln spelar roll,
   utan att nämna regeln. Följer agenten den?
4. Låt agenten committa ändringen i `AGENTS.md` på en egen gren, pusha och öppna en PR mot
   `main`. Granska och merga själv.

**Reflektion (skriv i PR-beskrivningen):**

- Hur reagerade agenten på uppdragen som bröt mot reglerna?
- Vilken regel lade du till, och varför?
- Följde agenten regeln i den nya sessionen? Om inte, vad tror du att det berodde på?

---

## Avslutande reflektion (5 min)

Skriv i PR:en för steg 4:

- Vad var agenten bäst på i de här övningarna?
- När var du tvungen att tänka själv, eller att stoppa agenten?
- Nämn en sak du kommer att göra annorlunda nästa gång du arbetar med en AI-agent.

---

## Extra, om du har tid över

- **Låt en agent granska koden.** Starta en ny session och be agenten granska din PR från steg 2
  som om den vore någon annans kod. Jämför med din egen granskning. Hittade den något du missade,
  eller påpekade den saker som inte är problem?
- **Gör ett återanvändbart arbetssätt.** Mappen `.agents/skills/` innehåller exempel på *skills*,
  instruktioner som agenten laddar när de behövs. Skriv en egen, till exempel "lägg till ett nytt
  kommando", i det format din agent stöder (skills, prompt-filer eller liknande). Testa den på en
  ny funktion från steg 2.
