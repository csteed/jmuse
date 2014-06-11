import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/* testing */

public class jMUSE extends JFrame implements ActionListener, DataFileListener {
	private static final String APP_TITLE = "jMUSE";

	private File lastFileDirectory = new File(
			"/Users/chad/Documents/workspace/jmuse/data/");
	private MatrixVisPanel visPanel;

	public jMUSE() {
		setTitle(APP_TITLE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		createMainPanel();
		pack();
		setLocationRelativeTo(null);
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals("open file")) {
			openFile();
		}
	}

	private void openFile() {
		JFileChooser chooser = new JFileChooser(lastFileDirectory);
		chooser.setDialogTitle("Open .csv File");
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ret = chooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();

			DataFile df = new DataFile(selectedFile);
			df.addDataFileListener(this);

			Thread thread = new Thread(df);
			thread.start();
		}
	}

	public void dataFileReadStarted() {
		// start an indefinite progress bar
		System.out.println("jMUSE.dataFileReadStarted(): Entered method.");
	}

	public void dataFileReadFinished(DataFile df) {
		// stop the indefinite progress bar

		// add file data to the view panel.
		System.out.println("jMUSE.dataFileReadFinished(): Entered method.");

		ArrayList variableNames = df.getVariableNames();
		ArrayList variableData = new ArrayList();
		for (int i = 0; i < df.getVariableCount(); i++) {
			ArrayList dataList = df.getVariableValues(i);
			double[] data = new double[dataList.size()];
			int count = 0;
			Iterator iter = dataList.iterator();
			while (iter.hasNext()) {
				data[count++] = ((Number) iter.next()).doubleValue();
			}
			variableData.add(data);
		}

		visPanel.addVariables(variableNames, variableData);
	}

	private void createMainPanel() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu menu = new JMenu("File");
		JMenuItem mi = new JMenuItem("Open", KeyEvent.VK_O);
		mi.addActionListener(this);
		mi.setActionCommand("open file");
		menu.add(mi);

		menu.addSeparator();

		mi = new JMenuItem("Exit", KeyEvent.VK_X);
		mi.addActionListener(this);
		mi.setActionCommand("exit");
		menu.add(mi);
		menuBar.add(menu);

		visPanel = new MatrixVisPanel();

		JTabbedPane settingsPane = new JTabbedPane();

		JSplitPane horizontalSplit = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, settingsPane, visPanel);
		horizontalSplit.setDividerLocation(20);
		horizontalSplit.setOneTouchExpandable(true);

		JPanel mainPanel = (JPanel) getContentPane();
		mainPanel.add(horizontalSplit, BorderLayout.CENTER);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		((JPanel) getContentPane()).setPreferredSize(new Dimension(
				(int) (dim.height * .9), (int) (dim.height * .9)));

	}

	public static void main(String args[]) throws Exception {
		jMUSE app = new jMUSE();
		app.setVisible(true);
	}
}
