package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClientGUI {

	public static void main(String[] args) {
		new ChatClientGUI("localhost", 30000);
	}

	private URL url;
	private URLConnection uc;
	private BufferedReader reader;
	private JFrame frame = new JFrame("Chat"); // F�nstret

	private JPanel mainPanel = new JPanel();
	private JTextArea messages = new JTextArea();

	private JTextField textField2 = new JTextField();
	private JPanel buttonPanel = new JPanel();
	private JButton broadcastButton = new JButton("Broadcast");
	private JButton echoButton = new JButton("Echo");
	private JButton quitButton = new JButton("Quit");

	private Thread readThread;

	public ChatClientGUI(String host, int port) {
		quitButton.addActionListener(new QuitButtonListener());

		buttonPanel.setLayout(new GridLayout(1, 0));
		buttonPanel.add(broadcastButton);
		buttonPanel.add(echoButton);
		buttonPanel.add(quitButton);

		textField2.setText("text2");
		mainPanel.setLayout(null);
		mainPanel.add(messages);
		mainPanel.add(textField2);
		mainPanel.add(buttonPanel);
		messages.setBounds(0, 0, 700, 425);
		textField2.setBounds(0, 425, 700, 150);
		buttonPanel.setBounds(0, 575, 700, 100);

		frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
		frame.setSize(new Dimension(700, 700));
		frame.setLocationRelativeTo(null); // G�r s� att f�nstret hamnar mitt p�
											// sk�rmen
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setVisible(true);

		// G�r s� att f�nstret anropar quit() n�r det st�ngs.
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				quit();
			}
		});

		try {
			url = new URL("http://" + host + ":" + port);
			uc = url.openConnection();
			uc.setDoOutput(true);
			reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			quitButton.addActionListener(new QuitButtonListener());
			broadcastButton.addActionListener(new BroadcastButtonListener());
			echoButton.addActionListener(new EchoButtonListener());

			readThread = new Thread() {
				private LinkedList<String> messageList = new LinkedList<String>();

				public void run() {
					while (true) {
						try {
							String line = reader.readLine();
							if (line != null) {
								if (messageList.size() == 26) {
									messageList.removeFirst();
								}
								messageList.addLast(line);
								StringBuilder displayText = new StringBuilder();
								for (String message : messageList) {
									displayText.append(message + "\n");
								}
								messages.setText(displayText.toString());
							}
						} catch (IOException e) {
							System.exit(0);
						}
					}
				}
			};
			readThread.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void sendHTTPMessage(String mess) {
		try {
			//URL url = new URL("http://localhost:30000/?M=asd");
			//URLConnection uc = url.openConnection();
			//BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			System.out.println(mess);
			Writer writer = new OutputStreamWriter(new BufferedOutputStream(uc.getOutputStream()));
			writer.write(mess);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void sendMessage(String mess) {
		sendHTTPMessage(mess + "\r\n");
	}

	private void quit() {
		try {
			sendMessage("Q");
			reader.close();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private class QuitButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			quit();
		}
	}

	private class BroadcastButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("broadcast");
			sendMessage("M:" + textField2.getText());
		}

	}

	private class EchoButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			sendMessage("E:" + textField2.getText());
		}

	}

}
