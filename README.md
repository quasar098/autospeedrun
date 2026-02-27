# autospeedrun

"MCSR Robot League" coming soon

contact me on discord if you are interested and want more information

## references

https://www.mcpk.wiki/wiki/Horizontal_Movement_Formulas

https://minecraft.wiki/w/Options.txt

## practice seed list

- `-1807904186447035469` easy trees, easy bt

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
- f3 left shift vs crouch left shift discrepancy for faster pie charting
- per biome block assumption heuristics to save mouse scan power
- double bucket fast 2x1 portal on BTs/shipwrecks with lots of iron
- cinematic camera for scanning / more tolerable camera

## broad todo list

- change most classes to singleton and private defaults instead of having
public static everywhere
- shake mouse with nav enabled and it doesn't go the right way but it should
- all other seed types than BT

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
