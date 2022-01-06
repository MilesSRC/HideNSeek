# HideNSeek
A hide and seek plugin made for FNAFMC's 2021 Holiday Event

## Notice
May possibly be really buggy in certain areas. This project was discontinued but may be used in a future project of mine.
The world that players are tp'd to is set in util/EventDataManager.java
as well as the coordinates. These should be changed before using the plugin.

## Perms:
fnafmc.event - Grants big boy commands

## Commands:
/event join - Join a pre-start event.
/event create - Start an event count down and start the event
/event forcestart - Forcefully start the event skipping the countdown.
/event cancel - Cancel /event create, don't use if the count down has gone past or you used /event forcestart
/event forceend - End a already started and spawned in event.

/event disqualify <player> - Disqualify a player from the event.
/event respawn <all / seeker / hiders / player> - Respawn someone to the inital spawn.
/event status - View debug about the event regarding if the event is spawned, whos seeker, is it started, etc.
/event rigseeker <player> - Can be used after /event create and before /event forcestart or the end of the countdown to rig the seeker. (THIS WILL ANNOUNCE TO EVERYONE POST-SPAWN THAT IT WAS RIGGED.)
