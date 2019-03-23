This is a basic implementation of CoreWar to demonstrate the basic concept in this program battleground.
Simulator.java is the main entry point to run a single CoreWar game, where an Imp and a Dwarf is spawned randomly in a fixed-size circular buffer and try to kill each other.
By killing I simply mean forcing an opponent program to execute either 1) a line of code that doesn't belong to it or 2) data.
Note this is not how real CoreWar is played because in a real CoreWar game, a program dies iff it executes data. The basic idea remains largely the same nonetheless.

For more information about CoreWar, please visit https://www.corewars.org
