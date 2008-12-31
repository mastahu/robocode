/*******************************************************************************
 * Copyright (c) 2001, 2008 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/cpl-v10.html
 *
 * Contributors:
 *     Mathew A. Nelson
 *     - Initial API and implementation
 *     Flemming N. Larsen
 *     - Added showInBrowser() for displaying content from an URL
 *     - Added showRoboWiki(), showYahooGroupRobocode(), showRobocodeRepository()
 *     - Removed the Thread.sleep(diff) from showSplashScreen()
 *     - Updated to use methods from the FileUtil, which replaces file operations
 *       that have been (re)moved from the robocode.util.Utils class
 *     - Changed showRobocodeFrame() to take a visible parameter
 *     - Added packCenterShow() for windows where the window position and
 *       dimension should not be read or saved to window.properties
 *     Luis Crespo & Flemming N. Larsen
 *     - Added showRankingDialog()
 *******************************************************************************/
package net.sf.robocode.ui;


import net.sf.robocode.IRobocodeManager;
import net.sf.robocode.battle.BattleProperties;
import net.sf.robocode.battle.BattleResultsTableModel;
import net.sf.robocode.io.FileUtil;
import net.sf.robocode.ui.battle.AwtBattleAdaptor;
import net.sf.robocode.ui.dialog.*;
import net.sf.robocode.ui.editor.RobocodeEditor;
import net.sf.robocode.ui.packager.RobotPackager;
import robocode.control.events.BattleCompletedEvent;
import robocode.control.events.IBattleListener;
import robocode.control.snapshot.ITurnSnapshot;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;


/**
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 * @author Luis Crespo (contributor)
 */
public class WindowManager implements IWindowManagerExt {

	private final static int TIMER_TICKS_PER_SECOND = 50;
	private final AwtBattleAdaptor awtAdaptor;
	private RobocodeEditor robocodeEditor;
	private RobotPackager robotPackager;
	private RobotExtractor robotExtractor;
	private RobocodeFrame robocodeFrame;
	private final IRobocodeManager manager;
	private RankingDialog rankingDialog;
	private IImageManager imageManager;
	private IRobotDialogManager robotDialogManager;

	public WindowManager() {
		this.manager = net.sf.robocode.core.Container.instance.getComponent(IRobocodeManager.class);
		awtAdaptor = new AwtBattleAdaptor(manager.getBattleManager(), TIMER_TICKS_PER_SECOND, true);

		// we will set UI better priority than robots and battle have
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 2);
				} catch (SecurityException ex) {// that's a pity
				}
			}
		});
	}

	public synchronized void addBattleListener(IBattleListener listener) {
		awtAdaptor.addListener(listener);
	}

	public synchronized void removeBattleListener(IBattleListener listener) {
		awtAdaptor.removeListener(listener);
	}

	public ITurnSnapshot getLastSnapshot() {
		return awtAdaptor.getLastSnapshot();
	}

	public int getFPS() {
		return awtAdaptor.getFPS();
	}

	public RobocodeFrame getRobocodeFrame() {
		if (robocodeFrame == null) {
			robocodeFrame = new RobocodeFrame(manager);
		}
		return robocodeFrame;
	}

	public void showRobocodeFrame(boolean visible, boolean iconified) {
		RobocodeFrame frame = getRobocodeFrame();

		if (iconified) {
			frame.setState(Frame.ICONIFIED);
		}
		
		if (visible) {
			// Pack frame to size all components
			WindowUtil.packCenterShow(frame);

			WindowUtil.setStatusLabel(frame.getStatusLabel());

			frame.checkUpdateOnStart();

		} else {
			frame.setVisible(false);
		}
	}

	public void showAboutBox() {
		packCenterShow(new AboutBox(getRobocodeFrame(), manager), true);
	}

	public String showBattleOpenDialog(final String defExt, final String name) {
		JFileChooser chooser = new JFileChooser(manager.getBattleManager().getBattlePath());

		chooser.setFileFilter(
				new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory()
						|| pathname.getName().toLowerCase().lastIndexOf(defExt.toLowerCase())
								== pathname.getName().length() - defExt.length();
			}

			@Override
			public String getDescription() {
				return name;
			}
		});

		if (chooser.showOpenDialog(getRobocodeFrame()) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getPath();
		}
		return null;
	}

	public String saveBattleDialog(String path, final String defExt, final String name) {
		File f = new File(path);

		JFileChooser chooser;

		chooser = new JFileChooser(f);

		javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory()
						|| pathname.getName().toLowerCase().lastIndexOf(defExt.toLowerCase())
								== pathname.getName().length() - defExt.length();
			}

			@Override
			public String getDescription() {
				return name;
			}
		};

		chooser.setFileFilter(filter);
		int rv = chooser.showSaveDialog(manager.getWindowManager().getRobocodeFrame());
		String result = null;

		if (rv == JFileChooser.APPROVE_OPTION) {
			result = chooser.getSelectedFile().getPath();
			int idx = result.lastIndexOf('.');
			String extension = "";

			if (idx > 0) {
				extension = result.substring(idx);
			}
			if (!(extension.equalsIgnoreCase(defExt))) {
				result += defExt;
			}
		}
		return result;
	}

	public void showVersionsTxt() {
		showInBrowser(
				"file://" + new File(FileUtil.getCwd(), "").getAbsoluteFile() + System.getProperty("file.separator")
				+ "versions.txt");
	}

	public void showHelpApi() {
		showInBrowser(
				"file://" + new File(FileUtil.getCwd(), "").getAbsoluteFile() + System.getProperty("file.separator")
				+ "javadoc" + System.getProperty("file.separator") + "index.html");
	}

	public void showFaq() {
		showInBrowser("http://robocode.sourceforge.net/help/robocode.faq.txt");
	}

	public void showOnlineHelp() {
		showInBrowser("http://robocode.sourceforge.net/help");
	}

	public void showJavaDocumentation() {
		showInBrowser("http://java.sun.com/j2se/1.5.0/docs");
	}

	public void showRobocodeHome() {
		showInBrowser("http://robocode.sourceforge.net");
	}

	public void showRoboWiki() {
		showInBrowser("http://robowiki.net");
	}

	public void showYahooGroupRobocode() {
		showInBrowser("http://groups.yahoo.com/group/robocode");
	}

	public void showRobocodeRepository() {
		showInBrowser("http://robocoderepository.com");
	}

	public void showOptionsPreferences() {
		try {
			manager.getBattleManager().pauseBattle();

			WindowUtil.packCenterShow(getRobocodeFrame(), new PreferencesDialog(manager));
		} finally {
			manager.getBattleManager().resumeIfPausedBattle(); // THIS is just dirty hack-fix of more complex problem with desiredTPS and pausing.  resumeBattle() belongs here.
		}
	}

	public void showResultsDialog(BattleCompletedEvent event) {
		packCenterShow(new ResultsDialog(manager, event.getSortedResults(), event.getBattleRules().getNumRounds()), true);
	}

	public void showRankingDialog(boolean visible) {
		if (rankingDialog == null) {
			rankingDialog = new RankingDialog(manager);
			if (visible) {
				packCenterShow(rankingDialog, true);
			} else {
				rankingDialog.dispose();
			}
		} else {
			if (visible) {
				packCenterShow(rankingDialog, false);
			} else {
				rankingDialog.dispose();
			}
		}
	}

	public void showRobocodeEditor() {
		if (robocodeEditor == null) {
			robocodeEditor = new net.sf.robocode.ui.editor.RobocodeEditor(manager);
			WindowUtil.packCenterShow(robocodeEditor);
		} else {
			robocodeEditor.setVisible(true);
		}
	}

	public void showRobotPackager() {
		if (robotPackager != null) {
			robotPackager.dispose();
			robotPackager = null;
		}

		robotPackager = new net.sf.robocode.ui.packager.RobotPackager(manager);
		WindowUtil.packCenterShow(robotPackager);
	}

	public void showRobotExtractor(JFrame owner) {
		if (robotExtractor != null) {
			robotExtractor.dispose();
			robotExtractor = null;
		}

		robotExtractor = new net.sf.robocode.ui.dialog.RobotExtractor(owner, manager);
		WindowUtil.packCenterShow(robotExtractor);
	}

	public void showSplashScreen() {
		RcSplashScreen splashScreen = new RcSplashScreen(manager);

		packCenterShow(splashScreen, true);

		WindowUtil.setStatusLabel(splashScreen.getSplashLabel());

		manager.getRepositoryManager().loadRobotRepository();

		WindowUtil.setStatusLabel(splashScreen.getSplashLabel());
		getImageManager();
		manager.getCpuManager().getCpuConstant();

		WindowUtil.setStatus("");
		WindowUtil.setStatusLabel(null);

		splashScreen.dispose();
	}

	public void showNewBattleDialog(BattleProperties battleProperties) {
		try {
			manager.getBattleManager().pauseBattle();
			WindowUtil.packCenterShow(getRobocodeFrame(), new NewBattleDialog(manager, battleProperties));
		} finally {
			manager.getBattleManager().resumeBattle();
		}
	}

	public boolean closeRobocodeEditor() {
		return robocodeEditor == null || !robocodeEditor.isVisible() || robocodeEditor.close();
	}

	/**
	 * Gets the manager.
	 *
	 * @return Returns a RobocodeManager
	 */
	public IRobocodeManager getManager() {
		return manager;
	}

	public void showCreateTeamDialog() {
		TeamCreator teamCreator = new TeamCreator(manager);

		WindowUtil.packCenterShow(teamCreator);
	}

	public void showImportRobotDialog() {
		JFileChooser chooser = new JFileChooser();

		chooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isHidden()) {
					return false;
				}
				if (pathname.isDirectory()) {
					return true;
				}
				String filename = pathname.getName();

				if (filename.equals("robocode.jar")) {
					return false;
				}
				int idx = filename.lastIndexOf('.');

				String extension = "";

				if (idx >= 0) {
					extension = filename.substring(idx);
				}
				return extension.equalsIgnoreCase(".jar") || extension.equalsIgnoreCase(".zip");
			}

			@Override
			public String getDescription() {
				return "Jar Files";
			}
		});

		chooser.setDialogTitle(
				"Select the robot .jar file to copy to " + manager.getRepositoryManager().getRobotsDirectory());

		if (chooser.showDialog(getRobocodeFrame(), "Import") == JFileChooser.APPROVE_OPTION) {
			File inputFile = chooser.getSelectedFile();
			String fileName = chooser.getSelectedFile().getName();
			int idx = fileName.lastIndexOf('.');
			String extension = "";

			if (idx >= 0) {
				extension = fileName.substring(idx);
			}
			if (!extension.equalsIgnoreCase(".jar")) {
				fileName += ".jar";
			}
			File outputFile = new File(manager.getRepositoryManager().getRobotsDirectory(), fileName);

			if (inputFile.equals(outputFile)) {
				JOptionPane.showMessageDialog(getRobocodeFrame(),
						outputFile.getName() + " is already in the robots directory!");
				return;
			}
			if (outputFile.exists()) {
				if (JOptionPane.showConfirmDialog(getRobocodeFrame(), outputFile + " already exists.  Overwrite?",
						"Warning", JOptionPane.YES_NO_OPTION)
						== JOptionPane.NO_OPTION) {
					return;
				}
			}
			if (JOptionPane.showConfirmDialog(getRobocodeFrame(),
					"Robocode will now copy " + inputFile.getName() + " to " + outputFile.getParent(), "Import robot",
					JOptionPane.OK_CANCEL_OPTION)
					== JOptionPane.OK_OPTION) {
				try {
					FileUtil.copy(inputFile, outputFile);
					manager.getRepositoryManager().clearRobotList();
					JOptionPane.showMessageDialog(getRobocodeFrame(), "Robot imported successfully.");
				} catch (IOException e) {
					JOptionPane.showMessageDialog(getRobocodeFrame(), "Import failed: " + e);
				}
			}
		}
	}

	/**
	 * Shows a web page using the browser manager.
	 *
	 * @param url The URL of the web page
	 */
	private void showInBrowser(String url) {
		try {
			BrowserManager.openURL(url);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(getRobocodeFrame(), e.getMessage(), "Unable to open browser!",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void showSaveResultsDialog(BattleResultsTableModel tableModel) {
		JFileChooser chooser = new JFileChooser();

		chooser.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (pathname.isHidden()) {
					return false;
				}
				if (pathname.isDirectory()) {
					return true;
				}
				String filename = pathname.getName();
				int idx = filename.lastIndexOf('.');

				String extension = "";

				if (idx >= 0) {
					extension = filename.substring(idx);
				}
				return extension.equalsIgnoreCase(".csv");
			}

			@Override
			public String getDescription() {
				return "Comma Separated Value (CSV) File Format";
			}
		});

		chooser.setDialogTitle("Save battle results");

		if (chooser.showSaveDialog(getRobocodeFrame()) == JFileChooser.APPROVE_OPTION) {

			String filename = chooser.getSelectedFile().getPath();

			if (!filename.endsWith(".csv")) {
				filename += ".csv";
			}

			boolean append = manager.getProperties().getOptionsCommonAppendWhenSavingResults();

			tableModel.saveToFile(filename, append);
		}
	}

	/**
	 * Packs, centers, and shows the specified window on the screen.
	 * @param window the window to pack, center, and show
	 * @param center {@code true} if the window must be centered; {@code false} otherwise
	 */
	private void packCenterShow(Window window, boolean center) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		window.pack();
		if (center) {
			window.setLocation((screenSize.width - window.getWidth()) / 2, (screenSize.height - window.getHeight()) / 2);
		}
		window.setVisible(true);
	}

	public void cleanup() {
		if (manager.isGUIEnabled()) {
			getRobocodeFrame().dispose();
		}
	}

	public void setStatus(String s) {
		WindowUtil.setStatus(s);
	}

	public boolean isIconified() {
		return getRobocodeFrame().isIconified();
	}

	public IImageManager getImageManager() {
		if (imageManager == null) {
			imageManager = new ImageManager();
		}
		return imageManager;
	}

	public IRobotDialogManager getRobotDialogManager() {
		if (robotDialogManager == null) {
			robotDialogManager = new RobotDialogManager();
		}
		return robotDialogManager;
	}

	/**
	 * Sets the Look and Feel (LAF). This method first try to set the LAF to the
	 * system's LAF. If this fails, it try to use the cross platform LAF.
	 * If this also fails, the LAF will not be changed.
	 */
	public void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable t) {
			// Work-around for problems with setting Look and Feel described here:
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6468089
			Locale.setDefault(Locale.US);

			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Throwable t2) {
				// For some reason Ubuntu 7 can cause a NullPointerException when trying to getting the LAF
				System.err.println("Could not set the Look and Feel (LAF).  The default LAF is used instead");
			}
		}
	}

}