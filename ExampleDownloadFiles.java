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
 * Simple example to show how it is possible to download files using bittorrent
 * protocol with a given .torrent file
 */
class ExampleDownloadFiles {

    public ExampleDownloadFiles(String[] args){
        try {
            TorrentProcessor tp = new TorrentProcessor();

            if(args.length < 1){
                System.err.println(
                        "Incorrect use, please provide the path of the torrent file...\r\n" +
                        "\r\nCorrect use of ExampleDownloadFiles:\r\n"+
                        "ExampleDownloadFiles torrentPath");

                System.exit(1);
            }
            TorrentFile t = tp.getTorrentFile(tp.parseTorrent(args[0]));
            if(args.length > 1)
                Constants.SAVEPATH = args[1];
            if (t != null) {
                DownloadManager dm = new DownloadManager(t, Utils.generateID());
                dm.startListening(6881, 6889);
                dm.startTrackerUpdate();
                dm.blockUntilCompletion();
                dm.stopTrackerUpdate();
                dm.closeTempFiles();
            } else {
                System.err.println(
                        "Provided file is not a valid torrent file");
                System.err.flush();
                System.exit(1);
            }
        } catch (Exception e) {

            System.out.println("Error while processing torrent file. Please restart the client");
            //e.printStackTrace();
            System.exit(1);
        }

    }
    public static void main(String[] args) {
        new ExampleDownloadFiles(args);
    }
}
