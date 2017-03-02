package com.ligure.aws;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import com.actionsoft.application.server.PBE;
import com.actionsoft.awf.util.Base64;

public class LicenseUtil extends JFrame {

    private static final long serialVersionUID = -6652034043875518641L;

    private static final String KEY2 = "JVM INSTR swap";
    private static final String KEY1 = "~O~Y~ACTIONSOFT~@~";

    JTextField srcFileBox;
    JButton srcFileButton;
    JTextArea srcDataBox;
    JTextField tarFileBox;
    JButton tarFileButton;
    JButton makeLicButton;

    public LicenseUtil() {
	super();
	init();
    }

    private void init() {
	final JFrame topFrame = this;
	setLayout(new BorderLayout(0, 0));
	JPanel panel = new JPanel();
	panel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),
		"LicenseUtil"));
	add(panel, BorderLayout.NORTH);
	panel.setLayout(new GridBagLayout());

	srcFileBox = new JTextField();
	srcFileButton = new JButton("old");
	srcFileButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent event) {
		JFileChooser jfc = new JFileChooser();
		if (jfc.showOpenDialog(topFrame) == JFileChooser.APPROVE_OPTION) {
		    File f = jfc.getSelectedFile();
		    srcFileBox.setText(f.getAbsolutePath());
		    try {
			FileInputStream fis = new FileInputStream(f);
			int i = fis.available();
			byte[] dat = new byte[i];
			fis.read(dat);
			fis.close();
			srcDataBox.setText(new String(PBE.decrypt(Base64
				.decode(PBE.decrypt(Base64.decode(dat), KEY2)),
				KEY1), "utf-8"));
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    }
	});
	tarFileBox = new JTextField();
	tarFileButton = new JButton("new");
	tarFileButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent event) {
		JFileChooser jfc = new JFileChooser();
		if (jfc.showOpenDialog(topFrame) == JFileChooser.APPROVE_OPTION) {
		    tarFileBox.setText(jfc.getSelectedFile().getAbsolutePath());
		}
	    }
	});

	GridBagConstraints gbcLabel = new GridBagConstraints();
	gbcLabel.gridx = 0;
	gbcLabel.anchor = GridBagConstraints.EAST;
	GridBagConstraints gbcText = new GridBagConstraints();
	gbcText.gridx = 1;
	gbcText.gridwidth = 1;
	gbcText.weightx = 1;
	gbcText.fill = GridBagConstraints.HORIZONTAL;
	GridBagConstraints gbcButton = new GridBagConstraints();
	gbcButton.gridx = 2;
	gbcButton.anchor = GridBagConstraints.WEST;

	gbcLabel.gridy = gbcText.gridy = gbcButton.gridy = 0;
	panel.add(new JLabel("Old License"), gbcLabel);
	panel.add(srcFileBox, gbcText);
	panel.add(srcFileButton, gbcButton);
	gbcLabel.gridy = gbcText.gridy = gbcButton.gridy = 1;
	panel.add(new JLabel("New License"), gbcLabel);
	panel.add(tarFileBox, gbcText);
	panel.add(tarFileButton, gbcButton);

	srcDataBox = new JTextArea();
	srcDataBox.setRows(10);
	srcDataBox.setLineWrap(true);
	gbcText.gridx = 0;
	gbcText.gridy = 2;
	gbcText.gridwidth = 3;
	panel.add(srcDataBox, gbcText);

	makeLicButton = new JButton("Create License");
	makeLicButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent event) {
		try {
		    String path = tarFileBox.getText();
		    String data = srcDataBox.getText();
		    if (path.length() > 0 && data.length() > 0) {
			if (path.endsWith(File.separator)) {
			    path += "license.dat";
			}
			File newLic = new File(path);
			if (!newLic.exists()) {
			    if (!newLic.getParentFile().exists()) {
				newLic.getParentFile().mkdirs();
			    }
			    newLic.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(
				newLic));
			bw.write(new String(Base64.encode(PBE.encrypt(Base64
				.encode(PBE.encrypt(data.getBytes(), KEY1)),
				KEY2)), "utf-8"));
			bw.flush();
			bw.close();
		    } else if (path.length() == 0) {
			JOptionPane.showMessageDialog(topFrame,
				"choose old license!", "error",
				JOptionPane.WARNING_MESSAGE);
		    } else {
			JOptionPane.showMessageDialog(topFrame,
				"choose new license!", "error",
				JOptionPane.WARNING_MESSAGE);
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
	gbcButton.gridy = 3;
	panel.add(makeLicButton, gbcButton);

    }

    public static void main(String[] args) {
	try {
	    String lafClass = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	    UIManager.setLookAndFeel(lafClass);
	} catch (Exception e) {
	    try {
		UIManager.setLookAndFeel(UIManager
			.getSystemLookAndFeelClassName());
	    } catch (Exception ee) {
		System.out.println("Error setting native LAF: " + ee);
	    }
	}
	LicenseUtil lic = new LicenseUtil();
	lic.setTitle("LicenseUtil V0.1");
	lic.setSize(600, 500);
	lic.centerWindow();
	lic.setDefaultCloseOperation(EXIT_ON_CLOSE);
	lic.setVisible(true);
    }

    private void centerWindow() {
	Dimension dim = getToolkit().getScreenSize();
	setLocation((dim.width - getWidth()) / 2,
		(dim.height - getHeight()) / 2);
    }

}
