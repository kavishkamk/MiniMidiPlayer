import javax.sound.midi.Sequencer;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.InvalidMidiDataException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.io.*;

public class MiniMusicPlayer1 implements ControllerEventListener{
	
	static JFrame f = new JFrame("My first music video");
	static MyDrawPanel ml;

	public static void main(String[] args){
		MiniMusicPlayer1 obj = new MiniMusicPlayer1();
		obj.go();
	}
	
	public void setUpGui() {
		ml = new MyDrawPanel();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(ml);
		f.setBounds(30,30, 300,300);
		f.setVisible(true);
	}
		
	public void go(){
		setUpGui();
		
		try{
			Sequencer sequencer = MidiSystem.getSequencer();
			sequencer.open();
			
			sequencer.addControllerEventListener(ml, new int[] {127});
			
			Sequence seq = new Sequence(Sequence.PPQ, 4);
			Track track = seq.createTrack();
			
			int r = 0;
			for (int i = 5; i < 61; i+= 4) {
				r = (int) ((Math.random() * 50) + 1);
				track.add(makeEvent(144,1,r,100,i));
				track.add(makeEvent(176,1,127,0,i));
				track.add(makeEvent(128,1,r,100,i + 2));
			}
			
			sequencer.setSequence(seq);
			sequencer.start();
			sequencer.setTempoInBPM(120);
			
		}
		catch(MidiUnavailableException ex){
			System.out.println("Can not access Sequencer");
		}
		catch(SecurityException ex){
			System.out.println("Can not opan. Because of Security restriction");
		}
		catch(InvalidMidiDataException ex){
			System.out.println("Invalid data exception");
		}
		catch(IllegalStateException ex){
			System.out.println("Illegal State exception");
		}
	}
	
	public void controlChange(ShortMessage event) {
		System.out.println("la");
	}
	
	public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick){
		MidiEvent event = null;
		
		try{
			ShortMessage message = new ShortMessage();
			message.setMessage(comd, chan, one, two);
			event = new MidiEvent(message, tick);
		}
		catch(InvalidMidiDataException ex){
			System.out.println("Invalid midi data");
		}
		
		return event;
	}
	
	class MyDrawPanel extends JPanel implements ControllerEventListener {
		boolean msg = false;
		
		public void controlChange(ShortMessage event) {
			msg = true;
			repaint();
		}
		
		public void paintComponent(Graphics g) {
			if (msg) {
				Graphics2D g2 = (Graphics2D) g;
				int r = (int) (Math.random() * 250);
				int gr = (int) (Math.random() * 250);
				int b = (int) (Math.random() * 250);
				g.setColor(new Color(r,gr,b));
				int ht = (int) ((Math.random() * 120) + 10);
				int width = (int) ((Math.random() * 120) + 10);
				int x = (int) ((Math.random() * 40) + 10);
				int y = (int) ((Math.random() * 40) + 10);
				g.fillRect(x, y, width, ht);
				msg = false;
			} // close if
		} // close method
	} // close inner class
}