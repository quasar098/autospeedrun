# autospeedrun

"MCSR Robot League" coming soon

contact me on discord if you are interested and want more information

## references

https://www.mcpk.wiki/wiki/Horizontal_Movement_Formulas

https://minecraft.wiki/w/Options.txt

https://en.wikipedia.org/wiki/3D_projection#Perspective_projection

https://en.wikipedia.org/wiki/Boolean_operations_on_polygons

https://dl.acm.org/doi/epdf/10.1145/129902.129906

## practice seed list

- `-1807904186447035469` easy trees, easy bt
- `-405953324338831477` water traversal required for bt, hilly

## setup

recommended to open with IntelliJ IDEA

all files/folders in `./settings/*` must be copied into `./run/`, overwriting files on conflict

## notes

1. mouse calibration
2. pie ray for general bt direction
3. travel to best chunk and go 9,9
4. mine down until chest or until 3 blocks broken (re scan if not there)
5. open chest, loot all essentials
6. travel back to spawn location
7. wander island and scan for leaves/logs
8. break logs
9. crafting table underneath feet, do crafting
10. magma vision detection? O counter? kelp strat? find ravine
11. go down to side of ravine get gravel
12. floor scan l shape magma
13. make l shape portal or something
14. in nether e ray scan for bastion
15. die in lava or something idfk

## potential strategies

- subpixelless one eye using mouse sens and pixel stepping and LLL
- f3 right shift vs crouch left shift discrepancy for faster pie charting
- per biome blockType assumption heuristics to save mouse scan power
- double bucket fast 2x1 portal on BTs/shipwrecks with lots of iron
- cinematic camera for scanning / more tolerable camera (?)

## broad todo list

- change most classes to singleton and private defaults instead of having
public static everywhere
- all other seed types than BT
- dead bush/door/whatever overrides because weird block hitbox
- pie ray use relative instead of global percentages
- Nef surfaces sphere mapping sort of thing to figure out optimal block angle looking location for partially hidden blocks
- switch to awaitables something something instead of having booleans and waiting for the next tick in a weird way

## licensing

there are two licenses present in this codebase.

everything by default is under GNU Affero Public License (AGPL).
a copy of the GNU Affero Public License (AGPL) license is present in `./LICENSE`.

all "user code"/"bot code" (everything in `./src/main/java/name/quasar/autospeedrun/usercode`) is licensed under the
MIT license. a copy of the MIT license is present in `./src/main/java/name/quasar/autospeedrun/usercode/LICENSE`.

the reasoning for this licensing setup is that I want to prevent people from profiting financially or socially
off of a codebase that took me a nonneglible amount of time to write and plan. simultaneously, i would like to see great
innovation when it comes to developing MCSR bots, so my sample "user code" is open for any modifications.

tl;dr: this API for making MCSR bots is strong copyleft, but for the actual bot code do whatever you want with it

## anti slop policy

this is a project that is developed just for the fun of not just the final destination but also the journey there. for this reason, slopping code is not allowed in this repository; only braincoding is allowed. take a look at the AI-preventing in `AGENTS.md` and `CLAUDE.md` for more information. they are copied from [github.com/Vxrpenter/AIMania](https://github.com/Vxrpenter/AIMania). pull requests containing slopped code will not be accepted.
