package ru.bekandrey.httpserver;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by Bek on 03.10.2015.
 */
public class DirectoryHtmlIndex {
        public static String readDirToStream(OutputStream outputStream, File inFile, boolean headerOnly) throws IOException {
            String currentDir = inFile.getPath();

            class myComparator implements Comparator<File> {
                @Override
                public int compare(File f1, File f2) {
                    int res = 0;
                    if (f1.isDirectory() && !f2.isDirectory()) {
                        res = -1;
                    }
                    if (!f1.isDirectory() && f2.isDirectory()) {
                        res = 1;
                    }
                    if (res == 0) {
                        res = f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
                    }
                    return res;
                }
            }


            if (inFile.isDirectory()) {

                Writer outWriter = new OutputStreamWriter(outputStream);
                List<File> fList = new ArrayList<>();

                for (File file : inFile.listFiles()) {
                    if (file.getName().equals("index.html")) {
                        readIndexFileToStream(outWriter, file);
                        return inFile.getPath();
                    } else {
                        fList.add(file);
                    }
                }

                Collections.sort(fList, new myComparator());
                MessageFormat formatDateTime = new MessageFormat("{0, date, yyyy/MM/dd hh:mm:ss}");

                outWriter.write("HTTP/1.0 200 OK\r\n");
                outWriter.write("Content-Type: text/html\r\n");

                if(!headerOnly) {
                    outWriter.write("\r\n");
                    outWriter.write("<html>\n");

                    outWriter.write("<head>\n");
                    outWriter.write("   <meta charset = \"windows-1251\"></meta>");
                    outWriter.write("   <title> Content of folder: " + inFile.getName() + "</title>\n");
                    outWriter.write("</head>\n");


                    outWriter.write("<body>\n");
                    outWriter.write("   <H1> Content of folder: " + inFile.getName() + "</H1>\n");
                    outWriter.write("   <table>\n");
                    outWriter.write("       <tr>\n");
                    outWriter.write("           <td>..</td>\n");
                    outWriter.write("           <td><a href='../'>..</a></td>\n");
                    outWriter.write("           <td>" + formatDateTime.format(new Object[]{new Date(inFile.lastModified())}) + "</td>\n");
                    outWriter.write("       </tr>\n");
                    for (File file : fList) {
                        outWriter.write("       <tr>\n");
                        outWriter.write("           <td>" + (file.isDirectory() ? "dir" : "") + "</td>\n");
                        outWriter.write("           <td><a href= " + URLEncoder.encode(file.getName(), "windows-1251") + ">" + file.getName() + "</a></td>\n");
                        outWriter.write("           <td>" + formatDateTime.format(new Object[]{new Date(file.lastModified())}) + "</td>\n");
                        outWriter.write("       </tr>\n");
                    }
                    outWriter.write("   </table>\n");
                    outWriter.write("</body>\n");
                    outWriter.write("</html>\n");
                }
                outWriter.close();
            } else { // получаем сам файл

                String mimeType = new MimetypesFileTypeMap().getContentType(inFile);

                outputStream.write(("HTTP/1.0 200 OK\r\n").getBytes());
                outputStream.write(("Content-Type:" + mimeType + "\r\n").getBytes());
                outputStream.write((("Content-Length: " + inFile.length() + "\r\n")).getBytes());

                if(!headerOnly) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));

                    int curReadChar;
                    while ((curReadChar = r.read()) != -1) {
                        outputStream.write(curReadChar);
                    }
                }
                outputStream.close();
            }

            return currentDir;
        }

        private static void readIndexFileToStream(Writer outWriter, File indexFile) throws IOException {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(indexFile)));
            int curReadChar;
            while ((curReadChar = r.read()) != -1){
                outWriter.write(curReadChar);
            }
            outWriter.close();
        }

}
