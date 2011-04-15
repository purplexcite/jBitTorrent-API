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

/**
 *
 * Represent a Handshake message according to Bittorrent Protocol. It has the form:
 * <pstrlen><pstr><reserved><info_hash><peer_id>
 *
 * - # pstrlen: string length of <pstr>, as a single raw byte -
 * - # pstr: string identifier of the protocol -
 * - # reserved: eight (8) reserved bytes -
 * - # info_hash: 20-byte SHA1 hash of the info key in the metainfo file -
 * - # peer_id: 20-byte string used as a unique ID for the client -
 *
 * @author Baptiste Dubuis
 * @version 0.1
 */
public class Message_HS extends Message {

    private byte[] length = new byte[1];
    private byte[] protocol = new byte[19];
    private byte[] reserved = new byte[8];
    private byte[] fileID = new byte[20];
    private byte[] peerID = new byte[20];

    /**
     * Creates an empty HS message
     */
    public Message_HS(){
        super(-1, 0);
    }

    /**
     * Creates a HS message with the given infoHash and peerID and default values
     * @param infoHash byte[]
     * @param peerID byte[]
     */
    public Message_HS(byte[] infoHash, byte[] peerID){
        this(new byte[]{19}, "BitTorrent protocol".getBytes(),
                new byte[]{0,0,0,0,0,0,0,0}, infoHash, peerID);
    }

    /**
     * Create a HS message with all given parameters
     * @param length byte[]
     * @param protocol byte[]
     * @param reserved byte[]
     * @param fileID byte[]
     * @param peerID byte[]
     */
    public Message_HS(byte[] length, byte[] protocol, byte[] reserved, byte[] fileID, byte[] peerID){
        super(-1, 0);
        this.length = length;
        this.protocol = protocol;
        this.reserved = reserved;
        this.fileID = fileID;
        this.peerID = peerID;
    }
    /**
     * Return the length of the protocol string as a byte
     * @return byte
     */
    public byte getLength(){
        return length[0];
    }
    /**
     * Return the protocol string as a byte array
     * @return byte[]
     */
    public byte[] getProtocol(){
        return this.protocol;
    }
    /**
     * Return the reserved bytes as a byte array
     * @return byte[]
     */
    public byte[] getReserved(){
        return this.reserved;
    }
    /**
     * Return the infoHash as a byte array
     * @return byte[]
     */
    public byte[] getFileID(){
        return this.fileID;
    }
    /**
     * Return the peerID as a byte array
     * @return byte[]
     */
    public byte[] getPeerID(){
        return this.peerID;
    }

    /**
     * Set the values of the fields according to the parameters
     * @param length byte[]
     * @param protocol byte[]
     * @param reserved byte[]
     * @param fileID byte[]
     * @param peerID byte[]
     */
    public void setData(byte[] length, byte[] protocol, byte[] reserved,
                        byte[] fileID, byte[] peerID){
        this.length = length;
        this.protocol = protocol;
        this.reserved = reserved;
        this.fileID = fileID;
        this.peerID = peerID;
    }
    /**
     * Generate the byte array representing the whole message that can then be transmitted
     * @return byte[]
     */
    public byte[] generate(){
        return Utils.concat(this.length, Utils.concat(this.protocol, Utils.concat(this.reserved, Utils.concat(this.fileID, this.peerID))));
    }
    /**
     * Display the message in a readable format
     * @return String
     */
    public String toString(){
        String toString = "";
        toString += this.length[0]+"+";
        toString += new String(this.protocol)+"+";
        toString += Utils.byteArrayToByteString(this.reserved)+"+";
        toString += Utils.byteArrayToByteString(this.fileID)+"+";
        toString += Utils.byteArrayToByteString(this.peerID);

        return toString;
    }
}
