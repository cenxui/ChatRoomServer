package test;

import javax.swing.JFrame;

import server.Server;

public class ServerTest {

	public static void main(String[] args) {
		Server sally = new Server();
		sally.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sally.startRunning();
	}

}
