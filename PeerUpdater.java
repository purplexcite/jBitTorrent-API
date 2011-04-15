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

import java.util.*;
import javax.swing.event.EventListenerList;
import java.net.UnknownHostException;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class providing methods to enable communication between the client and a tracker.
 * Provide method to decode and parse tracker response.
 *
 * @author Baptiste Dubuis
 * @version 0.1
 */
public class PeerUpdater extends Thread {
    private LinkedHashMap<String, Peer> peerList;
    private byte[] id;
    private TorrentFile torrent;


    private long downloaded = 0;
    private long uploaded = 0;
    private long left = 0;
    private String event = "&event=started";
    private int listeningPort = 6881;

    private int interval = 150;
    private int minInterval = 0;
    private boolean first = true;
    private boolean end = false;


    private final EventListenerList listeners = new EventListenerList();

    public PeerUpdater(byte[] id, TorrentFile torrent) {
        peerList = new LinkedHashMap();
        this.id = id;
        this.torrent = torrent;
        this.left = torrent.total_length;
        this.setDaemon(true);
        //this.start();
    }

    public void setListeningPort(int port){
        this.listeningPort = port;
    }

    /**
     * Returns the last interval for updates received from the tracker
     * @return int
     */
    public int getInterval() {
        return this.interval;
    }

    /**
     * Returns the last minimal interval for updates received from the tracker
     * @return int
     */

    public int getMinInterval() {
        return this.minInterval;
    }

    /**
     * Returns the number of bytes that have been downloaded so far
     * @return int
     */

    public long getDownloaded() {
        return this.downloaded;
    }

    /**
     * Returns the number of bytes that have been uploaded so far
     * @return int
     */

    public long getUploaded() {
        return this.uploaded;
    }

    /**
     * Returns the number of bytes still to download to complete task
     * @return int
     */

    public long getLeft() {
        return this.left;
    }

    /**
     * Returns the current event of the client
     * @return int
     */

    public String getEvent() {
        return this.event;
    }

    /**
     * Sets the interval between tracker update
     * @param interval int
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * Sets the mininterval between tracker update
     * @param minInt int
     */
    public void setMinInterval(int minInt) {
        this.minInterval = minInt;
    }

    /**
     * Sets the # of bytes downloaded so far
     * @param dl long
     */

    public void setDownloaded(long dl) {
        this.downloaded = dl;
    }

    /**
     * Sets the # of bytes uploaded so far
     * @param ul long
     */
    public void setUploaded(long ul) {
        this.uploaded = ul;
    }

    /**
     * Sets the # of bytes still to download
     * @param left long
     */
    public void setLeft(long left) {
        this.left = left;
    }

    /**
     * Sets the current state of the client
     * @param event String
     */
    public void setEvent(String event) {
        this.event = event;
    }

    /**
     * Returns the list of peers in its current state
     * @return LinkedHashMap
     */
    public LinkedHashMap getList() {
        return this.peerList;
    }

    /**
     * Update the parameters for future tracker communication
     * @param dl int
     * @param ul int
     * @param event String
     */
    public synchronized void updateParameters(int dl, int ul, String event) {
        synchronized (this) {
            this.downloaded += dl;
            this.uploaded += ul;
            this.left -= dl;
            this.event = event;
        }
    }

    /**
     * Thread method that regularly contact the tracker and process its response
     */
    public void run() {
        int tryNB = 0;
        byte[] b = new byte[0];
        while (!this.end) {
            tryNB++;

            this.peerList = this.processResponse(this.contactTracker(id,
                    torrent, this.downloaded,
                    this.uploaded,
                    this.left, this.event));
            if (peerList != null) {
                if (first) {
                    this.event = "";
                    first = false;
                }
                tryNB = 0;
                this.fireUpdatePeerList(this.peerList);
                try {
                    synchronized (b) {
                        b.wait(interval * 1000);
                    }
                } catch (InterruptedException ie) {}
            } else {
                try {
                    synchronized (b) {
                        b.wait(2000);
                    }
                } catch (InterruptedException ie) {}
            }
        }
    }

    /**
     * Process the map representing the tracker response, which should contain
     * either an error message or the peers list and other information such as
     * the interval before next update, aso
     * @param m The tracker response as a Map
     * @return LinkedHashMap A HashMap containing the peers and their ID as keys
     */
    public synchronized LinkedHashMap<String, Peer> processResponse(Map m) {
        LinkedHashMap<String, Peer> l = null;
        if (m != null) {
            if (m.containsKey("failure reason")) {
                this.fireUpdateFailed(0,
                                      "The tracker returns the following error message:" +
                                      "\t'" +
                                      new String((byte[]) m.get(
                                              "failure reason")) +
                                      "'");
                return null;
            } else {
                if (((Long) m.get("interval")).intValue() < this.interval)
                    this.interval = ((Long) m.get("interval")).intValue();
                else
                    this.interval *= 2;

                Object peers = m.get("peers");
                ArrayList peerList = new ArrayList();
                l = new LinkedHashMap<String, Peer>();
                if (peers instanceof List) {
                    peerList.addAll((List) peers);
                    if (peerList != null && peerList.size() > 0) {
                        for (int i = 0; i < peerList.size(); i++) {
                            String peerID = new String((byte[]) ((Map) (
                                    peerList.
                                    get(i))).
                                    get(
                                            "peer_id"));
                            String ipAddress = new String((byte[]) ((Map) (
                                    peerList.
                                    get(
                                            i))).
                                    get("ip"));
                            int port = ((Long) ((Map) (peerList.get(i))).get(
                                    "port")).intValue();
                            Peer p = new Peer(peerID, ipAddress, port);
                            l.put(p.toString(), p);
                        }
                    }
                } else if (peers instanceof byte[]) {
                    byte[] p = ((byte[]) peers);
                    for (int i = 0; i < p.length; i += 6) {
                        Peer peer = new Peer();
                        peer.setIP(Utils.byteToUnsignedInt(p[i]) + "." +
                                   Utils.byteToUnsignedInt(p[i + 1]) + "." +
                                   Utils.byteToUnsignedInt(p[i + 2]) + "." +
                                   Utils.byteToUnsignedInt(p[i + 3]));
                        peer.setPort(Utils.byteArrayToInt(Utils.subArray(p,
                                i + 4, 2)));
                        l.put(peer.toString(), peer);
                    }
                }
            }
            return l;
        } else
            return null;
    }

    /**
     * Contact the tracker according to the HTTP/HTTPS tracker protocol and using
     * the information in the TorrentFile.
     * @param id byte[]
     * @param t TorrentFile
     * @param dl long
     * @param ul long
     * @param left long
     * @param event String
     * @return A Map containing the decoded tracker response
     */
    public synchronized Map contactTracker(byte[] id,
                                           TorrentFile t, long dl, long ul,
                                           long left, String event) {
        try {
            URL source = new URL(t.announceURL + "?info_hash=" +
                                 t.info_hash_as_url + "&peer_id=" +
                                 Utils.byteArrayToURLString(id) + "&port="+
                                this.listeningPort +
                                 "&downloaded=" + dl + "&uploaded=" + ul +
                                 "&left=" +
                                 left + "&numwant=100&compact=1" + event);
//            System.out.println("Contact Tracker. URL source = " + source);   //DAVID
            URLConnection uc = source.openConnection();
            InputStream is = uc.getInputStream();

            BufferedInputStream bis = new BufferedInputStream(is);

            // Decode the tracker bencoded response
            Map m = BDecoder.decode(bis);
//            System.out.println(m);
            bis.close();
            is.close();

            return m;
        } catch (MalformedURLException murle) {
            this.fireUpdateFailed(2,
                                  "Tracker URL is not valid... Check if your data is correct and try again");
        } catch (UnknownHostException uhe) {
            this.fireUpdateFailed(3, "Tracker not available... Retrying...");
        } catch (IOException ioe) {
            this.fireUpdateFailed(4, "Tracker unreachable... Retrying");
        } catch (Exception e) {
            this.fireUpdateFailed(5, "Internal error");
        }
        return null;
    }

    /**
     * Stops the update process. This methods sends one last message to
     * the tracker saying this client stops sharing the file and it also exits
     * the run method
     */
    public void end() {
        this.event = "&event=stopped";
        this.end = true;
        this.contactTracker(this.id, this.torrent, this.downloaded,
                            this.uploaded, this.left, "&event=stopped");
    }

    /**
     * Adds a PeerUpdateListener to the list of listeners, enabling communication
     * with this object
     * @param listener PeerUpdateListener
     */
    public void addPeerUpdateListener(PeerUpdateListener listener) {
        listeners.add(PeerUpdateListener.class, listener);
    }

    /**
     * Removes a PeerUpdateListener from the list of listeners
     * @param listener PeerUpdateListener
     */

    public void removePeerUpdateListener(PeerUpdateListener listener) {
        listeners.remove(PeerUpdateListener.class, listener);
    }

    /**
     * Returns the list of object that are currently listening to this PeerUpdater
     * @return PeerUpdateListener[]
     */
    public PeerUpdateListener[] getPeerUpdateListeners() {
        return listeners.getListeners(PeerUpdateListener.class);
    }

    /**
     * Sends a message to all listeners with a HashMap containg the list of all
     * peers present in the last tracker response
     * @param l LinkedHashMap
     */
    protected void fireUpdatePeerList(LinkedHashMap l) {
        for (PeerUpdateListener listener : getPeerUpdateListeners()) {
            listener.updatePeerList(l);
        }
    }

    /**
     * Sends a message to all listeners with an error code and a String representing
     * the reason why the last try to contact tracker failed
     * @param error int
     * @param message String
     */

    protected void fireUpdateFailed(int error, String message) {
        for (PeerUpdateListener listener : getPeerUpdateListeners()) {
            listener.updateFailed(error, message);
        }
    }

}
