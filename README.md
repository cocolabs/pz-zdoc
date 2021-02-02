# ZomboidDoc

![Java CI](https://github.com/yooksi/pz-zdoc/workflows/Java%20CI/badge.svg?branch=dev) ![release](https://img.shields.io/github/v/release/yooksi/pz-zdoc) [![codecov](https://codecov.io/gh/yooksi/pz-zdoc/branch/master/graph/badge.svg?token=4D4PT2512I)](https://codecov.io/gh/yooksi/pz-zdoc) [![License](https://img.shields.io/github/license/yooksi/pz-zdoc)](https://www.gnu.org/licenses/)

ZomboidDoc is a Lua library compiler for Project Zomboid.

## Introduction

Mods for Project Zomboid that change existing or add custom game logic are composed of scripts written in Lua language. Scripts accomplish this by interacting with Java classes exposed by the game engine (also known as modding API) which are documented online via [Javadocs](https://projectzomboid.com/modding/). Documents provide us with necessary information to start creating script mods but we still lack a comfortable development environment required to stay motivated and creative. 

This is where ZomboidDoc comes in. Powered by [IntelliJ IDEA](https://www.jetbrains.com/idea/) ZomboidDoc makes mod development an enjoyable experience by providing a high degree of interactive feedback when writing code in addition to all the information you need to code an amazing mod right from you IDE.

## Features

- Creates a fully documented, readable and always up-to-date modding Lua library. 
- Parses online API documentation to include information not available from decompiled code.
- Uses [EmmyLua](https://github.com/EmmyLua/IntelliJ-EmmyLua) annotations to enable a high degree of interactive code feedback in IDEA:
	- **Syntax highlighting** for Lua language to help you navigate your code.
	- On the fly **code inspection** to identify problems and offer solutions.
	- **Smart completion** that gives you a list of relevant symbols applicable in current context.
	- Much more features to discover as you create your mods.

## Installation

### Requirements

- Up-to-date installation of Project Zomboid.

### Setup

- Download the [latest release](https://github.com/yooksi/pz-zdoc/releases/latest) from the repository releases section.
- Extract the release archive to your game installation directory <i>or</i> anywhere on your computer.

- Run the application via command line using command-appropriate syntax.
  `java -jar <jar filename> <command> <args>`
- Use `help` command to print command usage info for all available commands. 
## How to use

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