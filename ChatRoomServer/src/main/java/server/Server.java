package server;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.*;
import javax.swing.*;

public class Server extends JFrame {
	private final JTextField userText;
	private final JTextArea chatWindow;
	private final ExecutorService service;
	
	private ServerSocket server;
	private boolean running = true;
	
	private List<Servlet> servlets = new LinkedList<>();
	
	/*
	 * constructor
	 */
	public Server() {
		super("Buckys Instant Messege");
		this.userText = new JTextField();
		this.userText.setEditable(false);
		this.userText.addActionListener((e) -> {
			sendMessage(e.getActionCommand());
			this.userText.setText("");
		});
		add(this.userText, BorderLayout.NORTH);
		this.chatWindow = new JTextArea();
		add(new JScrollPane(this.chatWindow));
		setSize(300, 150);
		setVisible(true);
		
		this.service = Executors.newCachedThreadPool();
	}

	/**
	 * set up and run the server
	 */
	public void startRunning() {
		try {
			this.server = new ServerSocket(6789, 100);
			while (this.running == true) {
				try {
					showMessage(" Server is waiting for someone to connect...\n");
					waitForConnection();
				} catch (EOFException e) {
					showMessage("\n Server end the connection");
				} 
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeCrap();
		}
	}
	
	void addMessageQueue(String message) {	
		synchronized (this.servlets) {
			if (this.servlets.size() == 0) {
				return;
			}
			for (Servlet servlet: this.servlets) {
				servlet.queue.add(message);
			}
		}
	}
	
	void removeServlet(Servlet servlet) {
		synchronized (this.servlets) {
			this.servlets.remove(servlet);
		}
	}

	private void waitForConnection() throws IOException {	
		Socket connection = this.server.accept();
		Servlet servlet = new Servlet(connection, this);
		this.servlets.add(servlet);
		this.service.submit(servlet);
		ableToType(true);
		showMessage(connection.getInetAddress().getHostName() + " joined!\n");
	}

	/**
	 * close streams and sockets after you are done chatting
	 */
	private void closeCrap() {
		showMessage("\n Closing connections... \n");
		ableToType(false);
		this.service.shutdownNow();
	}

	/**
	 * send a message to client
	 * 
	 * @param message
	 */
	private void sendMessage(String message) {		
		showMessage(message);
		addMessageQueue(message);
	}
	/**
	 * update chat window
	 * @param text
	 */
	private void showMessage(final String text) {
		SwingUtilities.invokeLater(() -> {
			this.chatWindow.append(text);
		});
	}
	/**
	 * let the user type stuff into their box
	 * @param tof
	 */
	private void ableToType(final boolean tof) {
		SwingUtilities.invokeLater(()->{
			this.userText.setEditable(tof);
		});
	}
}
