<p align="center">
  <img src="https://github.com/aparx/perx/assets/47287352/9dcafd26-54dd-4e72-b195-784195dda887" width="200" />
  <p align="center">
    A permission grouping system for large Bukkit server networks.<br/>
    Perx is asynchronous by nature and is required to be used with a Database.<br/>
  </p>
</p>
<br/>

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

### An das PlayLegend Entwicklungsteam
Folgend findet sich eine Projektübersicht (Deutsch): https://docs.google.com/presentation/d/1XapqCryj0wmB78rzzybbEU5LY6gxr__NbOUVa-hRonc/edit?usp=sharing
