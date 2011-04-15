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

import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.InputStream;
import java.util.Random;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.util.Iterator;

/**
 * <p>Title: Client HTTP Request class</p>
 * <p>Description: this class helps to send POST HTTP requests with various form data,
 * including files. Cookies can be added to be included in the request.</p>
 *
 * @author Vlad Patryshev
 * @version 1.0
 */
public class ClientHttpRequest {
    URLConnection connection;
    OutputStream os = null;
    Map cookies = new HashMap();

    protected void connect() throws IOException {
        if (os == null)
            os = connection.getOutputStream();
    }

    protected void write(char c) throws IOException {
        connect();
        os.write(c);
    }

    protected void write(String s) throws IOException {
        connect();
        os.write(s.getBytes());
    }

    protected void newline() throws IOException {
        connect();
        write("\r\n");
    }

    protected void writeln(String s) throws IOException {
        connect();
        write(s);
        newline();
    }

    private static Random random = new Random();

    protected static String randomString() {
        return Long.toString(random.nextLong(), 36);
    }

    String boundary = "---------------------------" + randomString() +
                      randomString() + randomString();

    private void boundary() throws IOException {
        write("--");
        write(boundary);
    }

    /**
     * Creates a new multipart POST HTTP request on a freshly opened URLConnection
     *
     * @param connection an already open URL connection
     * @throws IOException
     */
    public ClientHttpRequest(URLConnection connection) throws IOException {
        this.connection = connection;
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type",
                                      "multipart/form-data; boundary=" +
                                      boundary);
    }

    /**
     * Creates a new multipart POST HTTP request for a specified URL
     *
     * @param url the URL to send request to
     * @throws IOException
     */
    public ClientHttpRequest(URL url) throws IOException {
        this(url.openConnection());
    }

    /**
     * Creates a new multipart POST HTTP request for a specified URL string
     *
     * @param urlString the string representation of the URL to send request to
     * @throws IOException
     */
    public ClientHttpRequest(String urlString) throws IOException {
        this(new URL(urlString));
    }


    public void postCookies() {
        StringBuffer cookieList = new StringBuffer();

        for (Iterator i = cookies.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) (i.next());
            cookieList.append(entry.getKey().toString() + "=" + entry.getValue());

            if (i.hasNext()) {
                cookieList.append("; ");
            }
        }
        if (cookieList.length() > 0) {
            connection.setRequestProperty("Cookie", cookieList.toString());
        }
    }

    /**
     * adds a cookie to the requst
     * @param name cookie name
     * @param value cookie value
     * @throws IOException
     */
    public void setCookie(String name, String value) throws IOException {
        cookies.put(name, value);
    }

    /**
     * adds cookies to the request
     * @param cookies the cookie "name-to-value" map
     * @throws IOException
     */
    public void setCookies(Map cookies) throws IOException {
        if (cookies == null)
            return;
        this.cookies.putAll(cookies);
    }

    /**
     * adds cookies to the request
     * @param cookies array of cookie names and values (cookies[2*i] is a name, cookies[2*i + 1] is a value)
     * @throws IOException
     */
    public void setCookies(String[] cookies) throws IOException {
        if (cookies == null)
            return;
        for (int i = 0; i < cookies.length - 1; i += 2) {
            setCookie(cookies[i], cookies[i + 1]);
        }
    }

    private void writeName(String name) throws IOException {
        newline();
        write("Content-Disposition: form-data; name=\"");
        write(name);
        write('"');
    }

    /**
     * adds a string parameter to the request
     * @param name parameter name
     * @param value parameter value
     * @throws IOException
     */
    public void setParameter(String name, String value) throws IOException {
        boundary();
        writeName(name);
        newline();
        newline();
        writeln(value);
    }

    private static void pipe(InputStream in, OutputStream out) throws
            IOException {
        byte[] buf = new byte[500000];
        int nread;
        int navailable;
        int total = 0;
        synchronized (in) {
            while ((nread = in.read(buf, 0, buf.length)) >= 0) {
                out.write(buf, 0, nread);
                total += nread;
            }
        }
        out.flush();
        buf = null;
    }

    /**
     * adds a file parameter to the request
     * @param name parameter name
     * @param filename the name of the file
     * @param is input stream to read the contents of the file from
     * @throws IOException
     */
    public void setParameter(String name, String filename, InputStream is) throws
            IOException {
        boundary();
        writeName(name);
        write("; filename=\"");
        write(filename);
        write('"');
        newline();
        write("Content-Type: ");
        String type = connection.guessContentTypeFromName(filename);
        if (type == null)
            type = "application/x-bittorrent";
        writeln(type);
        newline();
        pipe(is, os);
        newline();
    }

    /**
     * adds a file parameter to the request
     * @param name parameter name
     * @param file the file to upload
     * @throws IOException
     */
    public void setParameter(String name, File file) throws IOException {
        setParameter(name, file.getName(), new FileInputStream(file));
    }

    /**
     * adds a parameter to the request; if the parameter is a File, the file is uploaded, otherwise the string value of the parameter is passed in the request
     * @param name parameter name
     * @param object parameter value, a File or anything else that can be stringified
     * @throws IOException
     */
    public void setParameter(String name, Object object) throws IOException {
        if (object instanceof File) {
            setParameter(name, (File) object);
        } else {
            setParameter(name, object.toString());
        }
    }

    /**
     * adds parameters to the request
     * @param parameters "name-to-value" map of parameters; if a value is a file, the file is uploaded, otherwise it is stringified and sent in the request
     * @throws IOException
     */
    public void setParameters(Map parameters) throws IOException {
        if (parameters == null)
            return;
        for (Iterator i = parameters.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            setParameter(entry.getKey().toString(), entry.getValue());
        }
    }

    /**
     * adds parameters to the request
     * @param parameters array of parameter names and values (parameters[2*i] is a name, parameters[2*i + 1] is a value); if a value is a file, the file is uploaded, otherwise it is stringified and sent in the request
     * @throws IOException
     */
    public void setParameters(Object[] parameters) throws IOException {
        if (parameters == null)
            return;
        for (int i = 0; i < parameters.length - 1; i += 2) {
            setParameter(parameters[i].toString(), parameters[i + 1]);
        }
    }

    /**
     * posts the requests to the server, with all the cookies and parameters that were added
     * @return input stream with the server response
     * @throws IOException
     */
    public InputStream post() throws IOException {
        boundary();
        writeln("--");
        os.close();
        return connection.getInputStream();
    }

    /**
     * posts the requests to the server, with all the cookies and parameters that were added before (if any), and with parameters that are passed in the argument
     * @param parameters request parameters
     * @return input stream with the server response
     * @throws IOException
     */
    public InputStream post(Map parameters) throws IOException {
        setParameters(parameters);
        return post();
    }

    /**
     * posts the requests to the server, with all the cookies and parameters that were added before (if any), and with parameters that are passed in the argument
     * @param parameters request parameters
     * @return input stream with the server response
     * @throws IOException
     */
    public InputStream post(Object[] parameters) throws IOException {
        setParameters(parameters);
        return post();
    }

    /**
     * posts the requests to the server, with all the cookies and parameters that were added before (if any), and with cookies and parameters that are passed in the arguments
     * @param cookies request cookies
     * @param parameters request parameters
     * @return input stream with the server response
     * @throws IOException
     */
    public InputStream post(Map cookies, Map parameters) throws IOException {
        setCookies(cookies);
        setParameters(parameters);
        return post();
    }

    /**
     * posts the requests to the server, with all the cookies and parameters that were added before (if any), and with cookies and parameters that are passed in the arguments
     * @param cookies request cookies
     * @param parameters request parameters
     * @return input stream with the server response
     * @throws IOException
     */
    public InputStream post(String[] cookies, Object[] parameters) throws
            IOException {
        setCookies(cookies);
        setParameters(parameters);
        return post();
    }

    /**
     * post the POST request to the server, with the specified parameter
     * @param name parameter name
     * @param value parameter value
     * @return input stream with the server response
     * @throws IOException
     */
    public InputStream post(String name, Object value) throws IOException {
        setParameter(name, value);
        return post();
    }

    /**
     * post the POST request to the server, with the specified parameters
     * @param name1 first parameter name
     * @param value1 first parameter value
     * @param name2 second parameter name
     * @param value2 second parameter value
     * @return input stream with the server response
     * @throws IOException
     */
    public InputStream post(String name1, Object value1, String name2,
                            Object value2) throws IOException {
        setParameter(name1, value1);
        return post(name2, value2);
    }

    /**
     * post the POST request to the server, with the specified parameters
     * @param name1 first parameter name
     * @param value1 first parameter value
     * @param name2 second parameter name
     * @param value2 second parameter value
     * @param name3 third parameter name
     * @param value3 third parameter value
     * @return input stream with the server response
     * @throws IOException
     */
    public InputStream post(String name1, Object value1, String name2,
                            Object value2, String name3, Object value3) throws
            IOException {
        setParameter(name1, value1);
        return post(name2, value2, name3, value3);
    }

    /**
     * post the POST request to the server, with the specified parameters
     * @param name1 first parameter name
     * @param value1 first parameter value
     * @param name2 second parameter name
     * @param value2 second parameter value
     * @param name3 third parameter name
     * @param value3 third parameter value
     * @param name4 fourth parameter name
     * @param value4 fourth parameter value
     * @return input stream with the server response
     * @throws IOException
     */
    public InputStream post(String name1, Object value1, String name2,
                            Object value2, String name3, Object value3,
                            String name4, Object value4) throws IOException {
        setParameter(name1, value1);
        return post(name2, value2, name3, value3, name4, value4);
    }

    /**
     * posts a new request to specified URL, with parameters that are passed in the argument
     * @param parameters request parameters
     * @return input stream with the server response
     * @throws IOException
     */
    public static InputStream post(URL url, Map parameters) throws IOException {
        return new ClientHttpRequest(url).post(parameters);
    }

    /**
     * posts a new request to specified URL, with parameters that are passed in the argument
     * @param parameters request parameters
     * @return input stream with the server response
     * @throws IOException
     */
    public static InputStream post(URL url, Object[] parameters) throws
            IOException {
        return new ClientHttpRequest(url).post(parameters);
    }

    /**
     * posts a new request to specified URL, with cookies and parameters that are passed in the argument
     * @param cookies request cookies
     * @param parameters request parameters
     * @return input stream with the server response
     * @throws IOException
     */
    public static InputStream post(URL url, Map cookies, Map parameters) throws
            IOException {
        return new ClientHttpRequest(url).post(cookies, parameters);
    }

    /**
     * posts a new request to specified URL, with cookies and parameters that are passed in the argument
     * @param cookies request cookies
     * @param parameters request parameters
     * @return input stream with the server response
     * @throws IOException
     */
    public static InputStream post(URL url, String[] cookies,
                                   Object[] parameters) throws IOException {
        return new ClientHttpRequest(url).post(cookies, parameters);
    }

    /**
     * post the POST request specified URL, with the specified parameter
     * @param name1 parameter name
     * @param value1 parameter value
     * @return input stream with the server response
     * @throws IOException
     */
    public static InputStream post(URL url, String name1, Object value1) throws
            IOException {
        return new ClientHttpRequest(url).post(name1, value1);
    }

    /**
     * post the POST request to specified URL, with the specified parameters
     * @param name1 first parameter name
     * @param value1 first parameter value
     * @param name2 second parameter name
     * @param value2 second parameter value
     * @return input stream with the server response
     * @throws IOException
     */
    public static InputStream post(URL url, String name1, Object value1,
                                   String name2, Object value2) throws
            IOException {
        return new ClientHttpRequest(url).post(name1, value1, name2, value2);
    }

    /**
     * post the POST request to specified URL, with the specified parameters
     * @param name1 first parameter name
     * @param value1 first parameter value
     * @param name2 second parameter name
     * @param value2 second parameter value
     * @param name3 third parameter name
     * @param value3 third parameter value
     * @return input stream with the server response
     * @throws IOException
     */
    public static InputStream post(URL url, String name1, Object value1,
                                   String name2, Object value2, String name3,
                                   Object value3) throws IOException {
        return new ClientHttpRequest(url).post(name1, value1, name2, value2,
                                               name3, value3);
    }

    /**
     * post the POST request to specified URL, with the specified parameters
     * @param name1 first parameter name
     * @param value1 first parameter value
     * @param name2 second parameter name
     * @param value2 second parameter value
     * @param name3 third parameter name
     * @param value3 third parameter value
     * @param name4 fourth parameter name
     * @param value4 fourth parameter value
     * @return input stream with the server response
     * @throws IOException
     */
    public static InputStream post(URL url, String name1, Object value1,
                                   String name2, Object value2, String name3,
                                   Object value3, String name4, Object value4) throws
            IOException {
        return new ClientHttpRequest(url).post(name1, value1, name2, value2,
                                               name3, value3, name4, value4);
    }
}
