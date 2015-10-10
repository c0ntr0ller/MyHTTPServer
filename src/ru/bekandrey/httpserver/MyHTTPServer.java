package ru.bekandrey.httpserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;

/**
 * Created by Bek on 06.10.2015.
 */
public class MyHTTPServer {
    private static String DEFAULTCOMMAND = "HEAD";
    private static String DEFAULTPATH = System.getProperty("user.dir");

    public void serverRun(int port, String homeDir) throws IOException {
        if (homeDir.equals("")){
            homeDir = DEFAULTPATH;
        }

        int socCount = 0;
        System.out.println("serverRun");
        ServerSocket serverSocket = new ServerSocket(port);
        while (true){
            Socket newSocket = serverSocket.accept();
            System.out.println("serverSocket.accept; Count=" + socCount);
            new Thread(new ServerProcessor(newSocket, homeDir)).start();
        }

    }

    private class ServerProcessor implements Runnable{
        private Socket s;
        private InputStream inputStream;
        private OutputStream outputStream;
        private String homeDir;
        private String currentDir;

        public ServerProcessor(Socket inSocket, String homeDir) throws IOException {
            s = inSocket;
            inputStream = inSocket.getInputStream();
            outputStream = inSocket.getOutputStream();
            this.homeDir = homeDir;
            this.currentDir = homeDir;
        }

        @Override
        public void run() {
            try(Socket nSocket = s) {
                String requestString = readRequest();
                String resourceString = extractResourceName(requestString);
                String commandString = extractCommandName(requestString).toUpperCase();

                System.out.println("requestString=" +requestString);
                System.out.println("resourceString=" + resourceString);
                System.out.println("commandString=" + commandString);

                switch (commandString){
                    case "HEAD":{
                        currentDir = sendData(resourceString, true);
                        break;
                    }
                    case "GET":{
                        currentDir = sendData(resourceString, false);
                        break;
                    }
                    default:{
                        sendError(501, "Unsupported command");
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * читает запрос из буфера
         */
        private String readRequest() throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String bs;
            while(true){
                bs = bufferedReader.readLine();

                stringBuilder.append(bs);

                break;
            }
            return stringBuilder.toString();
        }

        /**
         * выделяет из строки запроса указанный путь
         */
        private String extractResourceName(String request) throws UnsupportedEncodingException {
            String path = "";

            int pathstart = request.indexOf(" ") + 1;

            if (pathstart > 1){
                int pathend = request.indexOf(" ",pathstart);

                if (pathend > 1) {
                    path = request.substring(pathstart, pathend);
                }
            }
            path = currentDir + path;
            path = path.isEmpty()||path.equals("/")?homeDir : path;

            return URLDecoder.decode(path, "windows-1251");
        }

        private String extractCommandName(String request){
            String command = "";
            int commstart = 0; //request.indexOf("?") + 1;
            int pathstart = request.indexOf(" ",commstart);

            if (pathstart > 1) {
                command = request.substring(commstart, pathstart);
            }
            return command.isEmpty()?DEFAULTCOMMAND:command;
        }

        private String sendData(String resourcePathString, boolean headerOnly) throws IOException {
            String curDir = homeDir;
            File requestFile = new File(resourcePathString);

            if(!requestFile.exists()){
              sendError(404, "File not found");
            }
            curDir = DirectoryHtmlIndex.readDirToStream(outputStream, requestFile, headerOnly);
            return curDir;
        }

        private void sendError(int errorCode, String message) throws IOException {
            System.out.println("Error = " + errorCode);

            Writer outWriter = new OutputStreamWriter(outputStream);
            outWriter.write("HTTP/1.0 " + errorCode + " " + message + "\r\n");
            outWriter.write("Content-Type: text/html\r\n");
            outWriter.write("\r\n");
            outWriter.write("<html>\n");

            outWriter.write("<head>\n");
            outWriter.write("   <meta charset = \"windows-1251\"></meta>");
            outWriter.write("   <title> 404 Error;" +  message + "</title>\n");
            outWriter.write("</head>\n");
            outWriter.write("</html>\n");
            outWriter.close();
        }
    }
}
