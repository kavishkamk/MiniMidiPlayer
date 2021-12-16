# MiniMidiPlayer
This mini midi player created for test few java libraries
-
:arrow_down_small:Main Features
	* Create Tracks using java.sound.midi package
	* Create, Play, Stop, Tempo Up, Tempo down
	* GUI with swing and awt packages
	* Save Created Tracks(using Serialization) and reload saved Trackes (using Deserialization)
	* Simple client server architecture to handle multiple clients in local network
	* chat box (send created track with message over network and display on table. reservers can select on track and load and play)
	
:use the program
	* Compile two files
	* First run Music Server ( java MusicServer )
	* Then run BetaBox ( java BetaBox <userName> ) - user your user name when run.