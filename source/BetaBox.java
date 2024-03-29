import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.ListSelectionModel;
import java.util.ArrayList;
import java.awt.event.*;
import javax.sound.midi.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.event.*;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.LookAndFeel;

public class BetaBox {
	
	private String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cumbal",
		"Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", 
		"Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};
	private int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63}; // instruments keys
	public ArrayList<JCheckBox> checkBoxList;
	private Track track;
	private Sequence seq;
	private Sequencer sequencer;
	private JFrame frame;
	private JTextField sendMsg;
	private String userName;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private HashMap<String, boolean[]> otherSeqMap = new HashMap<String, boolean[]>();
	private Vector<String> listVector = new Vector<String>();
	private JList<String> incomingList;
	
	public static void main(String[] args){
		
		new BetaBox().startUp((args.length > 0) ? args[0] : "user " + (int) (Math.random() * 100000));
	}
	
	public void startUp(String uName){
		userName = uName;
		
		try{
			// set up connection
			Socket socket = new Socket("localhost", 4242);
			ois = new ObjectInputStream(socket.getInputStream());
			oos = new ObjectOutputStream(socket.getOutputStream());
			Thread readerThread = new Thread(new RemoteReader());
			readerThread.start();
		}
		catch(Exception ex){
			System.out.println("Cannot join with the server. running in offline");
		}
		
		setUpMidi();
		
		// start running GUI
		EventQueue.invokeLater(new Runnable(){
			@Override
			public void run(){
				// added nimbus look and feel
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						try {
							UIManager.setLookAndFeel(info.getClassName());
						} catch(Exception ex) {
							ex.printStackTrace();
						}
						break;
					}
				}
				go();
			}
		});
	}
	
	public void go(){
		
		frame = new JFrame("Cyber BetaBox");
		frame.setLayout(new BorderLayout(10, 5));
		frame.setIconImage(new ImageIcon("logo.png").getImage());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		Box buttonBox = new Box(BoxLayout.Y_AXIS);
		checkBoxList = new ArrayList<JCheckBox>();
		
		// set buttons, register with action listener and add to the interface
		
		Dimension de = new Dimension(0,2);
		
		// start the track
		// when start, it go through all the check boxes and read the data and then create track and play it as a loop
		JButton start = new JButton("Start");
		start.setFocusable(false);
		start.addActionListener(new StartBtnhandle());
		buttonBox.add(start);
		buttonBox.add(Box.createRigidArea(de));
		
		JButton stop = new JButton("Stop");
		stop.setFocusable(false);
		stop.addActionListener(new StopBtnHandler());
		buttonBox.add(stop);
		buttonBox.add(Box.createRigidArea(de));
		
		JButton tempoUp = new JButton("Tempo Up");
		tempoUp.setFocusable(false);
		tempoUp.addActionListener(new TempoUpBtnHandler());
		buttonBox.add(tempoUp);
		buttonBox.add(Box.createRigidArea(de));
		
		JButton tempoDown = new JButton("Tempo Down");
		tempoDown.setFocusable(false);
		tempoDown.addActionListener(new TempDownBtnHandler());
		buttonBox.add(tempoDown);
		buttonBox.add(Box.createRigidArea(de));
		
		JButton serialize = new JButton("SerializeIt");
		serialize.setFocusable(false);
		serialize.setMnemonic(KeyEvent.VK_S);
		serialize.addActionListener(new MySendListener());
		buttonBox.add(serialize);
		buttonBox.add(Box.createRigidArea(de));
		
		JButton restore = new JButton("Restore");
		restore.setFocusable(false);
		restore.addActionListener(new MyReadInListener());
		buttonBox.add(restore);
		buttonBox.add(Box.createRigidArea(de));
		
		JButton send = new JButton("Send");
		send.setFocusable(false);
		send.addActionListener(new SendOverConnectionListener());
		buttonBox.add(send);
		
		sendMsg = new JTextField();
		buttonBox.add(sendMsg);
		buttonBox.add(Box.createRigidArea(de));
		
		incomingList = new JList<String>();
		incomingList.addListSelectionListener(new MyListSelectionListener());
		incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane listScroller = new JScrollPane(incomingList);
		buttonBox.add(listScroller);
		buttonBox.add(Box.createRigidArea(de));
		incomingList.setListData(listVector);
		
		Box nameBox = new Box(BoxLayout.Y_AXIS);
		
		// set instruments names
		for(int i = 0; i < instrumentNames.length; i++){
			nameBox.add(new Label(instrumentNames[i]));
		}
		
		background.add(BorderLayout.WEST, nameBox);
		background.add(BorderLayout.EAST, buttonBox);
		
		GridLayout grid = new GridLayout(16,16);
		grid.setHgap(2);
		grid.setVgap(1);
		JPanel gridPanel = new JPanel(grid);
		
		// create check box, set to the UI and add to the list
		for(int i = 0; i < 256; i++){
			JCheckBox b = new JCheckBox();
			//b.setSelected(false);
			checkBoxList.add(b);
			gridPanel.add(b);
		}
		
		background.add(BorderLayout.CENTER, gridPanel);
		frame.getContentPane().add(background);
		
		frame.setBounds(50,50,300,300);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	// set the midi sequencer
	public void setUpMidi() {
		try{
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			
			seq = new Sequence(Sequence.PPQ, 4);
			track = seq.createTrack();
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
	
	// go through check boxes anc create track and start
	public void buildTrackAndStart(){
		int[] trackList = null;
		
		seq.deleteTrack(track);
		track = seq.createTrack();
		
		for(int i = 0; i < 16; i++){
			trackList = new int[16];
			int key = instruments[i];
			
			for(int j = 0; j < 16; j++){
				JCheckBox cb = checkBoxList.get(j + 16*i);
				if(cb.isSelected()){
					trackList[j] = key;
				}
				else{
					trackList[j] = 0;
				}
			}
			makeTracks(trackList);
			track.add(makeEvent(176,1,127,0,16));
		}
		track.add(makeEvent(192,9,1,0,15));
		
		try {
			sequencer.setSequence(seq);
			sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
			sequencer.setTempoInBPM(120);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	// create midi events
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
	
	// create track
	public void makeTracks(int[] list) {
		for (int i = 0; i < 16; i++) {
			int key = list[i];
			if (key != 0) {
				track.add(makeEvent(144,9,key, 100, i));
				track.add(makeEvent(128,9,key, 100, i+1));
			}
		}
	}
	
	// start button handle
	public class StartBtnhandle implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event){
			buildTrackAndStart();
		}
	}
	
	// to stort the sequencer
	public class StopBtnHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event){
			sequencer.stop();
		}
	}
	
	// increase tempo by 3% for each click
	public class TempoUpBtnHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event){
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor * 1.03));
		}
	}
	
	// dicrease tempo by 3% for each click
	public class TempDownBtnHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event){
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor * .97));
		}
	}
	
	// this class is used to store parttens
	private class MySendListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent event){
			
			boolean[] checkBoxStates = new boolean[256];
			
			for(int i = 0; i < 256; i++)
				if(checkBoxList.get(i).isSelected())
					checkBoxStates[i] = true;
			
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("serialized files", "sar");
			chooser.setFileFilter(filter);
			int val = chooser.showSaveDialog(frame);
			if(val == JFileChooser.APPROVE_OPTION){
				try{
					ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(chooser.getSelectedFile().getName() + ".sar"));
					os.writeObject(checkBoxStates);
					os.close();
				}
				catch(NullPointerException ex){
					System.out.println("Pleace choose a File");
					ex.printStackTrace();
				}
				catch(FileNotFoundException ex){
					System.out.println("Cannot Open");
					ex.printStackTrace();
				}
				catch(IOException ex){
					ex.printStackTrace();
				}
			}
			
		}
	}
	
	// restore previous track
	private class MyReadInListener implements ActionListener {
		
		boolean[] checkBoxStatus = null;
		
		@Override
		public void actionPerformed(ActionEvent event){
			
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("serialized files", "sar");
			chooser.setFileFilter(filter);
			int val = chooser.showOpenDialog(frame);
			if(val == JFileChooser.APPROVE_OPTION){
				try{
					ObjectInputStream is = new ObjectInputStream(new FileInputStream(chooser.getSelectedFile()));
					checkBoxStatus = (boolean []) is.readObject();
					is.close();
				}
				catch(ClassNotFoundException ex){
					ex.printStackTrace();
				}
				catch(NullPointerException ex){
					System.out.println("Please choose a File");
					ex.printStackTrace();
				}
				catch(FileNotFoundException ex){
					System.out.println("Cannot Open");
					ex.printStackTrace();
				}
				catch(IOException ex){
					ex.printStackTrace();
				}
				
				for(int i = 0; i < 256; i++){
					if(checkBoxStatus[i] == true){
						checkBoxList.get(i).setSelected(true);
					}
					else{
						checkBoxList.get(i).setSelected(false);
					}
				}
				sequencer.stop();
				buildTrackAndStart();
			}
		}
	}
	
	// this class for read message over network
	private class RemoteReader implements Runnable {
		
		private Object resObj = null;
		private boolean[] resCheckBoxStatus = null;
		
		@Override
		public void run(){
			try{
				while((resObj = ois.readObject()) != null){
					String nameShow = (String) resObj;
					resCheckBoxStatus = (boolean[]) ois.readObject();
					otherSeqMap.put(nameShow, resCheckBoxStatus);
					listVector.add(nameShow);
					incomingList.setListData(listVector);
				}
			}
			catch(Exception ex){
				
			}
		}
	}
	
	// broad cast track with the user message type to the users
	private class SendOverConnectionListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent event){
			
			boolean[] checkBoxStates = new boolean[256];
			
			for(int i = 0; i < 256; i++)
				if(checkBoxList.get(i).isSelected())
					checkBoxStates[i] = true;
			
			
			String mess = null;
			
			try{
				oos.writeObject(userName + " : " + sendMsg.getText());
				oos.writeObject(checkBoxStates);
			}
			catch(Exception ex){
				System.out.println("Sorry. Cannot send to the server");
			}
		}
	}
	
	// get track from selected list
	public class MyListSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent le) {
			if (!le.getValueIsAdjusting()) {
				String selected = (String) incomingList.getSelectedValue();
				if (selected != null) {
					// now go to the map, and change the sequence
					boolean[] selectedState = (boolean[]) otherSeqMap.get(selected);
					changeSequence(selectedState);
					sequencer.stop();
					buildTrackAndStart();
				}
			}
		}
	}
	
	// set given track
	public void changeSequence(boolean[] checkboxState) {
		for (int i = 0; i < 256; i++) {
			JCheckBox check = (JCheckBox) checkBoxList.get(i);
			if (checkboxState[i]) {
				check.setSelected(true);
			}
			else {
				check.setSelected(false);
			}
		}
	}
}