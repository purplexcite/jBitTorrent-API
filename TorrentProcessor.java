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
import java.util.*;
import java.nio.ByteBuffer;
import java.net.URL;
import java.net.MalformedURLException;

/**
 *
 * Class enabling to process a torrent file
 * @author Baptiste Dubuis
 * @version 0.1
 */
public class TorrentProcessor {

    private TorrentFile torrent;

    int startLevel;


    public TorrentProcessor(TorrentFile torrent){
        this.torrent = torrent;
    }

    public TorrentProcessor(){
        this.torrent = new TorrentFile();
    }

    /**
     * Given the path of a torrent, parse the file and represent it as a Map
     * @param filename String
     * @return Map
     */
    public Map parseTorrent(String filename){
        return this.parseTorrent(new File(filename));
    }

    /**
     * Given a File (supposed to be a torrent), parse it and represent it as a Map
     * @param file File
     * @return Map
     */
    public Map parseTorrent(File file){
        try{
            return BDecoder.decode(IOManager.readBytesFromFile(file));
        } catch(IOException ioe){}
        return null;
    }

    /**
     * Given a Map, retrieve all useful information and represent it as a TorrentFile object
     * @param m Map
     * @return TorrentFile
     */
    public TorrentFile getTorrentFile(Map m){
        if(m == null)
            return null;
        if(m.containsKey("announce")) // mandatory key
            this.torrent.announceURL = new String((byte[]) m.get("announce"));
        else
            return null;
        if(m.containsKey("comment")) // optional key
            this.torrent.comment = new String((byte[]) m.get("comment"));
        if(m.containsKey("created by")) // optional key
            this.torrent.createdBy = new String((byte[]) m.get("created by"));
        if(m.containsKey("creation date")) // optional key
            this.torrent.creationDate = (Long) m.get("creation date");
        if(m.containsKey("encoding")) // optional key
            this.torrent.encoding = new String((byte[]) m.get("encoding"));

        //Store the info field data
        if(m.containsKey("info")){
            Map info = (Map) m.get("info");
            try{

                this.torrent.info_hash_as_binary = Utils.hash(BEncoder.encode(info));
                this.torrent.info_hash_as_hex = Utils.byteArrayToByteString(
                                                this.torrent.info_hash_as_binary);
                this.torrent.info_hash_as_url = Utils.byteArrayToURLString(
                                                this.torrent.info_hash_as_binary);
            }catch(IOException ioe){return null;}
            if (info.containsKey("name"))
                this.torrent.saveAs = new String((byte[]) info.get("name"));
            if (info.containsKey("piece length"))
                this.torrent.pieceLength = ((Long) info.get("piece length")).intValue();
            else
                return null;

            if (info.containsKey("pieces")) {
                byte[] piecesHash2 = (byte[]) info.get("pieces");
                if (piecesHash2.length % 20 != 0)
                    return null;

                for (int i = 0; i < piecesHash2.length / 20; i++) {
                    byte[] temp = Utils.subArray(piecesHash2, i * 20, 20);
                    this.torrent.piece_hash_values_as_binary.add(temp);
                    this.torrent.piece_hash_values_as_hex.add(Utils.
                            byteArrayToByteString(
                                    temp));
                    this.torrent.piece_hash_values_as_url.add(Utils.
                            byteArrayToURLString(
                                    temp));
                }
            } else
                return null;

            if (info.containsKey("files")) {
                List multFiles = (List) info.get("files");
                this.torrent.total_length = 0;
                for (int i = 0; i < multFiles.size(); i++) {
                    this.torrent.length.add(((Long) ((Map) multFiles.get(i)).
                                             get("length")).intValue());
                    this.torrent.total_length += ((Long) ((Map) multFiles.get(i)).
                                                  get("length")).intValue();

                    List path = (List) ((Map) multFiles.get(i)).get(
                            "path");
                    String filePath = "";
                    for (int j = 0; j < path.size(); j++) {
                        filePath += new String((byte[]) path.get(j));
                    }
                    this.torrent.name.add(filePath);
                }
            } else {
                this.torrent.length.add(((Long) info.get("length")).intValue());
                this.torrent.total_length = ((Long) info.get("length")).intValue();
                this.torrent.name.add(new String((byte[]) info.get("name")));
            }
        }else
            return null;
        return this.torrent;
    }

    /**
     * Sets the TorrentFile object of the Publisher equals to the given one
     * @param torr TorrentFile
     */
    public void setTorrent(TorrentFile torr) {
        this.torrent = torr;
    }

    /**
     * Updates the TorrentFile object according to the given parameters
     * @param url The announce url
     * @param pLength The length of the pieces of the torrent
     * @param comment The comments for the torrent
     * @param encoding The encoding of the torrent
     * @param filename The path of the file to be added to the torrent
     */
    public void setTorrentData(String url, int pLength, String comment,
                               String encoding, String filename) {
        this.torrent.announceURL = url;
        this.torrent.pieceLength = pLength * 1024;
        this.torrent.createdBy = Constants.CLIENT;
        this.torrent.comment = comment;
        this.torrent.creationDate = System.currentTimeMillis();
        this.torrent.encoding = encoding;
        this.addFile(filename);
    }

    /**
     * Updates the TorrentFile object according to the given parameters
     * @param url The announce url
     * @param pLength The length of the pieces of the torrent
     * @param comment The comments for the torrent
     * @param encoding The encoding of the torrent
     * @param name The name of the directory to save the files in
     * @param filenames The path of the file to be added to the torrent
     * @throws java.lang.Exception
     */
    public void setTorrentData(String url, int pLength, String comment,
                               String encoding, String name, List filenames) throws Exception {
        this.torrent.announceURL = url;
        this.torrent.pieceLength = pLength * 1024;
        this.torrent.comment = comment;
        this.torrent.createdBy = Constants.CLIENT;
        this.torrent.creationDate = System.currentTimeMillis();
        this.torrent.encoding = encoding;
        this.torrent.saveAs = name;
        this.addFiles(filenames);
    }

    /**
     * Sets the announce url of the torrent
     * @param url String
     */
    public void setAnnounceURL(String url) {
        this.torrent.announceURL = url;
    }

    /**
     * Sets the pieceLength
     * @param length int
     */
    public void setPieceLength(int length) {
        this.torrent.pieceLength = length * 1024;
    }

    /**
     * Sets the directory the files have to be saved in (in case of multiple files torrent)
     * @param name String
     */
    public void setName(String name) {
        this.torrent.saveAs = name;
    }

    /**
     * Sets the comment about this torrent
     * @param comment String
     */
    public void setComment(String comment) {
        this.torrent.comment = comment;
    }

    /**
     * Sets the creator of the torrent. This should be the client name and version
     * @param creator String
     */
    public void setCreator(String creator) {
        this.torrent.createdBy = creator;
    }

    /**
     * Sets the time the torrent was created
     * @param date long
     */
    public void setCreationDate(long date) {
        this.torrent.creationDate = date;
    }

    /**
     * Sets the encoding of the torrent
     * @param encoding String
     */
    public void setEncoding(String encoding) {
        this.torrent.encoding = encoding;
    }

    /**
     * Add the files in the list to the torrent
     * @param l A list containing the File or String object representing the files to be added
     * @return int The number of files that have been added
     * @throws Exception
     */
    public int addFiles(List l) throws Exception {
        return this.addFiles(l.toArray());
    }

    /**
     * Add the files in the list to the torrent
     * @param file The file to be added
     * @return int The number of file that have been added
     * @throws Exception
     */
    public int addFile(File file) {
        return this.addFiles(new File[] {file});
    }

    /**
     * Add the files in the list to the torrent
     * @param filename The path of the file to be added
     * @return int The number of file that have been added
     * @throws Exception
     */
    public int addFile(String filename) {
        return this.addFiles(new String[] {filename});
    }
    /**
     * Add the files in the list to the torrent
     * @param filenames An array containing the files to be added
     * @return int The number of files that have been added
     * @throws Exception
     */
    public int addFiles(Object[] filenames) {
        int nbFileAdded = 0;

        if (this.torrent.total_length == -1)
            this.torrent.total_length = 0;
        
        File f = null;
        for (int i = 0; i < filenames.length; i++) {
            if (filenames[i] instanceof String)
                f = new File((String) filenames[i]);
            else if (filenames[i] instanceof File)
                f = (File) filenames[i];
            if (f != null)
                if (f.exists()) {
                    if(f.isDirectory())
                    {
                        DirUtils du = new DirUtils();

                        List l = du.recurseDir(f.getAbsolutePath());

                        for(int j = 1; j < l.size(); j++)
                        {
                            String pth = null;

                            pth = (String) l.get(j);
                            
                            File recursiveListFile = new File(pth);

                            this.torrent.total_length += recursiveListFile.length();
                            this.torrent.name.add(pth);
                            this.torrent.length.add(new Long(recursiveListFile.length()).intValue());
                            nbFileAdded++;
                        }
                    }
                    else if(f.isFile())
                    {
                        this.torrent.total_length += f.length();
                        this.torrent.name.add(f.getPath());
                        this.torrent.length.add(new Long(f.length()).intValue());
                        nbFileAdded++;
                    }
                }
        }

        startLevel = f.getParent().split("/").length;
        
        return nbFileAdded;
    }

    /**
     * Generate the SHA-1 hashes for the file in the torrent in parameter
     * @param torr TorrentFile
     */
    public void generatePieceHashes(TorrentFile torr) {
        ByteBuffer bb = ByteBuffer.allocate(torr.pieceLength);
        int index = 0;
        long total = 0;
        torr.piece_hash_values_as_binary.clear();
        for (int i = 0; i < torr.name.size(); i++) {
            total += (Integer) torr.length.get(i);
            File f = new File((String) torr.name.get(i));
            if (f.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(f);
                    int read = 0;
                    byte[] data = new byte[torr.pieceLength];
                    while ((read = fis.read(data, 0, bb.remaining())) != -1) {
                        bb.put(data, 0, read);
                        if (bb.remaining() == 0) {
                            torr.piece_hash_values_as_binary.add(Utils.hash(bb.
                                    array()));
                            bb.clear();
                        }
                    }
                } catch (FileNotFoundException fnfe) {} catch (IOException ioe) {}
            }
        }
        if (bb.remaining() != bb.capacity())
            torr.piece_hash_values_as_binary.add(Utils.hash(Utils.subArray(
                    bb.array(), 0, bb.capacity() - bb.remaining())));
    }
    /**
     * Generate the SHA-1 hashes for the files in the current object TorrentFile
     */
    public void generatePieceHashes() {
        this.generatePieceHashes(this.torrent);
    }

    /**
     * Generate the bytes of the bencoded TorrentFile data
     * @param torr TorrentFile
     * @return byte[]
     */
    public byte[] generateTorrent(TorrentFile torr) {
        SortedMap map = new TreeMap();
        map.put("announce", torr.announceURL);
        if(torr.comment.length() > 0)
            map.put("comment", torr.comment);
        if(torr.creationDate >= 0)
            map.put("creation date", torr.creationDate);
        if(torr.createdBy.length() > 0)
            map.put("created by", torr.createdBy);
        SortedMap info = new TreeMap();
        
        if (torr.name.size() == 1) {
            info.put("length", (Integer) torr.length.get(0));
            info.put("name", new File((String) torr.name.get(0)).getName());
        } else {
            if (!torr.saveAs.matches(""))
                info.put("name", torr.saveAs);
            else
                info.put("name", "noDirSpec");
            
            ArrayList files = new ArrayList();

            for (int i = 0; i < torr.name.size(); i++) {
                SortedMap file = new TreeMap();
                String[] path = ((String) torr.name.get(i)).split("/");
                File f = new File((String)(torr.name.get(i)));

                ArrayList pathLst = new ArrayList();

                String tmp = "", pth = null;
                pth = (String) torr.name.get(i);
                long fileLength = new File(pth).length();

                torr.length.set(i, (int) fileLength);
                file.put("length", torr.length.get(i));

                String[] pthSplit = pth.split("/");

                for(int c = startLevel; c < pthSplit.length; c++)
                {
                    tmp += pthSplit[c];

                    pathLst.add(pthSplit[c]);
                }

                file.put("path", new ArrayList(pathLst));
                files.add(new TreeMap(file));
                }
            
            info.put("files", files);
        }
        info.put("piece length", torr.pieceLength);
        byte[] pieces = new byte[0];
        for (int i = 0; i < torr.piece_hash_values_as_binary.size(); i++)
            pieces = Utils.concat(pieces,
                                  (byte[]) torr.piece_hash_values_as_binary.
                                  get(i));
        info.put("pieces", pieces);
        map.put("info", info);
        
        try {
            byte[] data = BEncoder.encode(map);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public class DirUtils
    {
        public List recurseDir(String dir)
        {
            String result, _result[];

            result = recurseInDirFrom(dir);
            _result = result.split("\\|");
            return Arrays.asList(_result);
        }

        private String recurseInDirFrom(String dirItem)
        {
            File file;
            String list[], result;

            result = dirItem;

            file = new File(dirItem);
            if(file.isDirectory())
            {
                list = file.list();
                for(int i = 0; i < list.length; i++)
                {
                    result = result + "|"
                            + recurseInDirFrom(dirItem + File.separatorChar + list[i]);
                }
            }
            return result;
        }
    }

    /**
     * Generate the bytes for the current object TorrentFile
     * @return byte[]
     */
    public byte[] generateTorrent() {
        return this.generateTorrent(this.torrent);
    }

    /**
     * Returns the local TorrentFile in its current state
     * @return TorrentFile
     */
    public TorrentFile getTorrent(){
        return this.torrent;
    }

}
