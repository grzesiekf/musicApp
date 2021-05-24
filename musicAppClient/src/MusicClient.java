import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class MusicClient implements MetaEventListener {

    JFrame mainFrame;
    JPanel panelMain;
    JList list;
    JTextField comm;
    ArrayList<JCheckBox> jCheckBoxArrayList;
    int number;
    Vector<String> vector = new Vector<String>();
    String user;
    ObjectOutputStream out;
    ObjectInputStream in;
    HashMap<String,boolean[]> map = new HashMap<String, boolean[]>();
    JFileChooser chooser;


    Sequencer sequencer;
    Sequence sequence;
    Sequence myseq;
    Track track;


    String[] Instrumentsn = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crush Cymbal", "Hand Clap", "High Tom", "Hi Bongo", " Maracas",
            "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};

    int[] inssumentsi = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {

        MusicClient m = new MusicClient();
        m.config();
        m.makeGUI();

    }

    public void config()
    {
        user="music";
        chooser= new JFileChooser();
        try{
            Socket socket = new Socket("192.168.0.157",4242);
            out= new ObjectOutputStream(socket.getOutputStream());
            in=new ObjectInputStream(socket.getInputStream());
            Thread netThread = new Thread(new NetReader());
            netThread.start();
        }catch (Exception ex){
            System.out.println("Network error");
        }
    }


    public void makeGUI() {
        mainFrame = new JFrame("MusicClient");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel panelT1a = new JPanel(layout);
        panelT1a.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jCheckBoxArrayList = new ArrayList<JCheckBox>();
        Box buttonArea = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MStartListener());
        buttonArea.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MStopListener());
        buttonArea.add(stop);

        JButton tempoG = new JButton("Faster");
        stop.addActionListener(new TempoGListener());
        buttonArea.add(tempoG);

        JButton tempoD = new JButton("Slower");
        stop.addActionListener(new TempoDListener());
        buttonArea.add(tempoD);

        JButton save = new JButton("Save");
        save.addActionListener(new SaveListener());
        buttonArea.add(save);

        JButton load = new JButton("Load");
        load.addActionListener(new LoadListener());
        buttonArea.add(load);

        JButton send = new JButton("Send");
        send.addActionListener(new SendListener());
        buttonArea.add(send);

        list = new JList();
        list.addListSelectionListener(new SelectfromListListener());
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        JScrollPane l=new JScrollPane(list);
        list.setListData(vector);
        buttonArea.add(l);

        buttonArea.add (new JLabel("Add a Description: "));

        comm = new JTextField();
        buttonArea.add(comm);




        Box nameArea = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            nameArea.add(new Label(Instrumentsn[i]));
        }

        panelT1a.add(BorderLayout.EAST, buttonArea);
        panelT1a.add(BorderLayout.WEST, nameArea);

        mainFrame.getContentPane().add(panelT1a);

        GridLayout net = new GridLayout(16, 16);
        net.setVgap(1);
        net.setHgap(2);
        panelMain = new JPanel(net);
        panelT1a.add(BorderLayout.CENTER, panelMain);


        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            jCheckBoxArrayList.add(c);
            panelMain.add(c);
        }

        konfigMidi();

        mainFrame.setBounds(50, 50, 300, 300);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public void konfigMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void createTrackandPlay() {
        int[] tracklist = null;
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            tracklist = new int[16];
            int key = inssumentsi[i];

            for (int j = 0; j < 16; j++) {
                JCheckBox jc = (JCheckBox) jCheckBoxArrayList.get(j + (16 * i));
                if (jc.isSelected()) {
                    tracklist[j] = key;
                } else {
                    tracklist[j] = 0;
                }
            }
            createTrack(tracklist);
            track.add(makeEvent(192, 9, 1, 0, 15));
            try {
                sequencer.setSequence(sequence);
                sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
                sequencer.start();
                sequencer.setTempoInBPM(120);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void meta(MetaMessage meta) {

    }

    public class MStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            createTrackandPlay();
        }
    }


    public class MStopListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sequencer.stop();
        }
    }

    public class TempoGListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float temp = sequencer.getTempoFactor();
            sequencer.setTempoFactor(temp * 1.03f);
        }
    }


    public class TempoDListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float temp = sequencer.getTempoFactor();
            sequencer.setTempoFactor(temp * 0.97f);
        }
    }

    public class SaveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] stan = new boolean[256];

            for (int i = 0; i < 256; i++) {
                JCheckBox pol = (JCheckBox) jCheckBoxArrayList.get(i);
                if (pol.isSelected()) {
                    stan[i] = true;
                }
            }

            try {
                if (chooser.showSaveDialog(chooser) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(stan);
                    oos.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public class LoadListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] stan = null;

            try {
                if (chooser.showOpenDialog(chooser) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    FileInputStream fos = new FileInputStream(file);
                    ObjectInputStream oos = new ObjectInputStream(fos);
                    stan = (boolean[]) oos.readObject();
                    oos.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }


            for (int i = 0; i < 256; i++) {
                JCheckBox pol = (JCheckBox) jCheckBoxArrayList.get(i);
                if (stan[i]) {
                    pol.setSelected(true);
                }
                else pol.setSelected(false);
            }

            sequencer.stop();
            createTrackandPlay();
        }
    }

    public class SendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
           boolean[] stan=new boolean[256];
           for (int i=0;i<256;i++)
           {
               JCheckBox p = (JCheckBox) jCheckBoxArrayList.get(i);
               if(p.isSelected())
               {
                   stan[i]=true;
               }
           }
           try{
               out.writeObject(user+number++ +": "+comm.getText());
               out.writeObject(stan);
           }catch (Exception ex){
               System.out.println("Sanding failed");
           }
           comm.setText("");

        }


    }



    public class SelectfromListListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent le) {
            if(!le.getValueIsAdjusting())
            {
                String selected = (String) list.getSelectedValue();
                if(selected!=null)
                {
                    boolean[] stan=(boolean[]) map.get(selected);
                    changeSequence(stan);
                    sequencer.stop();
                    createTrackandPlay();
                }
            }

        }
    }

    public class NetReader implements Runnable
    {
        boolean[] stan = null;
        String name = null;
        Object obj =null;
        public void run()
        {
            try {
                while ((obj = in.readObject()) != null) {
                    System.out.println("Pobrano obiekt z serwera");
                    String showedName = (String) obj;
                    stan = (boolean[]) in.readObject();
                    map.put(showedName,stan);
                    vector.add(showedName);
                    list.setListData(vector);
                }
            }catch (Exception ex){ System.out.println("Operation failed");}
        }
    }




    public void changeSequence(boolean[] stan)
    {
        for (int i=0;i<256;i++)
        {
            JCheckBox pole = (JCheckBox) jCheckBoxArrayList.get(i);
            if(stan[i]) pole.setSelected(true);
            else pole.setSelected(false);
        }
    }


    public void createTrack(int[] list) {
        for (int i = 0; i < 16; i++) {
            int key = list[i];
            if (key != 0) {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i + 1));
            }

        }
    }


    public static MidiEvent makeEvent(int plc, int can, int one, int two, int tact) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(plc, can, one, two);
            event = new MidiEvent(a, tact);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return event;
    }

}
