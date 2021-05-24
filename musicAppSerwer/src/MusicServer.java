import javax.swing.*;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class MusicServer
{
    ArrayList<ObjectOutputStream> outClient;
    JFrame jFrame;

    public static void main(String[] args)
    {


       MusicServer musicServer = new MusicServer();
       musicServer.makeGUI();
       musicServer.startServer();
    }


    public class Service implements Runnable
    {
        ObjectInputStream in;
        Socket socket;

        public Service(Socket sok)
        {
            try{
                socket=sok;
                in=new ObjectInputStream(socket.getInputStream());

            }catch (Exception ex){ex.printStackTrace();}
        }

        public void run()
        {
            Object o1 =null;
            Object o2 = null;

            try{
                while ((o1=in.readObject()) != null)
                {
                    o2=in.readObject();
                    System.out.println("Przekazano 2 obiekty");
                    sendToAll(o1,o2);
                }

            }catch (Exception ex){System.out.println("Connection lost");}
        }
    }

    public void makeGUI()
    {
        jFrame= new JFrame();
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setVisible(true);
        jFrame.setSize(150,100);
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel,BoxLayout.Y_AXIS));
        jPanel.add( new JLabel("MUSIC SERVER ON" ));
        jPanel.add( new JLabel("Close to turn it off." ));
        jFrame.getContentPane().add(jPanel);
    }

    public  void startServer()
    {
        outClient = new ArrayList<ObjectOutputStream>();

        try {
            ServerSocket serverSocket = new ServerSocket(4242);

            while (true)
            {
                Socket clientSocket = serverSocket.accept();
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                outClient.add(out);
                Thread thread = new Thread(new Service(clientSocket));
                thread.start();
                System.out.println("new connection");

            }

        }catch (Exception ex){ex.printStackTrace();}
    }

    public  void sendToAll(Object o1, Object o2)
    {
        Iterator iterator = outClient.iterator();
        while (iterator.hasNext())
        {
            try {
                ObjectOutputStream out = (ObjectOutputStream) iterator.next();
                out.writeObject(o1);
                out.writeObject(o2);

            }catch (Exception ex){ex.printStackTrace();}
        }
    }

}
