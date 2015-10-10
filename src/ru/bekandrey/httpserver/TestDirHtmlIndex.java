package ru.bekandrey.httpserver;

import java.io.IOException;

/**
 * Created by Bek on 03.10.2015.
 */
public class TestDirHtmlIndex {
    private static int DEFAULTPORT = 8080;
    public static void main(String[] args) throws IOException {
        MyHTTPServer httpServer = new MyHTTPServer();
        int port = 0;
        if (args.length > 0) {
            String sPort = args[0];
            try {
                port = new Integer(sPort);
            }
            catch (Exception e){
                port = 0;
            }
        }
        port = port > 0?port:DEFAULTPORT;
        System.out.println("server port=" + port);
        httpServer.serverRun(port, "");

//        File inPath = new File(((args.length == 0)?"..":args[0]));
//        try(Writer w = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("index.html")))) {
//            DirectoryHtmlIndex dhi = new DirectoryHtmlIndex();
//            dhi.readDirToStream(w, inPath);
//            w.close();
//        }
    }
}
