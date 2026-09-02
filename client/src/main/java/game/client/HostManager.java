package game.client;

import game.server.GameServer;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class HostManager {

    private GameServer server;

    private int port;

    public synchronized boolean startServer(int port) {

        if (server != null && server.isRunning()) {

            System.out.println(
                    "Server is already running"
            );

            return false;
        }

        try {

            server =
                    new GameServer(port);

            this.port =
                    server.getPort();

            Thread serverThread =
                    new Thread(
                            server::start,
                            "Embedded-GameServer"
                    );

            serverThread.setDaemon(true);
            serverThread.start();

            System.out.println(
                    "Embedded server started on port "
                            + this.port
            );

            return true;

        } catch (IOException e) {

            System.err.println(
                    "Failed to start embedded server"
            );

            e.printStackTrace();

            server = null;

            return false;
        }
    }

    public synchronized void stopServer() {

        if (server == null) {
            return;
        }

        System.out.println(
                "Stopping embedded server..."
        );

        server.stop();

        server = null;
    }

    public synchronized boolean isRunning() {

        return server != null
                && server.isRunning();
    }

    public synchronized int getPort() {
        return port;
    }

    public synchronized GameServer getServer() {
        return server;
    }

    // =====================================
    // NETWORK ADDRESSES
    // =====================================

    public List<String> getLocalAddresses() {

        List<String> addresses =
                new ArrayList<>();

        try {

            Enumeration<NetworkInterface> interfaces =
                    NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {

                NetworkInterface networkInterface =
                        interfaces.nextElement();

                if (
                        networkInterface.isLoopback()
                                ||
                                !networkInterface.isUp()
                ) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses =
                        networkInterface.getInetAddresses();

                while (inetAddresses.hasMoreElements()) {

                    InetAddress address =
                            inetAddresses.nextElement();

                    if (
                            address instanceof Inet4Address
                                    &&
                                    !address.isLoopbackAddress()
                    ) {

                        addresses.add(
                                address.getHostAddress()
                        );
                    }
                }
            }

        } catch (SocketException e) {

            e.printStackTrace();
        }

        return addresses;
    }

    public List<String> getConnectionAddresses() {

        List<String> result =
                new ArrayList<>();

        for (String address : getLocalAddresses()) {

            result.add(
                    address + ":" + getPort()
            );
        }

        return result;
    }
}