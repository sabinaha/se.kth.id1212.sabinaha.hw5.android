package server.net;

import server.controller.GameController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The class for accepting client requests and handling them.
 */
public class ConnectionHandler {

    private static final int LINGER_TIME = 5000;

    private GameController gameController;

    /**
     * Creates a server socket that listens for connections and accepts them as well as
     * delegating the connections to new threads.
     * @param port The port to listen on.
     * @param gameController The <code>GameController</code> to be used by the connecting clients.
     */
    public ConnectionHandler(int port, GameController gameController) {
        // This listens for incoming connections and handles them in a separate thread in the net layer.
        this.gameController = gameController;
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                Socket socket = serverSocket.accept();
                socket.setSoLinger(true, LINGER_TIME);
                Thread handlerThread = new Thread(new ClientHandler(socket, this.gameController));
                handlerThread.setPriority(Thread.MAX_PRIORITY);
                handlerThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
