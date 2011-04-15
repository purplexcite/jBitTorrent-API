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

import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Simple example to show how it is possible to create a new .torrent file to
 * share files using bittorrent protocol
 */
class ExampleCreateTorrent{
    public static void main(String[] args){
        if(args.length < 5){
            System.err.println("Wrong parameter number\r\n\r\nUse:\r\n" +
                               "ExampleCreateTorrent <torrentPath> <announce url> <pieceLength> " +
                               "<filePath1> <filePath2> ... <..> <creator> <..> <comment>");
            System.exit(0);
        }
        TorrentProcessor tp = new TorrentProcessor();
        tp.setAnnounceURL(args[1]);
        try{
            tp.setPieceLength(Integer.parseInt(args[2]));
        }catch(Exception e){
            System.err.println("Piece length must be an integer");
            System.exit(0);
        }
        int i = 3;
        ArrayList<String> files = new ArrayList<String>();
        if(!args[i+1].equalsIgnoreCase("..")){
            tp.setName(args[3]);
            i++;
        }
        while(i < args.length){
            if(args[i].equalsIgnoreCase(".."))
                break;
            files.add(args[i]);
            i++;
        }
        try{
            tp.addFiles(files);
        }catch(Exception e){
            System.err.println(
                    "Problem when adding files to torrent. Check your data");
            System.exit(0);
        }
        i++;
        String creator = "";
        while(i < args.length){
            if(args[i].equalsIgnoreCase(".."))
                break;
            creator += args[i];
            i++;
        }
        tp.setCreator(creator);
        i++;
        String comment = "";
        while(i < args.length){
            if(args[i].equalsIgnoreCase(".."))
                break;
            comment += args[i];
            i++;
        }
        tp.setComment(comment);
        try{
            System.out.println("Hashing the files...");
            System.out.flush();
            tp.generatePieceHashes();
            System.out.println("Hash complete... Saving...");
            FileOutputStream fos = new FileOutputStream(args[0]);
            fos.write(tp.generateTorrent());
            System.out.println("Torrent created successfully!!!");
        }catch(Exception e){
            System.err.println("Error when writing to the torrent file...");
            System.exit(1);
        }
    }
}
