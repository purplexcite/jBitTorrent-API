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

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Provides methods for interaction with remote host, like downloading or uploading
 * files using HTTP protocol.
 * @todo Optimize publish method to be able to specify (for example in a xml file)
 * the protocol to upload files to a given website
 */
public class ConnectionManager {

    /**
     * Download a file from a web site and save it according to the provided path
     * @param host Name of the host the torrent is located on
     * @param port Port of the host that provides the torrent file
     * @param filename Path of the file on the host
     * @param renameFile Path where the torrent will be saved
     * @return boolean True if the file has been successfully downloaded, false otherwise
     */
    public static boolean downloadFile(String host, int port, String filename,
                                    String renameFile) {
        try {
            URL source = new URL("HTTP", host, port, filename);
            //IOManager iom = new IOManager(renameFile);
            InputStream is = source.openStream();
            IOManager.saveFromURL(is, renameFile);
            is.close();
            return true;
        } catch (MalformedURLException murle) {
            System.err.println("URL not valid...");
            System.exit(1);
        } catch (Exception e) {
            System.err.println(
                    "Unable to download torrent file, host unreachable...");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean publish(String torrentPath){
        return publish(torrentPath, "", "", "", "", "", "", "");
    }

    public static boolean publish(String torrentPath, String trackerName,
                                  String username, String pwd, String torrentRename,
            String info, String comment, String catid) {
        URL url;
        URLConnection urlConn;
        DataOutputStream printout;
        DataInputStream input;
        int tracker = 1;
        if(trackerName.equalsIgnoreCase("")){
            tracker = new Integer(IOManager.readUserInput(
                    "On which tracker do you want your " +
                    "torrent to be published?\r\n1. " +
                    "smartorrent.com\r\n\t2.Localhost\r\nYour choice: ")).
                      intValue();
        }else if(trackerName.contains("smartorrent")){
            tracker = 1;
        }else
            tracker = 2;
        ClientHttpRequest c = null;
        try {
            switch (tracker) {
            case 2:
                c = new ClientHttpRequest(
                        trackerName);

                c.setParameter("name", torrentRename);
                c.setParameter("torrent", new File(torrentPath));
                c.setParameter("info", info);
                c.setParameter("comment", comment);
                c.post();

                break;
            case 1:
            default:
                url = new URL("http://www.smartorrent.com/?page=login");
                urlConn = url.openConnection();

                Map<String, List<String>> headers = urlConn.getHeaderFields();
                List<String> values = headers.get("Set-Cookie");
                String cookieValue = null;
                for (Iterator iter = values.iterator(); iter.hasNext(); ) {
                    String v = (((String) iter.next()).split(";"))[0];

                    if (cookieValue == null)
                        cookieValue = v;
                    else
                        cookieValue = cookieValue + ";" + v;
                }
                url = new URL("http://www.smartorrent.com/?page=login");
                urlConn = url.openConnection();
                urlConn.setDoInput(true);
                urlConn.setDoOutput(true);
                urlConn.setUseCaches(false);
                urlConn.setRequestProperty
                        ("Content-Type", "application/x-www-form-urlencoded");
                urlConn.setRequestProperty("Referer",
                                           "http://www.smartorrent.com/?page=upload");
                urlConn.setRequestProperty("Cookie",
                                           cookieValue);
                printout = new DataOutputStream(urlConn.getOutputStream());

                String data = URLEncoder.encode("loginreturn", "UTF-8") + "=" +
                              URLEncoder.encode("/?page=login", "UTF-8");
                data += "&" + URLEncoder.encode("loginusername", "UTF-8") + "=" +
                        URLEncoder.encode(username, "UTF-8");
                data += "&" + URLEncoder.encode("loginpassword", "UTF-8") + "=" +
                        URLEncoder.encode(pwd, "UTF-8");

                printout.writeBytes(data);
                printout.flush();
                printout.close();

                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        urlConn.
                        getInputStream()));
                while (rd.readLine() != null);

                rd.close();

                c = new ClientHttpRequest(
                        "http://www.smartorrent.com/?page=upload");
                String[] cookie = cookieValue.split("=");
                c.setCookie(cookie[0], cookie[1]);
                c.postCookies();

                c.setParameter("name", torrentRename);
                c.setParameter("torrent", new File(torrentPath));
                c.setParameter("catid", catid);
                c.setParameter("info", info);
                c.setParameter("comment", comment);
                c.setParameter("submit", "Upload");
                c.post();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
