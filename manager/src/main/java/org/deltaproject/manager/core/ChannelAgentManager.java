package org.deltaproject.manager.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;

public class ChannelAgentManager extends Thread {
    private static final Logger log = LoggerFactory.getLogger(ChannelAgentManager.class);

    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;

    private Process proc;
    private int procPID;

    private Configuration cfg = Configuration.getInstance();

    public ChannelAgentManager() {
        procPID = -1;
    }

    void setSocket(Socket in, DataOutputStream w, DataInputStream r) {
        this.socket = in;
        this.dos = w;
        this.dis = r;
    }

    public Socket getAppSocket() {
        if (socket != null) {
            return this.socket;
        } else
            return null;
    }

    public String read() {
        try {
            return dis.readUTF();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("read false in channelagent");
        }
        return "false";
    }

    public void write(String input) {
        try {
            dos.writeUTF(input);
            dos.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean runAgent() {
        String amAddr = cfg.getAMIP() + " " + cfg.getAMPort();
        String command = "ssh " + cfg.getChannelSSH() + " sudo java -jar delta-agent-channel-1.0-SNAPSHOT-jar-with-dependencies.jar " + amAddr;

        try {
            proc = Runtime.getRuntime().exec(command);
            Field pidField = Class.forName("java.lang.UNIXProcess").getDeclaredField("pid");
            pidField.setAccessible(true);
            Object value = pidField.get(proc);
            this.procPID = (Integer) value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public void stopAgent() {
        try {
            if (dos != null) {
                dos.writeUTF("close");
                dos.flush();

                dos.close();
                dos = null;
            }

            if (dis != null) {
                dis.close();
                dis = null;
            }

            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (procPID != -1)
            try {
                proc = Runtime.getRuntime().exec("sudo kill -9 " + this.procPID);
                proc.waitFor();
                procPID = -1;
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void run() {

        // TODO Auto-generated method stub
    }
}
