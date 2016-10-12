package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Servlet implements Callable<Void> {
	
	final Queue<String> queue = new ConcurrentLinkedQueue<>();
	
	private final Server server;
	private final Socket connection;
	private final String clientIP;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private boolean cancel = false;
	
	/**
	 * constructor
	 * @param connection
	 * @param server
	 */
	public Servlet(Socket connection, Server server) {
		this.connection = connection;
		this.clientIP = connection.getInetAddress().getHostName();
		this.server = server;

		try {
			this.output =  new ObjectOutputStream(this.connection.getOutputStream());
			this.output.flush();
			this.input = new ObjectInputStream(this.connection.getInputStream());		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Void call() throws Exception {
		try {
			new Thread(()-> {
				while (true) {
					if (queue.isEmpty() == false) {					
						try {
							this.output.writeObject(this.clientIP + " - " + this.queue.poll());
							this.output.flush();
						} catch (Exception e) {
							e.printStackTrace();
						}		
					}
				}	
			}).start();
			
			while (this.cancel == false) {
				String message = (String) this.input.readObject();
				if (message != null) {
					this.server.addMessageQueue(message);
				}			
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.output.close();
			this.input.close();
			this.connection.close(); 
			this.server.removeServlet(this);
		}
		return null;
	}
	
	void addMessage(String message) {
		this.server.addMessageQueue(message);
	}
	
	void cancel() {
		this.cancel = true;
	}
}
