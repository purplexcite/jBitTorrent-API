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
import java.util.BitSet;

public class Bits {
    private boolean[] bits;

    public Bits(int length){
        this.bits = new boolean[length];
    }
    public Bits(byte[] b) {
        this.bits = Utils.byteArray2BitArray(b);
    }
    public Bits(){}

    public Bits and(Bits b){
        if(this.length() != b.length()){
            System.err.println("Error during and operation: bits length doesn't match");
            return null;
        }
        Bits temp = new Bits(this.length());
        for(int i = 0; i < this.length(); i++)
            temp.set(i, this.get(i) && b.get(i));
        return temp;
    }

    public Bits or(Bits b){
        if(this.length() != b.length()){
            System.err.println("Error during and operation: bits length doesn't match");
            return null;
        }
        Bits temp = new Bits(this.length());
        for(int i = 0; i < this.length(); i++)
            temp.set(i, this.get(i) || b.get(i));
        return temp;
    }




    public void setBits(boolean[] b){
        this.bits = b;
    }

    public int length(){
        return this.bits.length;
    }

    public boolean[] getBits(){
        return this.bits;
    }

    public boolean get(int i){
        return this.bits[i];
    }
    public void set(int i){
        this.bits[i] = true;
    }
    public void set(int i, boolean val){
        this.bits[i] = val;
    }

    public String toString(){
        String toString = "";
        for(int i = 0; i < this.bits.length; i++)
            toString += this.bits[i] ? 1:0;
        return toString;
    }
}
