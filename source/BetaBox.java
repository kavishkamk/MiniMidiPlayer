import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import java.awt.Label;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import java.util.ArrayList;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.sound.midi.Sequencer;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.InvalidMidiDataException;

public class BetaBox {
	
	private String[] instruments = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cumbal",
		"Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", 
		"Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};
	private ArrayList<JCheckBox> checkBoxList;
	
	public static void main(String[] args){
		// start interface in EDT
		EventQueue.invokeLater(new Runnable(){
			@Override
			public void run(){
				BetaBox obj = new BetaBox();
				obj.go();
				System.out.println("1 -> " + Thread.currentThread().getName());
				System.out.println("1 -> " + SwingUtilities.isEventDispatchThread());
			}
		});
	}
	
	public void go(){
		JFrame frame = new JFrame("Cyber BetaBox");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		Box buttonBox = new Box(BoxLayout.Y_AXIS);
		checkBoxList = new ArrayList<JCheckBox>();
		
		JButton start = new JButton("Start");
		start.addActionListener(new StartBtnhandle());
		buttonBox.add(start);
		
		JButton stop = new JButton("Stop");
		stop.addActionListener(new StopBtnHandler());
		buttonBox.add(stop);
		
		JButton tempoUp = new JButton("Tempo Up");
		tempoUp.addActionListener(new TempoUpBtnHandler());
		buttonBox.add(tempoUp);
		
		JButton tempoDown = new JButton("Tempo Down");
		tempoDown.addActionListener(new TempDownBtnHandler());
		buttonBox.add(tempoDown);
		
		Box nameBox = new Box(BoxLayout.Y_AXIS);
		
		for(int i = 0; i < instruments.length; i++){
			nameBox.add(new Label(instruments[i]));
		}
		
		background.add(BorderLayout.WEST, nameBox);
		background.add(BorderLayout.EAST, buttonBox);
		
		GridLayout grid = new GridLayout(16,16);
		grid.setHgap(2);
		grid.setVgap(1);
		JPanel gridPanel = new JPanel(grid);
		
		for(int i = 0; i < 256; i++){
			JCheckBox b = new JCheckBox();
			//b.setSelected(false);
			checkBoxList.add(b);
			gridPanel.add(b);
		}
		
		background.add(BorderLayout.CENTER, gridPanel);
		frame.getContentPane().add(background);
		
		setUpMidi();
		
		frame.setBounds(50,50,300,300);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void setUpMidi() {
		try{
			Sequencer sequencer = MidiSystem.getSequencer();
			sequencer.open();
			
			Sequence seq = new Sequence(Sequence.PPQ, 4);
			Track track = seq.createTrack();
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
	}
	
	public class StartBtnhandle implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event){
			
		}
	}
	
	public class StopBtnHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event){
		
		}
	}
	
	public class TempoUpBtnHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event){
			
		}
	}
	
	public class TempDownBtnHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event){
			
		}
	}
}