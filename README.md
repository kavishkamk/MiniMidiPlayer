# MiniMidiMusicPlayer
This mini midi music player created for test few java libraries

## Main Features
- Create Tracks using java.sound.midi package
- Track create, Play, Stop, Tempo Up, Tempo down
- GUI with swing and awt packages
- Save Created Tracks(using Serialization) and reload saved Trackes (using Deserialization)
- Simple client server architecture to handle multiple clients in local network
- chat box (send created tracks with message over network and display on chat. reservers can select on track and load and play)
	
## use the program
- Compile two .java files
- First run Music Server ( java MusicServer )
- Then run BetaBox ( java BetaBox <userName> )
