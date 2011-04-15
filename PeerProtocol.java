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

import java.lang.ArrayIndexOutOfBoundsException;
import java.io.InputStream;
import java.io.IOException;

/**
 * Constants used in Peer Protocol.
 *
 * @author Baptiste Dubuis
 * @version 0.1
 */
public class PeerProtocol {
    public static final int HANDSHAKE = -1;
    public static final int KEEP_ALIVE = 0;
    public static final int CHOKE = 1;
    public static final int UNCHOKE = 2;
    public static final int INTERESTED = 3;
    public static final int NOT_INTERESTED = 4;
    public static final int HAVE = 5;
    public static final int BITFIELD = 6;
    public static final int REQUEST = 7;
    public static final int PIECE = 8;
    public static final int CANCEL = 9;
    public static final int PORT = 10;
    public static final String[] TYPE = {"Keep_Alive", "Choke", "Unchoke",
                                        "Interested", "Not_Interested", "Have",
                                        "Bitfield", "Request", "Piece",
                                        "Cancel", "Port"};

    public static final int BLOCK_SIZE = 16384;
    public static final byte[] BLOCK_SIZE_BYTES = Utils.intToByteArray(16384);
/*
    public static Message_HS readHS(InputStream is) {
        try {
            byte[] length = new byte[1];
            is.read(length);
            byte[] protocol = new byte[19];
            is.read(protocol);
            byte[] reserved = new byte[8];
            is.read(reserved);
            byte[] fileID = new byte[20];
            is.read(fileID);
            byte[] peerID = new byte[20];
            is.read(peerID);

            return new Message_HS(fileID, peerID);

        } catch (IOException ioe) {

        }
        return null;
    }

    public static Message_PP readMessage(InputStream is) {
        byte[] length = new byte[4];
        int id;
        byte[] payload = new byte[0];
        try {
            is.read(length);
            int l = Utils.byteArrayToInt(length);
            if (l == 0)
                return new Message_PP(KEEP_ALIVE);

            long timeout = System.currentTimeMillis();
            id = is.read();
            if (l == 1)
                return new Message_PP(id + 1);
            l = l - 1;
            for (int i = 0; i < l; ) {
                int available = is.available();
                if (available < l - i) {
                    byte[] temp = new byte[available];
                    is.read(temp);
                    payload = Utils.concat(payload, temp);
                    i += available;
                } else {
                    byte[] temp = new byte[l - i];
                    is.read(temp);
                    payload = Utils.concat(payload, temp);
                    break;
                }
            }
            return new Message_PP(id + 1, payload);
        } catch (IOException ioe) {
            System.err.println("Problem when reading message from stream...");
            ioe.printStackTrace();
        }
        return null;
    }
*/
}
