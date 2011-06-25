package de.jungblut.crawl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class StopGUIThread implements Runnable {

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void run() {
		final JFrame frame = new JFrame("STOP LOLZ");
		frame.setSize(200, 150);
		frame.setBounds(1000, 250, frame.getWidth(), frame.getHeight());
		JButton jButton = new JButton("STOP IT PLX");
		jButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("GUI said: STOP");
				SimpleCrawler.running = false;
				frame.setVisible(false);
				frame.dispose();
			}
		});
		frame.getContentPane().add(jButton);
		frame.setVisible(true);
	}

}
