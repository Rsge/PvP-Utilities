Beta:

0.2.0
Added:
- "all"-option to lives- and time commands
- command to change time multiplier
- UUID related options to all lives- and time commands
- option to use amount of 0 in lives command
- a few config options related to macros
- comments to config categories
Removed a bit of (almost) duplicate debug logging
Changed:
- time modifier to decimal number (float) instead of percentage
- spawn distance to circular radius instead of rectangular one
- a bit of wording
Finally fixed:
- enable & disable commands not even working
- time added on new day exceeding max time
- scoreboard not updating when changing lives via commands
Optimized:
- command handling
- life and time handling
Corrected more spelling mistakes

0.1.0
Initial Github commit
Fixed lives and time not updating when logging out of and back into a singleplayer world
Added Javadoc documentation
Corrected spelling mistakes


Alpha:

0.0.13
Added option to add no time per day

0.0.12
Added playerbound percentage-modifier for daily added time in "times.dat"-File
Added command-option with uuid for offline lives- and time-lookup

0.0.11
Added option for max experience-level
Improved commandlogging (Now logs the whole command)
Resorted config (again :P)

0.0.10
Added scoreboard-support for lives

0.0.9
Added differentiation between start-time and daily added time
Added option to stop time in spawn-area

0.0.8:
Added global "enable"- & "disable"-functions for lives & time
Config-sorting improvements

0.0.7:
Lots of small changes and bugfixes
Anti-macro still buggy

0.0.6:
Added anti-macro functionality (buggy)
Slight Performance-Optimizations
Resorted config

0.0.5:
Added life-chat after respawn

0.0.4:
Bugfixes

0.0.3:
Added "start-" and "stop"-functions for time
Removed logs of unnecessary commands

0.0.2:
Added "set"- and "reset"-functions for lives & time

0.0.1:
Closed release
Basic functions: Add, remove and look up lives and time