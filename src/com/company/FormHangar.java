package com.company;

import java.awt.*;
import java.awt.event.*;
import javax.security.auth.login.Configuration;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import org.apache.log4j.*;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class FormHangar {
    private HangarsCollection hangarsCollection;
    private LinkedList<Vehicle> deleted;
    JFrame w;
    cImage img_box;
    BufferedImage buffered_img;
    Graphics gh;
    JList list_box;

    private Logger logger = Logger.getLogger(FormHangar.class);

    void ReloadLevels() {
        int index = list_box.getSelectedIndex();
        DefaultListModel list = new DefaultListModel();
        for (int i = 0; i < hangarsCollection.getKeys().size(); i++) {
            list.addElement(hangarsCollection.getKeys().get(i));
        }
        list_box.setModel(list);

        if (list_box.getVisibleRowCount() > 0 && (index == -1 || index >= list_box.getVisibleRowCount())) {
            list_box.setSelectedIndex(0);
        }
        else if (list_box.getVisibleRowCount() > 0 && index > -1 && index < list_box.getVisibleRowCount()) {
            list_box.setSelectedIndex(index);
        }
        Draw();
    }

    private void Draw() {
        if (list_box.getSelectedIndex() > -1) {
            gh = buffered_img.createGraphics();
            gh.setColor(Color.WHITE);
            gh.fillRect(0, 0, img_box.getWidth(), img_box.getHeight());
            hangarsCollection.getValue((String) list_box.getSelectedValue()).Draw(gh);
            img_box.b_img = buffered_img;
            img_box.repaint();
        }
        else {
            gh = buffered_img.createGraphics();
            gh.setColor(Color.WHITE);
            gh.fillRect(0, 0, img_box.getWidth(), img_box.getHeight());
            img_box.b_img = buffered_img;
            img_box.repaint();
        }
    }

    WindowAdapter AddPlane = new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            Plane plane = (Plane) ((FormPlaneConfig) e.getSource()).GetPlane();
            if (plane != null && list_box.getSelectedIndex() > -1) {
                int num = 0;
                try {
                    num = hangarsCollection.getValue((String) list_box.getSelectedValue()).Add(plane);
                    JOptionPane.showMessageDialog(w, "Place " + num + " taken");
                    Draw();
                    logger.info("info: added plane " + plane);
                } catch (HangarOverflowException ex) {
                    logger.warn("hangar overflow in plane Adding");
                    JOptionPane.showMessageDialog(w, "Warning: Hangar filled");
                } catch (Exception ex) {
                    //new Mailer().logMail("Unknown fatal error in plane Adding");
                }
            }
        }
    };

    public FormHangar() {
        w = new JFrame("Hangar");
        w.setLayout(null);
        w.setSize(1101, 648);
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        PatternLayout patternLayout = new PatternLayout();
        patternLayout.setConversionPattern("%5p %c{1}:%L - %m (%d{дата dd.MM.yyyy})%n");

        FileAppender fileAppender = new FileAppender();
        fileAppender.setFile("D:\\\\Logs\\\\Java\\\\logs.txt");
        fileAppender.setLayout(patternLayout);
        fileAppender.activateOptions();

        BasicConfigurator.configure(fileAppender);

        JMenuItem saveB = new JMenuItem("Save all to file");

        saveB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (list_box.isSelectionEmpty()) {
                    return;
                }
                else {
                    FileDialog fd = new FileDialog(w, "Choose a file", FileDialog.LOAD);
                    fd.setVisible(true);
                    String filename = fd.getDirectory() + fd.getFile();
                    if (filename.contains(".txt")) {
                        try {
                            hangarsCollection.SaveFile(filename);
                            ReloadLevels();
                            logger.info("all saved");
                        }
                        catch (FileNotFoundException ex) {
                            logger.error("file not fount in SavingAll");
                        }
                        catch (IOException ex) {
                            logger.error("io error in SavingAll");
                        }
                        catch (Exception ex) {
                            //new Mailer().logMail("Unknown fatal error in SavingAll");
                        }
                    }
                }
            }
        });

        JMenuItem saveSHB = new JMenuItem("Save single hangar to file");

        saveSHB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (list_box.isSelectionEmpty()) {
                    return;
                }
                else {
                    FileDialog fd = new FileDialog(w, "Choose a file", FileDialog.LOAD);
                    fd.setVisible(true);
                    String filename = fd.getDirectory() + fd.getFile();
                    if (filename.contains(".txt")) {
                        try {
                            hangarsCollection.SaveSingleHangarFile(filename, list_box.getSelectedValue().toString());
                            ReloadLevels();
                            logger.info("single saved");
                        }
                        catch (FileNotFoundException ex) {
                            logger.error("file not found exception in SavingSingle");
                        }
                        catch (IOException ex) {
                            logger.error("io error in SavingSingle");
                        }
                        catch (Exception ex) {
                            //new Mailer().logMail("Unknown fatal error in SavingSingle");
                        }
                    }
                }
            }
        });

        JMenuItem loadB = new JMenuItem("Load from file");

        loadB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileDialog fd = new FileDialog(w, "Choose a file", FileDialog.LOAD);
                fd.setVisible(true);
                String filename = fd.getDirectory() + fd.getFile();
                if (filename.contains(".txt")) {
                    try {
                        hangarsCollection.LoadData(filename);
                        logger.info("data loaded");
                    }
                    catch (HangarOverflowException ex) {
                        logger.warn("hangar overflow exception in Loading");
                    }
                    catch (FileNotFoundException ex) {
                        logger.error("file not found error in Loading");
                    }
                    catch (IOException ex) {
                        logger.error("io error in Loading");
                    }
                    catch (Exception ex) {
                        //new Mailer().logMail("Unknown fatal error in Loading");
                    }
                }
                ReloadLevels();
            }
        });

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(saveB);
        fileMenu.add(saveSHB);
        fileMenu.add(loadB);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);

        w.setJMenuBar(menuBar);

        img_box = new cImage();
        img_box.setSize(900, 601);
        img_box.setLocation(0, 0);
        img_box.setVisible(true);

        buffered_img = new BufferedImage(img_box.getWidth(), img_box.getHeight(), BufferedImage.TYPE_INT_RGB);

        JLabel labelHangar = new JLabel("Hangar:");
        labelHangar.setBounds(912, 3, 53, 17);
        labelHangar.setVisible(true);

        TextField textFieldHangar = new TextField();
        textFieldHangar.setLocation(971, 1);
        textFieldHangar.setSize(94,22);
        textFieldHangar.setVisible(true);

        JButton buttonAddHangar = new JButton("Add hangar");
        buttonAddHangar.setLocation(906, 25);
        buttonAddHangar.setSize(165, 45);
        buttonAddHangar.setVisible(true);

        buttonAddHangar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (textFieldHangar.getText().equals("")) {
                    JOptionPane.showMessageDialog(w, "The name is not printed");
                    return;
                }
                hangarsCollection.AddHangar(textFieldHangar.getText());
                ReloadLevels();
                logger.info("added hangar " + textFieldHangar.getText());
            }
        });

        list_box = new JList();
        list_box.setLocation(906, 75);
        list_box.setSize(165,100);
        list_box.setVisible(true);

        list_box.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                Draw();
                if (!list_box.isSelectionEmpty()) logger.info("selected hangar " + list_box.getSelectedValue().toString());
            }
        });

        JButton buttonDeleteHangar = new JButton("Delete hangar");
        buttonDeleteHangar.setLocation(906, 180);
        buttonDeleteHangar.setSize(165, 45);
        buttonDeleteHangar.setVisible(true);

        buttonDeleteHangar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (list_box.getSelectedIndex() > -1) {
                    if (JOptionPane.showConfirmDialog(w,
                            "Are you want to delete hangar " + list_box.getSelectedValue().toString() + "?",
                            "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
                        logger.info("deleted hangar" + (String)list_box.getSelectedValue());
                        hangarsCollection.DelHangar((String)list_box.getSelectedValue());
                        ReloadLevels();
                    }
                }
            }
        });

        Button buttonSetPlane = new Button("Add plane");
        buttonSetPlane.setLocation(906, 280);
        buttonSetPlane.setSize(165, 45);
        buttonSetPlane.setVisible(true);

        buttonSetPlane.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (list_box.getSelectedIndex() > -1) {
                    FormPlaneConfig planeConfForm = new FormPlaneConfig();

                    planeConfForm.AddEvent(AddPlane);
                }
            }
        });

        JLabel labelUnset = new JLabel("Plane removing");
        labelUnset.setBounds(912, 330, 100, 17);
        labelUnset.setVisible(true);

        JLabel labelPlace = new JLabel("Place:");
        labelPlace.setBounds(912, 350, 53, 17);
        labelPlace.setVisible(true);

        TextField textFieldPlace = new TextField();
        textFieldPlace.setLocation(971, 350);
        textFieldPlace.setSize(94,22);
        textFieldPlace.setVisible(true);

        Button buttonUnset = new Button("Remove");
        buttonUnset.setLocation(915, 375);
        buttonUnset.setSize(150, 33);
        buttonUnset.setVisible(true);

        buttonUnset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (list_box.getSelectedIndex() > -1 && textFieldPlace.getText() != "") {
                    int place;
                    try {
                        place = Integer.parseInt(textFieldPlace.getText());
                    }
                    catch (NumberFormatException ex) {
                        logger.error("not int in text field for deleting");
                        return;
                    }

                    try {
                        logger.info("deleted plane from place " + place + " from hangar" + list_box.getSelectedValue().toString());
                        Vehicle plane = hangarsCollection.getValue(list_box.getSelectedValue().toString()).Remove(place);
                        if (plane != null) deleted.add(plane);
                        Draw();
                    }
                    catch (HangarNotFoundException ex) {
                        logger.warn("hangar not found exception in deleting");
                        JOptionPane.showMessageDialog(w, "Place " + place + " is empty");
                    }
                    catch (Exception ex) {
                        //new Mailer().logMail("Unknown fatal error in Deleting plane");
                    }
                }
            }
        });

        JLabel labelDeleted = new JLabel("Place in deleted list:");
        labelDeleted.setBounds(912, 410, 120, 17);
        labelDeleted.setVisible(true);

        TextField textFieldDeleted = new TextField();
        textFieldDeleted.setLocation(971, 430);
        textFieldDeleted.setSize(94,22);
        textFieldDeleted.setVisible(true);

        Button buttonMove = new Button("Move to plane form");
        buttonMove.setLocation(915, 455);
        buttonMove.setSize(150, 33);
        buttonMove.setVisible(true);

        buttonMove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (textFieldPlace.getText() != "") {
                    int ind;
                    try {
                        ind = Integer.parseInt(textFieldDeleted.getText());
                    }
                    catch (NumberFormatException ex) {
                        logger.error("not int in text field for moving");
                        return;
                    }

                    if (ind < 0 || ind > deleted.size()) return;

                    if (deleted.get(ind) != null) {
                        logger.info("moving plane from deleted array place " + ind);
                        FormPlane form = new FormPlane();
                        form.SetPlane(deleted.get(ind));
                        deleted.remove(ind);
                    }
                    Draw();
                }
            }
        });

        w.add(labelHangar);
        w.add(textFieldHangar);
        w.add(buttonAddHangar);
        w.add(buttonDeleteHangar);
        w.add(list_box);
        w.add(buttonSetPlane);
        w.add(labelUnset);
        w.add(labelPlace);
        w.add(textFieldPlace);
        w.add(buttonUnset);
        w.add(img_box);
        w.add(labelDeleted);
        w.add(textFieldDeleted);
        w.add(buttonMove);

        w.setVisible(true);

        hangarsCollection = new HangarsCollection(img_box.getWidth(), img_box.getHeight());
        deleted = new LinkedList<Vehicle>();

        Draw();
    }
}
