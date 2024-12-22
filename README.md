<img src="common/src/main/resources/assets/lithium/lithium-icon.png" width="128">

# Lithium

Lithium is a free and open-source Minecraft mod which works to optimize many areas of the game in order to provide
better overall performance. It works on both the **client and server**, and **doesn't require the mod to be installed
on both sides**.

## 📥 Downloads & Installation

Download Lithium from [Modrinth](https://modrinth.com/mod/lithium) or
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/lithium).

Lithium supports two mod loaders: Fabric and the NeoForge. You can use a launcher
(e.g. the [Modrinth launcher](https://modrinth.com/app)) to install lithium. Alternatively, you can install lithium
manually by placing lithium's jar-file in the `mods` folder of your game after you have installed the
corresponding mod loader.

### 📝 Why Choose Lithium?

Lithium is the perfect drop-in mod for players who want more performance without affecting their gameplay.
It doesn't change any game mechanics or visuals, it just makes the game run faster. Lithium is compatible with
most other mods, so you can use it alongside your favorite mods without any issues.

If you do encounter an issue where Lithium deviates from the norm or does not work with another mod, please don't
hesitate to [📬 open an issue](https://github.com/CaffeineMC/lithium/issues). Before opening a new issue, please check
using the search tool that your issue has not already been created, and that if
there is a suitable template for the issue you are opening, that it is filled out entirely. We will provide workarounds
and fixes for issues as soon as possible.

### 🌟 Support the Development

Lithium is actively developed by 2No2Name since JellySquid has stepped down from active development in 2020.
Several other contributors have also helped with the development of Lithium, and we are grateful for 
[their contributions](https://github.com/CaffeineMC/lithium-fabric/graphs/contributors).

If you would like to support the development of Lithium, you can do so by joining the community and contributing to the
project, or by signing up on patreon:

|                                                                                | Author     | Role             | Links                                   |
|--------------------------------------------------------------------------------|:-----------|:-----------------|:----------------------------------------|
| ![2No2Name's Avatar](https://avatars3.githubusercontent.com/u/50278648?s=32)   | 2No2Name   | Developer        | [Patreon](https://patreon.com/2No2Name) |
| ![jellysquid3's Avatar](https://avatars3.githubusercontent.com/u/1363084?s=32) | JellySquid | Former Developer |                                         |


### 💬 Join the CaffeineMC Community

We have an [official Discord community](https://jellysquid.me/discord) for all of our projects. By joining, you can:
- Get installation help and technical support with all of our mods 
- Be notified of the latest developments as they happen
- Get involved and collaborate with the rest of our team
- ... and just hang out with the rest of our community.

---

### ⚙️ Configuration

Out of the box, no additional configuration is necessary once the mod has been installed.
Lithium is made of a collection of vastly different optimizations. Very few optimizations depend on each other, enabling
you to resolve mod compatibility issues by disabling problematic optimizations. Optimizations are disabled by
default if there is a major issue with them that has not yet been resolved.

As such, an empty config file simply means you'd like to use the
default configuration, which includes all stable optimizations by default.
For the list of options, see the [configuration file summary](lithium-mixin-config.md).

### 🛠️ Bleeding-edge builds (unstable)

[![GitHub build status](https://github.com/CaffeineMC/lithium-fabric/actions/workflows/gradle.yml/badge.svg)](https://github.com/CaffeineMC/lithium-fabric/actions/workflows/gradle.yml)

If you are a player who is looking to get your hands on the latest **bleeding-edge changes for testing**, consider
taking a look at the automated builds produced through
our [GitHub Actions workflow](https://github.com/CaffeineMC/lithium-fabric/actions/workflows/gradle.yml?query=event%3Apush)
. This workflow automatically runs every time a change is pushed to the repository, and as such, the builds it produces
will generally reflect the latest snapshot of development.

Bleeding edge builds will often include unfinished code that hasn't been extensively tested. That code may introduce
incomplete features, bugs, crashes, and all other kinds of weird issues. You **should not use these bleeding edge builds**
unless you know what you are doing and are comfortable with software debugging. If you report issues using these builds,
we will expect that this is the case. Caveat emptor.

---

### 🛠️ Building from sources

Lithium uses the [Gradle build tool](https://gradle.org/) and can be built with the `gradle build` command. The build
artifacts (production binaries and their source bundles) can be found in the `build/mods` directory.

The [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:using_wrapper) is provided for ease of use and will automatically download and install the
appropriate version of Gradle for the project build. To use the Gradle wrapper, substitute `gradle` in build commands
with `./gradlew.bat` (Windows) or `./gradlew` (macOS and Linux).

### 📜 License

Lithium is licensed under GNU LGPLv3, a free and open-source license. For more information, please see the
[license file](LICENSE.md).
