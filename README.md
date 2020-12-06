## ZomboidDoc

[![Build Status](https://jitpack.io/v/yooksi/pz-zdoc.svg)](https://jitpack.io/#yooksi/pz-zdoc) [![Continuous Integration](https://jitci.com/gh/yooksi/pz-zdoc/svg)](https://jitci.com/gh/yooksi/pz-zdoc) ![Game Version](https://img.shields.io/badge/PZ%20Version-IWBUMS%3A%2041.47-red) [![codecov](https://codecov.io/gh/yooksi/pz-zdoc/branch/master/graph/badge.svg?token=4D4PT2512I)](https://codecov.io/gh/yooksi/pz-zdoc) [![License](https://img.shields.io/github/license/yooksi/pz-zdoc)](https://www.gnu.org/licenses/)

ZomboidDoc is an API parser and Lua library compiler for Project Zomboid.

## Motivation
Mods for Project Zomboid are written exclusively in Lua language by interacting with exposed Java classes and ame code is documented online through JavaDocs. This provides us with necessary information but makes development a bit of a hassle when using an integrated development environment such as IntelliJ IDEA since we are expecting a high degree of interactive feedback when writing code. Many advanced [features](https://www.jetbrains.com/idea/features/) offered by IDEA are unavailable since we do not have access to expected Lua source code.

ZomboidDoc solves this problem by parsing the online API documentation and compiling it into a fully annotated Lua library that can be read by IDEA which makes writing mods much easier.

## Features

- Documents local Lua files with [EmmyLua](https://github.com/EmmyLua/IntelliJ-EmmyLua/) annotations, making it much easier to write code by providing on the fly code inspection and completion when using IntelliJ IDEA.
- Parses available online API documentation and compiles it into annotated Lua code.
- Creates a fully documented and readable modding Lua library.

## Installation
- Download the [latest release](https://github.com/yooksi/pz-zdoc/releases/latest) from the repository releases section.
- Place the jar in `media\lua` relative to your game installation directory <i>or</i>i anywhere on your computer.

## How to use?
- Run the application via command line using command-appropriate syntax.
  `java -jar <jar filename> <command> <args>`
- Use `help` command to print command usage info for all available commands. 

### Annotate Lua

```shell
# Annotate local Lua files with EmmyLua annotations.
java -jar $APP_JAR lua -i <input_path> -o <output_path>
```

### Compile Lua

```shell
# Parse online java api doc from url and compile to lua
java -jar $APP_JAR java -a <url> -o <output_path>

# Parse local java doc from path and compile to lua
java -jar $APP_JAR java -i <input_path> -o <output_path>
```

## Credits

- The Indie Stone for developing Project Zomboid.
- [FWolfe](https://github.com/FWolfe/) for writing [Zomboid-Modding-Guide](https://github.com/FWolfe/Zomboid-Modding-Guide).

## License
This project is licensed under [GNU General Public License v3.0](https://github.com/yooksi/pz-zdoc/blob/master/LICENSE.txt).