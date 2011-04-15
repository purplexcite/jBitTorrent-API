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
 * Represent a Peer Protocol message according to Bittorrent protocol specifications.
 * This message format depends on its identity, so refer to Bittorrent specifications for further information
 *
 * @author Baptiste Dubuis
 * @version 0.1
 */
public class Message_PP extends Message {
    private byte[] length = new byte[4];
    private byte[] id = new byte[1];
    private byte[] payload;

    public Message_PP(){
        super();
    }

    public Message_PP(int type, int p) {
        super(type, p);
        this.setData(type);
    }

    public Message_PP(int type){
        this(type, 0);
    }

    public Message_PP(int type, byte[] payload, int p) {
        super(type, p);
        this.setData(type, payload);
    }

    public Message_PP(int type, byte[] payload){
        this(type, payload, 0);
    }


    public byte[] getLength() {
        return this.length;
    }

    public byte[] getID() {
        return this.id;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public void setLength(byte[] length) {
        this.length = length;
    }

    public void setID(int id) {
        this.id[0] = (byte) id;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public void setData(int type) {
        this.type = type;
        switch (type) {
        case 0:
            this.length = new byte[] {0, 0, 0, 0};
            break;
        case 1:
            this.length = new byte[] {0, 0, 0, 1};
            this.id[0] = 0;
            break;
        case 2:
            this.length = new byte[] {0, 0, 0, 1};
            this.id[0] = 1;
            break;
        case 3:
            this.length = new byte[] {0, 0, 0, 1};
            this.id[0] = 2;
            break;
        case 4:
            this.length = new byte[] {0, 0, 0, 1};
            this.id[0] = 3;
            break;
        }
    }

    public void setData(int type, byte[] payload) {
        this.type = type;
        switch (type) {
        case 5:
            this.length = new byte[] {0, 0, 0, 5};
            this.id[0] = 4;
            this.payload = payload;
            break;
        case 6:
            this.length = Utils.intToByteArray(1 + payload.length);
            this.id[0] = 5;
            this.payload = payload;
            break;
        case 7:
            this.length = new byte[] {0, 0, 0, 13};
            this.id[0] = 6;
            this.payload = payload;
            break;
        case 8:
            this.length = Utils.intToByteArray(1 + payload.length);
            this.id[0] = 7;
            this.payload = payload;
            break;
        case 9:
            this.length = new byte[] {0, 0, 0, 13};
            this.id[0] = 8;
            this.payload = payload;
            break;
        case 10:
            this.length = new byte[] {0, 0, 0, 3};
            this.id[0] = 9;
            this.payload = payload;
            break;
        }
    }

    public void setData(byte[] length, byte id, byte[] payload) {
        this.length = length;
        this.id[0] = id;
        this.payload = payload;
    }

    public byte[] generate() {
        if (this.type > 4)
            return Utils.concat(Utils.concat(this.length, this.id),
                                this.payload);
        else if (this.type > 0)
            return Utils.concat(this.length, this.id);
        else
            return this.length;
    }

    public String toString() {
        String toString = "";

        int length = Utils.byteArrayToInt(this.length);
        toString += "<length=" + length + ">";
        if (length > 0) {
            toString += "<id=" + (int)this.id[0] + ">";
            if (length > 1) {
                switch(this.id[0]+1){
                case PeerProtocol.HAVE:
                    toString += "<index=" + Utils.byteArrayToInt(this.payload) +
                            ">";
                    break;
                case PeerProtocol.BITFIELD:
                    toString += "<bitfield="+(new Bits(this.payload))+">";
                    break;
                case PeerProtocol.REQUEST:
                    toString += "<index=" + Utils.byteArrayToInt(Utils.subArray(this.payload,0,4)) +">";
                    toString += "<begin=" + Utils.byteArrayToInt(Utils.subArray(this.payload,4,4)) +">";
                    toString += "<length=" + Utils.byteArrayToInt(Utils.subArray(this.payload,8,4)) +">";
                    break;
                case PeerProtocol.PIECE:
                    toString += "<index=" + Utils.byteArrayToInt(Utils.subArray(this.payload,0,4)) +">";
                    toString += "<begin=" + Utils.byteArrayToInt(Utils.subArray(this.payload,4,4)) +">";
                    toString += "<block= "+(this.payload.length-8)+"bytes>";
                    break;
                case PeerProtocol.CANCEL:
                    toString += "<index=" + Utils.byteArrayToInt(Utils.subArray(this.payload,0,4)) +">";
                    toString += "<begin=" + Utils.byteArrayToInt(Utils.subArray(this.payload,4,4)) +">";
                    toString += "<length=" + Utils.byteArrayToInt(Utils.subArray(this.payload,8,4)) +">";
                    break;
                case PeerProtocol.PORT:
                    break;
                }
                        // + ">";
            }
        }
        return toString;
    }
}
