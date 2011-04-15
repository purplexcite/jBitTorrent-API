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

import java.io.*;
import java.util.ArrayList;

/**
 *
 */
public class BitExt {

    /**
     * @param args No arguments should be provided
     */
    public static void main(String[] args) {

        // Client id:
        byte[] myID = Utils.generateID();
        System.out.println("--------------------------------");
        System.out.println("| Extending BitTorrent Project |");
        System.out.println("--------------------------------\r\n");
        System.out.println("Client ID = " + new String(myID) + "\r\n\r\n");

        IOManager iom = new IOManager();
        String userInput = "";

        if (iom.readUserInput(
                "What you want to do?\r\n\t1) Publish file\r\n\t2) Retrieve file\r\nYour choice : ").
            matches("1")) {

            System.out.println("Publishing new files...\r\n");

            //Publisher p = new Publisher();
            TorrentProcessor tp = new TorrentProcessor();
            ArrayList<String> files = new ArrayList<String>();
            System.out.println(
                    "Enter the path of files you want to publish, no entry means you're done...");
            do {
                userInput = iom.readUserInput("File to publish: ");
                if (userInput.matches(""))
                    break;
                else
                    files.add(userInput);
            } while (true);
            try {
                if (files.size() > 1)
                    tp.setTorrentData(iom.readUserInput(
                            "Enter tracker announce url: "),
                                      Integer.parseInt(iom.readUserInput(
                                              "Enter piece length: ")),
                                      iom.readUserInput(
                                              "Enter comment for your torrent: "),
                                      "UTF8",
                                      iom.readUserInput(
                                              "Enter the name of the directory your files will be saved in: "),
                                      files);
                else if (files.size() == 1)
                    tp.setTorrentData(iom.readUserInput(
                            "Enter tracker announce url: "),
                                      Integer.parseInt(iom.readUserInput(
                                              "Enter piece length: ")),
                                      iom.readUserInput(
                                              "Enter comment for your torrent: "),
                                      "UTF8",
                                      (String) files.get(0));

                tp.generatePieceHashes();
                IOManager.save(tp.generateTorrent(),
                               (userInput=iom.readUserInput("Save torrent as: ")));
                // ConnectionManager.publish(userInput, "localhost", "", "", "test.torrent", "noInfo", "MyComment", "7");
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("Retrieving files...\r\n");
            boolean defParam = true;
            String host = "torrents.thepiratebay.org";
            int port = 80;
            String filename =
                    "/hashtorrent/3572543.torrent/333.3572543.TPB.torrent";
            String rename = "torrent/naruto333.torrent";

            boolean detailedTorrentInfo = false;

            if ((userInput = iom.readUserInput(
                    "Do you want to download a torrent or " +
                    "use an existing one?\r\n1. Download\r\n2. Use existing\r\nYour choice: ")).
                             matches("1")) {

                if (!(userInput = iom.readUserInput(
                        "\r\nWhich file would you like to download?\r\n\t1. Naruto 333" +
                        "\r\n\t2. Naruto 334\r\n\t3. FF7\r\n\t4. Picture 226\r\n\t" +
                        "5. Define parameters\r\nYour choice : ")).matches("") &&
                                  !userInput.matches("1")) {
                    if (userInput.matches("2")) {
                        filename =
                                "/hashtorrent/3575116.torrent/Naruto_chapter334.3575116.TPB.torrent";
                        rename = "torrent/naruto334.torrent";
                    } else if (userInput.matches("3")) {
                        host = "dl.torrentreactor.net";
                        filename = "/download.php?id=627079&name=Final%20Fantasy%20VII%20Advent%20Children%20(DUBBED)(ws)(DVDRIP)[TV-M]";
                        rename = "torrent/FF7_Dubbed.torrent";
                    } else if (userInput.matches("4")) {
                        filename =
                                "/hashtorrent/3574174.torrent/Picture_226.jpg.3574174.TPB.torrent";
                        rename = "torrent/Picture_226.torrent";
                    } else
                        defParam = false;
                }
                if (!defParam) {
                    if (!(userInput = iom.readUserInput(
                            "Please enter host name [default = dl.torrentreactor.net] : ")).
                                      matches(""))
                        host = userInput;
                    if (!(userInput = iom.readUserInput(
                            "Please enter host port [default = 80] :")).matches(
                                    ""))
                        port = new Integer(userInput).intValue();
                    if (!(userInput = iom.readUserInput(
                            "Please enter path of the " +
                            "file to download [default = /download.php?" +
                            "id=627079&name=Final%20Fantasy%20VII%20Advent%20Children%20(DUBBED)(ws)(DVDRIP)[TV-M]] :")).
                                      matches(""))
                        filename = userInput;
                    if (!(userInput = iom.readUserInput(
                            "Save this file as [default = FF7_Dubbed.torrent] :")).
                                      matches(""))
                        rename = userInput;

                    if (!(userInput = iom.readUserInput(
                            "Detailed torrent info? Yes/No [default = No]")).
                                      matches("")) {
                        if (userInput.equalsIgnoreCase("yes") ||
                            userInput.equalsIgnoreCase("y"))
                            detailedTorrentInfo = true;
                    }

                }

                // Download torrent file according to given information
                ConnectionManager.downloadFile(host, port, filename, rename);
            } else {
                rename = iom.readUserInput("Enter path to torrent: ");
            }

            // Process the torrent file to extract features
            try {
                TorrentProcessor tp = new TorrentProcessor();
                TorrentFile t = tp.getTorrentFile(tp.parseTorrent(rename));
                System.out.println("Torrent parsed...");
                if (t != null) {
                    DownloadManager dm = new DownloadManager(t, myID);
                    System.out.println("DM initiated...");
                    dm.startListening(6881, 6889);
                    System.out.println("Listening started...");
                    dm.startTrackerUpdate();
                    System.out.println("Updater started...");
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

                System.out.println("Error while processing torrent file");
                e.printStackTrace();
                System.exit(2);
            }

        }
    }
}






