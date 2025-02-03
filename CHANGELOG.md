_ReleaseTag_ is automatically replaced with the release tag, e.g. mc1.21.4-0.14.5
_MCVersion_ is automatically replaced with the minecraft version, e.g. 1.21.4
_LithiumVersion_ is automatically replaced with the lithium version, e.g. 0.14.5
Everything above the line is ignored and not included in the changelog. Everything below will be in the
changelog on GitHub, Modrinth and CurseForge.
----------
Lithium _LithiumVersion_ for Minecraft _MCVersion_ includes a new optimization, a bugfix and improves mod compatibility.

Make sure to take a backup of your world before using the mod and please report any bugs and mod compatibility issues at the [issue tracker](https://github.com/CaffeineMC/lithium-fabric/issues). You can check the [description of each optimization](https://github.com/CaffeineMC/lithium/blob/_ReleaseTag_/lithium-mixin-config.md) and how to disable it when encountering a problem.

## Additions
- Fast-path exit end portal search by counting nearby bedrock blocks. Reduces lag when placing the last end crystal when respawning the ender dragon by 97%.
- Debug option for detecting invalid chunk data packets

## Changes
- Use worlds for thread tests instead of minecraft server to improve compatibility with the worldthreader mod

## Fixes
- Set default return value in LithiumHashPalette copy