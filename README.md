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

## How to use

### Distribution

The release distribution archive contains two directories:

- `bin` directory contains application launch scripts.
- `lib` directory contains application and dependency `jar` files.

### Launch Script

Follow these steps in order to launch the application:

1. Open the terminal and navigate to application bin directory.
   `$ cd <absoulte_path_to_app_dir>/bin`
2. Set environment variable `PZ_DIR_PATH` to point to game installation directory.
   `$ set PZ_DIR_PATH=<path_to_game_dir>` - on Windows.
   `$ export PZ_DIR_PATH=<path_to_game_dir>` - on Unix.
3. Launch the application with an appropriate launch script.
   `$ start pz-zdoc.bat <command> <args>` - on Windows.
   `$ sh pz-zdoc <command> <args>` - on Unix.

Read [Commands](#commands) and [Examples](#examples) section for more information.

### Commands

Here is an overview list of available commands:

- `help` - print command usage info for all available commands.
- `version` - print Project Zomboid game installation version.
- `annotate` - annotate local Lua files with EmmyLua annotations.
- `compile` - compile lua library from modding API.

Notes to keep in mind when executing commands:

- To learn how to use each command run `help [command]` (e.g `help annotate`).
- Command path arguments that contain whitespaces need to be enclosed in quotation marks.

### Examples

Launch ZomboidDoc on Windows:

```batch
@rem Navigate to application bin directory
@rem Application is installed in game root directory
cd /D E:\Games\Steam\steamapps\common\ProjectZomboid\pz-zdoc\bin

# Set environment variable to game installation directory
set PZ_DIR_PATH=%cd%\..\..\

# Annotate local Lua files with EmmyLua annotations
start pz-zdoc.bat annotate -i %PZ_DIR_PATH%\media\lua -o ..\media\lua

# Compile lua library from modding API
start pz-zdoc.bat compile -i %PZ_DIR_PATH% -o ..\media\lua\shared\Library
```

Launch ZomboidDoc on Linux:

```shell
# Navigate to application bin directory
# Application is NOT installed in game root directory
cd /home/yooks/Documents/pz-zdoc/bin

# Set environment variable to game installation directory
export PZ_DIR_PATH=/home/yooks/.local/share/Steam/steamapps/common/ProjectZomboid/projectzomboid

# Annotate local Lua files with EmmyLua annotations
sh pz-zdoc annotate -i $PZ_DIR_PATH/media/lua -o ../media/lua

# Compile lua library from modding API
sh pz-zdoc compile -i $PZ_DIR_PATH -o ../media/lua/shared/Library
```

## Credits

- The Indie Stone for developing Project Zomboid.
- [FWolfe](https://github.com/FWolfe/) for writing [Zomboid-Modding-Guide](https://github.com/FWolfe/Zomboid-Modding-Guide).

## License

This project is licensed under [GNU General Public License v3.0](https://github.com/yooksi/pz-zdoc/blob/master/LICENSE.txt).