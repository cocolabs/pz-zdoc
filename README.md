# ZomboidDoc

![Java CI](https://github.com/yooksi/pz-zdoc/workflows/Java%20CI/badge.svg?branch=dev) ![release](https://img.shields.io/github/v/release/yooksi/pz-zdoc) [![codecov](https://codecov.io/gh/yooksi/pz-zdoc/branch/master/graph/badge.svg?token=4D4PT2512I)](https://codecov.io/gh/yooksi/pz-zdoc) [![License](https://img.shields.io/github/license/yooksi/pz-zdoc)](https://www.gnu.org/licenses/) [![chat](https://img.shields.io/discord/717757483376050203?color=7289DA)](https://discord.gg/vCeydWCbd9)
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fyooksi%2Fpz-zdoc.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fyooksi%2Fpz-zdoc?ref=badge_shield)

ZomboidDoc is an easy-to-use Java command-line application that compiles an annotated Lua library directly from modding API. Powered by [IntelliJ IDEA](https://www.jetbrains.com/idea/) it makes mod development an enjoyable experience by providing everything you need to write an amazing mod right from you IDE.

## Introduction

Mods for Project Zomboid that change existing or add custom game logic are composed of scripts written in Lua language. Scripts accomplish this by interacting with Java classes exposed by the game engine (also known as modding API) which are documented online via [Javadocs](https://projectzomboid.com/modding/). Although useful, this documentation is often out of date and a bit of a hassle to read through, but does provide us with necessary information to start writing mods.

However we still lack a comfortable development environment required to stay motivated and creative. This is where ZomboidDoc comes in! It compiles a Lua library directly from exposed game classes using online modding API to get important information not stored in compiled code such as parameter names and comments.

Because ZomboidDoc reads directly from game code the compiled Lua library is guaranteed to always be up-to-date with your installed game version regardless of online API documentation - great when modding for beta game versions.

## Features

- Creates a fully documented, readable and always up-to-date modding Lua library.
- Parses online API documentation to include information not available from decompiled code.
- Uses [EmmyLua](https://github.com/EmmyLua/IntelliJ-EmmyLua) annotations to enable a high degree of interactive code feedback.
- **Syntax highlighting** for Lua language to help you navigate your code.
- On the fly **code inspection** to identify problems and offer solutions.
- **Smart completion** that gives you a list of relevant symbols in current context.
- Much more features to discover as you create your mods.

## Installation

### Requirements

- Up-to-date installation of Project Zomboid.

### Setup

- Download the [latest release](https://github.com/yooksi/pz-zdoc/releases/latest) from the repository releases section.
- Extract the release archive to your game installation directory <i>or</i> anywhere on your computer.

## How to use

> - *Little question marks are hyperlinks that reveal more information on mouse hover and click.*
> - *Content captured in angled brackets represents substitution. For example if the path to game directory was `/home/projectzomboid/` then `PZ_DIR_PATH=<path_to_game_dir>` should be substituted with `PZ_DIR_PATH=/home/projectzomboid/`* 
> - *Application path arguments should not end with a backslash when running on Windows to avoid escaping quotation mark delimiters that separate quoted paths as option arguments.*  

### Distribution

The release distribution archive contains two directories:

- `bin` directory contains application launch scripts.
- `lib` directory contains application and dependency `jar` files.

### Launch Script

Follow these steps in order to launch ZomboidDoc:

1. Open the terminal and navigate to ZomboidDoc `bin` directory.  
   `$ cd /D <absoulte_path_to_app_dir>/bin`
2. Set environment variable `PZ_DIR_PATH` to point to game installation directory.  
   `$ set PZ_DIR_PATH=<path_to_game_dir>` - on Windows.  
   `$ export PZ_DIR_PATH=<path_to_game_dir>` - on Unix.
3. Launch ZomboidDoc with an appropriate launch script.  
   `$ start pz-zdoc.bat <command> <args>` - on Windows.  
   `$ sh pz-zdoc <command> <args>` - on Unix.

Read [Commands](#commands) and [Examples](#examples) section for more information.

### Commands

Here is an overview list of available commands:

- `help` - print command usage info for all available commands.
- `version` - print Project Zomboid game installation version.
- `annotate` - annotate vanilla Lua files with EmmyLua.
- `compile` - compile Lua library from modding API.

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

# Annotate vanilla Lua files with EmmyLua
start pz-zdoc.bat annotate -i %PZ_DIR_PATH%\media\lua -o ..\media\lua

# Compile Lua library from modding API
start pz-zdoc.bat compile -i %PZ_DIR_PATH% -o ..\media\lua\shared\Library

# Check compile ouput directory
cd ..\media\lua && dir /B
```

Launch ZomboidDoc on Linux:

```shell
# Navigate to application bin directory
# Application is NOT installed in game root directory
cd /home/yooks/Documents/pz-zdoc/bin

# Set environment variable to game installation directory
export PZ_DIR_PATH=/home/yooks/.local/share/Steam/steamapps/common/ProjectZomboid/projectzomboid

# Annotate vanilla Lua files with EmmyLua
sh pz-zdoc annotate -i $PZ_DIR_PATH/media/lua -o ../media/lua

# Compile Lua library from modding API
sh pz-zdoc compile -i $PZ_DIR_PATH -o ../media/lua/shared/Library

# Check compile ouput directory
cd ../media/lua && ls
```

### Lua library

#### Standalone

After compiling the library no additional steps are *required* and you can use it as-is with your favorite text editor.

Note that although the compiled Lua library can be used without any additional software integration it is intended to be used with IntelliJ IDEA to provide advanced [features](#features) only available when using IDE.

#### IDE integration

Follow these steps to create a new mod project and enable IDE integration:

- Download and install [IntelliJ IDEA](https://www.jetbrains.com/idea/download/).
- Install [EmmyLua](https://plugins.jetbrains.com/plugin/9768-emmylua) IDEA plugin.
- Create a new Lua project from IDEA.[<sup>?</sup>](#ide-integration "File -> New -> Project... -> Lua")
- Package the compiled library directory in an archive (to make it read-only).
- Create `libs` directory inside mod root directory and move the library archive inside it.
- Add the library archive as a main module dependency.[<sup>?</sup>](https://www.jetbrains.com/help/idea/working-with-module-dependencies.html#add-a-new-dependency "File -> Project Structure... -> Modules -> <module> -> Dependencies -> Add (Alt + Insert) -> Library... (Lua Zip Library)")
- Setup basic [mod structure](https://github.com/FWolfe/Zomboid-Modding-Guide/blob/master/structure/README.md) to start your modding adventure.

That's it, everything should be setup!

You can now continue modding with full confidence that everything you need to know to create an amazing mod is right at your fingertips. See list of [features](#features) to remind yourself what to expect.

## Discussion

- Feel free to [open a ticket](https://github.com/yooksi/pz-zdoc/issues/new) if you have any problems, questions or suggestions regarding the project.
- You are also welcome to join us on [Discord](https://discord.gg/vCeydWCbd9) to talk about Project Zomboid modding and follow community projects.

## Credits

- The Indie Stone for developing Project Zomboid.
- [FWolfe](https://github.com/FWolfe/) for writing [Zomboid-Modding-Guide](https://github.com/FWolfe/Zomboid-Modding-Guide).

## License

This project is licensed under [GNU General Public License v3.0](https://github.com/yooksi/pz-zdoc/blob/master/LICENSE.txt).

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fyooksi%2Fpz-zdoc.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Fyooksi%2Fpz-zdoc?ref=badge_large)