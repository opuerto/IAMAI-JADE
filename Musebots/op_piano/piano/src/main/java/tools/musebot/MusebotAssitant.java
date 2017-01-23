package tools.musebot;

/**
 * Created by OscarAlfonso on 12/27/2016.
 */
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Scanner;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;

public class MusebotAssitant extends Agent implements OSCListener {
    OSCServer server;
    InetSocketAddress controllerAddress;
    String clientID;
    MusebotAssitant OSC_m = this;
    long lastHeartbeat = System.currentTimeMillis();

    protected void setup()
    {
        System.out.println("Starting Agent " + getAID().getName() );
        //Conect to the UDP
        addBehaviour(new connectToMcBehaviour());
        //Send Im Alive to MC
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                send("/agent/alive", new Object[] {clientID});
                doWait(1000);
            }
        });
        //Check if the MC is currently listening
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                if(System.currentTimeMillis() - lastHeartbeat > 30000) {
                    System.out.println("Haven't heard from the MC so the kill message is being sent.");
                    doDelete();

                }
            }
        });

    }

    protected void takeDown()
    {
        System.out.println("Agent " +getAID().getName()+" going out");
    }

    public void messageReceived(OSCMessage msg, SocketAddress source, long timetag)
    {

        if(msg.getName().equals("/mc/agentList")) {
            //heartbeat from agent
            lastHeartbeat = System.currentTimeMillis();

        }else if(msg.getName().equals("/agent/off"))
        {
            //Kill the agent stage
            doDelete();
        }

    }

    public void send(String msgName, Object... args) {
        try {
            server.send(new OSCMessage(msgName, args), controllerAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class connectToMcBehaviour extends OneShotBehaviour
    {
        public void action()
        {
            //read in data from config file
            try {
                Scanner scanner = new Scanner(new File("..\\config.txt"));
                String conductorHostname = "";
                int conductorListenPort = 0;
                int myListenPort = 0;
                while(scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split("[ ]");
                    if(parts[0].equals("mc_hostname")) {
                        conductorHostname = parts[1];
                    } else if(parts[0].equals("mc_listen_port")) {
                        conductorListenPort = Integer.parseInt(parts[1]);
                    } else if(parts[0].equals("my_listen_port")) {
                        myListenPort = Integer.parseInt(parts[1]);
                    } else if(parts[0].equals("id")) {
                        clientID = parts[1].trim();
                    }
                }
                scanner.close();
                if(clientID == null) {
                    throw new IOException("No id parameter in config file.");
                }
                //set up server
                server = OSCServer.newUsing(OSCServer.UDP, myListenPort);
                server.addOSCListener(OSC_m);
                server.start();
                controllerAddress = new InetSocketAddress(conductorHostname, conductorListenPort);
                System.out.println("Musebot config: conductor=" + conductorHostname + "," + conductorListenPort);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Since you don't have the config file you're not allowed to continue.");
                System.err.println("It's for your own good. Goodbye!");
                System.exit(0);
            }
        }



    }




}
