import java.net.*;
import java.io.*;
import java.util.*;

public class MusicServer {
	
	private ArrayList<ObjectOutputStream> ossList;
	
	public static void main(String[] args){
		new MusicServer().go();
	}
	
	public void go(){
		
		ossList = new ArrayList<ObjectOutputStream>();
		
		try{
			ServerSocket serverSocket = new ServerSocket(4242);
			
			while(true){
				Socket socket = serverSocket.accept();
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				ossList.add(oos);
				
				Thread thread = new Thread(new ClientHandler(socket));
				thread.start();
			}
		}
		catch(Exception ex){ ex.printStackTrace(); }
	}
	
	public class ClientHandler implements Runnable {
		
		Socket soc;
		ObjectInputStream ois;
		
		public ClientHandler(Socket soc){
			try{
				this.soc = soc;
				ois = new ObjectInputStream(soc.getInputStream());
			}
			catch(Exception ex){ ex.printStackTrace(); }
		}
		
		@Override
		public void run(){
			
			Object ob1 = null;
			Object ob2 = null;
			
			try{
				while((ob1 = ois.readObject()) != null){
					ob2 = ois.readObject();
					tellEveryOne(ob1, ob2);
				}
			}
			catch(Exception ex){ ex.printStackTrace();};
		}
	}
	
	public void tellEveryOne(Object ob1, Object ob2){
		Iterator it = ossList.iterator();
		while(it.hasNext()) {
			try {
				ObjectOutputStream out = (ObjectOutputStream) it.next();
				out.writeObject(ob1);
				out.writeObject(ob2);
			}catch(Exception ex) {ex.printStackTrace();}
		}
	}
}