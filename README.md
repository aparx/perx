<p align="center">
  <img src="https://github.com/aparx/perx/assets/47287352/9dcafd26-54dd-4e72-b195-784195dda887" width="250" />
  <p align="center">
    A permission grouping system for large Bukkit server networks.<br/>
    Perx is asynchronous by nature and is required to be used with a Database.<br/>
    Project overview (German): https://shorturl.at/jyIW2
  </p>
</p>
<br/>

### PlayLegend
#### Mindestanforderungen
- [x] Gruppen können im Spiel erstellt und verwaltet werden
- [x] Die Gruppe muss mindestens folgende Eigenschaften haben (Name, Prefix + Suffix)
- [x] Spieler soll einer Gruppe zugewiesen werden können (Permanent, Temporär)
- [x] Prefix (+Suffix) von der Gruppe soll im Chat und beim Betreten des Servers angezeigt werden
- [x] Wenn der Spieler eine neue Gruppe zugewiesen bekommt, soll diese sich unmittelbar ändern (Spieler soll nicht gekickt werden)
- [x] Alle Nachrichten sollen in einer Konfigurationsdatei anpassbar sein
- [x] Durch einen Befehl erfährt der Spieler seine aktuelle Gruppe und ggf. wie lange er diese noch hat
- [x] Ein oder mehrere Schilder sollen hinzugefügt werden können, die Informationen eines einzelnen Spielers wie Name & Rang anzeigen
- [x] Alle nötigen Information werden in einer relationalen Datenbank gespeichert (konfigurierbare Texte nicht)
#### Bonus:
- [x] Für eine Gruppe können Berechtigungen festgelegt und sollen dem Spieler dementsprechend zugewiesen werden. Abfrage über #hasPermission sollte funktionieren
- [x] “*” Berechtigung
- [ ] Unterstützung von mehreren Sprachen (=> theoretisch einfach implementierbar)
- [x] Tabliste mit der jeweiligen Gruppe am besten sortiert
- [x] Scoreboard mit der jeweiligen Gruppe (im Prinzip schon)

### Key features
- Asynchronous to provide good performance on the primary thread 
- Groups, players, permissions, prefixes and suffixes all manageable through the game
- Signs that show a viewer's all their groups in an animated and intuitive way
- Sorted tablist, order completely customizable
- Extensive amount of customization

Try performing `/perx help` ingame to see all available commands.

### Creating signs
You can create a sign, that shows a viewer's groups by simply putting `[perx]` in the first line of your sign. This is what it would look like ingame:
<br/><br/>
<img src="https://i.gyazo.com/c66330001f1ee9ade9d42c0ae99eeccb.gif" />

### Creating groups programmatically
You do not want to use commands? You can extend Perx and create your groups using the `PerxGroupBuilder`, which produces `PerxGroup` instances.
```java
PerxGroupBuilder.builder("admin")
  .prefix(ChatColor.RED + "[Admin]")
  .suffix("your suffix")
  .setDefault(false)
  .addPermission("*")
  .setPermission("some.permission", false)
  .priority(0)
  //^ the lower the priority, the more important
  .build()
  .push();
  //^ upserts group to the database
```
