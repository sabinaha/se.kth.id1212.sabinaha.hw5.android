package se.kth.id1212.sabinaha.hw5.android.client.net;

public interface ServerCallback {
    void messageSent();
    void messageReceived(String receivedMessage);
    void notifyConnected();
    void notifyDisconnect();
}
