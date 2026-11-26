# CubingPaper

Fork of https://github.com/PaperMC/Paper-archive/tree/ver/1.8.8

## How to (Build from Source)

### Requirements

- JDK 8
- Maven
- curl
- git
- jq
- unzip

### Steps

1. Clone this repository:
   ```bash
   git clone https://github.com/speedcubing-top/cubingpaper.git
   cd cubingpaper
   ```
2. Run the build script:
   ```bash
   ./build.sh
   ```
3. After building, you'll find the server JAR located at:
   ```
   ./target/cubingpaper-1.8.8-R0.1-SNAPSHOT.jar
   ```

## Fog of war

Anti-ESP: a player is not sent to viewers that could not see it (walls, terrain), so ESP, chams and nametags through
walls have nothing to draw. Visibility is ray-checked on worker threads every few ticks, with lookahead for ping,
movement and the third person camera, so players are revealed before they come into sight.

Off by default. Configure it in `cubing.yml`:

```yaml
fogOfWar:
  enabled: false          # master switch; nothing runs while false
  default: true           # applies to players without a plugin override (see API below)
  interval-ticks: 2       # ticks between checks
  hide-after-checks: 3    # checks out of sight before a player is hidden
  camera-distance: 4.0    # third person camera reach (4 in vanilla 1.8)
  threads: <cores / 4>    # worker threads
  log-interval-seconds: 0 # statistics in the log, 0 = off
```

Players in spectator mode or with the permission `cubingpaper.fogofwar.bypass` always see everyone.

### Plugin API

Plugins can turn fog of war on or off per player, e.g. only for some game modes in a shared world:

```java
player.setFogOfWar(true);  // this player gets fog of war
player.setFogOfWar(false); // this player sees everyone
player.setFogOfWar(null);  // back to fogOfWar.default
player.hasFogOfWar();      // what applies now (always false while fogOfWar.enabled is false)
```

- The setting is per viewer: it decides what that player is sent, not whether others see it. Give every player of a
  match the same setting.
- Changes apply at the next check (within `interval-ticks`); turning it off reveals hidden players right away.
- Call from the main thread. Has no effect while `fogOfWar.enabled` is false.
- For per mode use, set `fogOfWar.default: false` and enable it for the players of the modes that want it.
