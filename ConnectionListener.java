/*
 * Java Bittorrent API as its name indicates is a JAVA API that implements the Bittorrent Protocol
 * This project contains two packages:
 * 1. jBittorrentAPI is the "client" part, i.e. it implements all classes needed to publish
 *    files, share them and download them.
 *    This package also contains example classes on how a developer could create new applications.
 * 2. trackerBT is the "tracker" part, i.e. it implements a all classes needed to run
 *    a Bittorrent tracker that coordinates peers exchanges. *
 *
 * Copyright (C) 2007 Baptiste Dubuis, Artificial Intelligence Laboratory, EPFL
 *
 * This file is part of jbittorrentapi-v1.0.zip
 *
 * Java Bittorrent API is free software and a free user study set-up;
 * you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Java Bittorrent API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Java Bittorrent API; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @version 1.0
 * @author Baptiste Dubuis
 * To contact the author:
 * email: baptiste.dubuis@gmail.com
 *
 * More information about Java Bittorrent API:
 *    http://sourceforge.net/projects/bitext/
 */

package jBittorrentAPI;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.event.EventListenerList;

/**
 * Thread that can listen for remote peers connection tries to this client
 *
 * @author Baptiste Dubuis
 * @version 0.1
 */
public class ConnectionListener extends Thread {
    private ServerSocket ss = null;
    private int minPort = -1;
    private int maxPort = -1;
    private int connectedPort = -1;
    private final EventListenerList listeners = new EventListenerList();
    private boolean acceptConnection = true;

    public ConnectionListener() {}
    public ConnectionListener(int minPort, int maxPort){
        this.minPort = minPort;
        this.maxPort = maxPort;
    }

    /**
     * Returns the port this client is listening on
     * @return int
     */
    public int getConnectedPort(){
        return this.connectedPort;
    }

    /**
     * Returns the minimal port number this client will try to listen on
     * @return int
     */
    public int getMinPort(){
        return this.minPort;
    }

    /**
     * Returns the maximal port number this client will try to listen on
     * @return int
     */
    public int getMaxPort(){
        return this.maxPort;
    }

    /**
     * Sets the minimal port number this client will try to listen on
     * @param minPort int
     */
    public void setMinPort(int minPort){
        this.minPort = minPort;
    }

    /**
     * Sets the minimal port number this client will try to listen on
     * @param maxPort int
     */
    public void setMaxPort(int maxPort){
        this.maxPort = maxPort;
    }

    /**
     * Try to create a server socket for remote peers to connect on within the
     * specified port range
     * @param minPort The minimal port number this client should listen on
     * @param maxPort The maximal port number this client should listen on
     * @return boolean
     */
    public boolean connect(int minPort, int maxPort){
        this.minPort = minPort;
        this.maxPort = maxPort;
        for(int i = minPort; i <= maxPort; i++)
            try {
                this.ss = new ServerSocket(i);
                this.connectedPort = i;
                this.setDaemon(true);
                this.start();
                return true;
            } catch (IOException ioe) {}
        return false;
    }

    /**
     * Try to create a server socket for remote peers to connect on within current
     * port range
     * @return boolean
     */
    public boolean connect(){
        if(this.minPort != -1 && this.maxPort != -1)
            return this.connect(this.minPort, this.maxPort);
        else
            return false;
    }

    public void run() {
        byte[] b = new byte[0];
        try {
            while (true) {
                if(this.acceptConnection){
                    this.fireConnectionAccepted(ss.accept());
                    sleep(1000);
                }else{
                    synchronized(b){
                        System.out.println("No more connection accepted for the moment...");
                        b.wait();
                    }
                }
            }
        } catch (IOException ioe) {
            System.err.println("Error in connection listener: "+ioe.getMessage());
            System.err.flush();
        } catch(InterruptedException ie){

        }
    }

    /**
     * Decides if the client should accept or not future connection
     * @param accept true if it should accept, false otherwise
     */
    public synchronized void setAccept(boolean accept){
        this.acceptConnection = accept;
        this.notifyAll();
    }


    public void addConListenerInterface(ConListenerInterface listener) {
        listeners.add(ConListenerInterface.class, listener);
    }

    public void removeConListenerInterface(ConListenerInterface listener) {
        listeners.remove(ConListenerInterface.class, listener);
    }

    public ConListenerInterface[] getConListenerInterfaces() {
        return listeners.getListeners(ConListenerInterface.class);
    }

    /**
     * Method used to send message to all object currently listening on this thread
     * when a new connection has been accepted. It provides the socket the connection
     * is bound to.
     *
     * @param s Socket
     */
    protected void fireConnectionAccepted(Socket s) {
        for (ConListenerInterface listener : getConListenerInterfaces()) {
            listener.connectionAccepted(s);
        }
    }

}
