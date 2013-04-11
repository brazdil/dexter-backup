package uk.ac.cam.db538.dexter.gui;

import gr.zeus.ui.JMessage;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.Thread.State;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import lombok.val;
import uk.ac.cam.db538.dexter.apk.Apk;

import com.alee.extended.window.WebProgressDialog;
import com.alee.laf.menu.WebMenu;
import com.alee.laf.menu.WebMenuBar;
import com.alee.laf.menu.WebMenuItem;
import com.alee.laf.tabbedpane.WebTabbedPane;
import com.rx201.dx.translator.Scratchpad;

public class MainWindow {

  private JFrame frame;
  private JTabbedPane tabbedPane;

  public MainWindow() {
    initialize();
  }

  private void initialize() {
    frame = new JFrame("Dexter");
    frame.setBounds(100, 100, 800, 600);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    tabbedPane = new WebTabbedPane();
    frame.add(tabbedPane);

    val menubar = new WebMenuBar();
    {
      val menuFile = new WebMenu("File");
      menuFile.setMnemonic(KeyEvent.VK_F);
      {
        val menuFileOpen = new WebMenuItem("Open", KeyEvent.VK_O);
        menuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuFileOpen.addActionListener(Listener_FileOpen);
        menuFile.add(menuFileOpen);

        val menuFileInstrument = new WebMenuItem("Instrument", KeyEvent.VK_I);
        menuFileInstrument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
        menuFileInstrument.addActionListener(Listener_FileInstrument);
        menuFile.add(menuFileInstrument);

        val menuFileInstrumentDebug = new WebMenuItem("Instrument (debug)", KeyEvent.VK_D);
        menuFileInstrumentDebug.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        menuFileInstrumentDebug.addActionListener(Listener_FileInstrumentDebug);
        menuFile.add(menuFileInstrumentDebug);

        val menuFileSSATransform = new WebMenuItem("SSA Transform", KeyEvent.VK_T);
        menuFileSSATransform.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        menuFileSSATransform.addActionListener(Listener_FileSSATransform);
        menuFile.add(menuFileSSATransform);

        val menuFileSave = new WebMenuItem("Save", KeyEvent.VK_S);
        menuFileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuFileSave.addActionListener(Listener_FileSave);
        menuFile.add(menuFileSave);
      }
      menubar.add(menuFile);
    }
    frame.setJMenuBar(menubar);
  }

  private void doModal(String message, final Thread task) {
    // Load dialog
    val progress = new WebProgressDialog(frame, "");
    progress.setText(message);
    progress.setIndeterminate(true);
    progress.setShowProgressText(false);
    progress.setResizable(false);
    progress.setDefaultCloseOperation(WebProgressDialog.DO_NOTHING_ON_CLOSE);
//    progress.addWindowListener(new WindowListener)

    // Starting updater thread
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          task.start();
          do {
            Thread.sleep(1000);
          } while (task.getState() != State.TERMINATED);
        } catch (InterruptedException e) {
        } finally {
          progress.setVisible(false);
        }
      }
    }).start();

    progress.setModal(true);
    progress.setVisible(true);
  }

  private JFileChooser FileChooser = new JFileChooser(System.getProperty("user.dir"));

  private ActionListener Listener_FileOpen = new ActionListener() {

    @Override
    public void actionPerformed(ActionEvent arg0) {
      if (FileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
        return;

      String[] frameworkOptions = { "framework-2.3/", "framework-4.2/" };
      int choice = JOptionPane.showOptionDialog(
                     frame,
                     "Which framework do you want to use?",
                     "Framework",
                     JOptionPane.YES_NO_CANCEL_OPTION,
                     JOptionPane.QUESTION_MESSAGE,
                     null,
                     frameworkOptions,
                     frameworkOptions[0]);
      val framework = frameworkOptions[choice];

      val file = FileChooser.getSelectedFile();
      doModal("Loading " + file.getName(), new Thread(new Runnable() {
        public void run() {
          try {
            val splitPane = new FileTab(new Apk(file, new File(framework)), file.getName());
            tabbedPane.addTab(file.getName(), splitPane);
          } catch (Throwable e) {
            JMessage.showErrorMessage(frame, "A problem occurred while loading file \"" + file.getName() + "\".", e);
            return;
          }
        }
      }));
    }
  };

  private void performInstrumentation(final boolean debug) {
    val selected = tabbedPane.getSelectedComponent();
    if (selected == null)
      return;

    val tab = (FileTab) selected;
    val dex = tab.getOpenedFile().getDexFile();
    doModal("Instrumenting " + tab.getOpenedFile_Filename(), new Thread(new Runnable() {
      public void run() {
        try {
          val warnings = dex.instrument(debug);
          if (!warnings.isEmpty()) {
            val str = new StringBuilder();
            str.append("Warnings were produced during instrumentation:");
            for (val warning : warnings) {
              str.append("\n - ");
              str.append(warning.getMessage());
            }
            JMessage.showWarningMessage(frame, str.toString());
          }
          tab.getTreeListener().valueChanged(null);
          tab.updateClassTree();
        } catch (Throwable e) {
          JMessage.showErrorMessage(frame, "A problem occurred while instrumenting file \"" + tab.getOpenedFile_Filename() + "\".", e);
          return;
        }
      }
    }));
  }

  private ActionListener Listener_FileInstrument = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent arg0) {
      performInstrumentation(false);
    }
  };

  private ActionListener Listener_FileInstrumentDebug = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent arg0) {
      performInstrumentation(true);
    }
  };

  private ActionListener Listener_FileSSATransform = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent arg0) {
      val selected = tabbedPane.getSelectedComponent();
      if (selected == null)
        return;

      val tab = (FileTab) selected;
      val dex = tab.getOpenedFile().getDexFile();
      doModal("Transforming " + tab.getOpenedFile_Filename() + " into SSA", new Thread(new Runnable() {
        public void run() {
          try {
            dex.transformSSA();
            tab.getTreeListener().valueChanged(null);
            tab.updateClassTree();
          } catch (Throwable e) {
            JMessage.showErrorMessage(frame, "A problem occurred while SSA transforming file \"" + tab.getOpenedFile_Filename() + "\".", e);
            return;
          }
        }
      }));
    }
  };

  private ActionListener Listener_FileSave = new ActionListener() {

    @Override
    public void actionPerformed(ActionEvent arg0) {
      val selected = tabbedPane.getSelectedComponent();
      if (selected == null)
        return;

      if (FileChooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION)
        return;

      val tab = (FileTab) selected;
      val file = FileChooser.getSelectedFile();

      doModal("Saving " + file.getName(), new Thread(new Runnable() {
        public void run() {
          try {
            tab.getOpenedFile().writeToFile(file);
          } catch (Throwable e) {
            JMessage.showErrorMessage(frame, "A problem occurred while saving into file \"" + file.getName() + "\".", e);
            return;
          }
        }
      }));
    }
  };

  // MAIN FUNCTION

  public static void main(String[] args) {
	  Scratchpad.test();
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          MainWindow window = new MainWindow();
          window.frame.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }
}
