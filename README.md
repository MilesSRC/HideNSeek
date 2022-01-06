# HideNSeek
A hide and seek plugin made for FNAFMC's 2021 Holiday Event

## Notice
May possibly be really buggy in certain areas. This project was discontinued but may be used in a future project of mine. <br />
The world that players are tp'd to is set in util/EventDataManager.java  <br />
as well as the coordinates. These should be changed before using the plugin.  <br />
This was also my first big plugin with Java. So apologies if its all messy.  <br />

## Perms:
fnafmc.event - Grants big boy commands  <br />

## Commands:
/event join - Join a pre-start event.  <br /> 
/event create - Start an event count down and start the event  <br /> 
/event forcestart - Forcefully start the event skipping the countdown.  <br />
/event cancel - Cancel /event create, don't use if the count down has gone past or you used /event forcestart  <br />
/event forceend - End a already started and spawned in event.  <br />

/event disqualify <player> - Disqualify a player from the event.  <br /> 
/event respawn <all / seeker / hiders / player> - Respawn someone to the inital spawn.  <br />
/event status - View debug about the event regarding if the event is spawned, whos seeker, is it started, etc.  <br />
/event rigseeker <player> - Can be used after /event create and before /event forcestart or the end of the countdown to rig the seeker. (THIS WILL ANNOUNCE TO EVERYONE POST-SPAWN THAT IT WAS RIGGED.)  <br />
