package uk.ac.cam.db538.dexter.gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import com.alee.extended.window.WebProgressDialog;
import com.alee.laf.menu.WebMenu;
import com.alee.laf.menu.WebMenuBar;
import com.alee.laf.menu.WebMenuItem;
import com.alee.laf.tabbedpane.WebTabbedPane;

public class MainWindow {

  private JFrame Frame;
  private JTabbedPane TabbedPane;

  public MainWindow() {
    initialize();
  }

  private void initialize() {
    Frame = new JFrame("Dexter");
    Frame.setBounds(100, 100, 800, 600);
    Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    TabbedPane = new WebTabbedPane();
    Frame.add(TabbedPane);

    val menubar = new WebMenuBar();
    {
      val menuFile = new WebMenu("File");
      menuFile.setMnemonic(KeyEvent.VK_F);
      {
        val menuFileOpen = new WebMenuItem("Open", KeyEvent.VK_O);
        menuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuFileOpen.addActionListener(new ActionListener() {
          private JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));

          @Override
          public void actionPerformed(ActionEvent arg0) {
            if (fc.showOpenDialog(Frame) == JFileChooser.APPROVE_OPTION) {
              val file = fc.getSelectedFile();
              doModal("Loading " + file.getName(), new Thread(new Runnable() {
                public void run() {
                  try {
                    openFile(file);
                  } catch (IOException | UnknownTypeException
                  | DexInstructionParsingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                }
              }));
            }
          }
        } );
        menuFile.add(menuFileOpen);

        val menuFileInstrument = new WebMenuItem("Instrument", KeyEvent.VK_I);
        menuFileInstrument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
        menuFileInstrument.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent arg0) {
            val selected = TabbedPane.getSelectedComponent();
            if (selected != null) {
              val tab = (FileTab) selected;
              tab.getOpenedFile().instrument();
              tab.getTreeListener().valueChanged(null);
            }
          }
        } );
        menuFile.add(menuFileInstrument);

        val menuFileSave = new WebMenuItem("Save", KeyEvent.VK_S);
        menuFileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuFileSave.addActionListener(new ActionListener() {
          private JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));

          @Override
          public void actionPerformed(ActionEvent arg0) {
            val selected = TabbedPane.getSelectedComponent();
            if (selected != null) {
              val tab = (FileTab) selected;
              if (fc.showSaveDialog(Frame) == JFileChooser.APPROVE_OPTION) {
                val file = fc.getSelectedFile();
                doModal("Saving " + file.getName(), new Thread(new Runnable() {
                  public void run() {
                    try {
                      tab.getOpenedFile().writeToFile(file);
                    } catch (IOException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                    }
                  }
                }));
              }
            }
          }
        } );
        menuFile.add(menuFileSave);
      }
      menubar.add(menuFile);
    }
    Frame.setJMenuBar(menubar);
  }

  private void doModal(String message, final Thread task) {
    // Load dialog
    val progress = new WebProgressDialog(Frame, "");
    progress.setText(message);
    progress.setIndeterminate(true);
    progress.setShowProgressText(false);

    // Starting updater thread
    new Thread(new Runnable() {
      @Override
      public void run() {
        task.start();
        try {
          task.join();
        } catch (InterruptedException e) {

        } finally {
          progress.setVisible(false);
        }
      }
    }).start();

    progress.setModal(true);
    progress.setVisible(true);
  }

  private void openFile(File filename) throws IOException, UnknownTypeException, DexInstructionParsingException {
    // load the file
    val dex = new Dex(filename);

    // create split pane
    val splitPane = new FileTab(dex);

    // create tab
    TabbedPane.addTab(filename.getName(), splitPane);
  }

  // MAIN FUNCTION

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          MainWindow window = new MainWindow();
          window.Frame.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }
}
