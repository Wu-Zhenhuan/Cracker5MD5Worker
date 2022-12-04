import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Cracker5MD5 Worker started!");
        if (args.length >= 1) {
            System.out.println("port number = " + args[0]);
        } else {
            System.out.println("Please use first command-line argument to set TCP port number.");
            return;
        }
        try (
                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]))) {
            while (true) {
                Socket socket = serverSocket.accept();
                WorkerSession workerSession = new WorkerSession(socket);
                workerSession.start();
            }
        } catch (Exception e) {
            System.err.println("Echo Server exits at main() due to the following:");
            e.printStackTrace();
            System.exit(0);
        }
    }

}

class WorkerSession extends Thread {
    static final char[] ALPHABET = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    static MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private Socket session;

    WorkerSession(Socket session) {
        this.session = session;
    }

    public void run() {
        System.out.println("Client at " + session.getInetAddress() + ":" + session.getPort() + " are connected!");
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(session.getInputStream()));
                PrintWriter writer = new PrintWriter(session.getOutputStream(), true)) {

            System.out.print("Waiting for user input from terminal..."); System.out.flush();
            while (!reader.ready()) {

            }
            String line = reader.readLine();
            System.out.println("\nTask:" + line);
            int start = getIndex(line.charAt(0));
            int end = getIndex(line.charAt(1));
            if ((start == -1) || (end == -1) || (start > end)) {
                System.err.println("Illegal range input!");
                writer.println("Illegal range input!");
                return;
            }
            String cypher = line.substring(2);
            System.out.println("First character range: from '" + ALPHABET[start] + "' to '" + ALPHABET[end] + "'");
            writer.println("First character range: from '" + ALPHABET[start] + "' to '" + ALPHABET[end] + "'");
            System.out.println("Cypher: " + cypher);
            writer.println("Cypher: " + cypher);

            String plain = this.crack(start, end, cypher);
            if (plain == null) {
                System.err.println("Unable to crack!");
                writer.println("Unable to crack!");
            } else {
                System.out.println("Plain: " + plain);
                writer.println("Plain: " + plain);
            }

        } catch (Exception e) {
            System.err.println("Cracker5MD5 Work exits at WorkerSession.run() due to the following:");
            e.printStackTrace();
            System.exit(0);
        }
    }
    private static int getIndex (char c) {
        if (('A' <= c) && (c <= 'Z')) {
            return (int) c - 65;
        } else if (('a' <= c) && (c <= 'z')) {
            return (int) c - 71;
        } else {
            return -1;
        }
    }

    private String crack(int start, int end, String cypher) {
        for (int i = start; i < ALPHABET.length; i++) {
            for (int j = 0; j < ALPHABET.length; j++) {
                for (int k = 0; k < ALPHABET.length; k++) {
                    for (int l = 0; l < ALPHABET.length; l++) {
                        for (int m = 0; m < ALPHABET.length; m++) {
                            String combo = new StringBuilder()
                                    .append(ALPHABET[i])
                                    .append(ALPHABET[j])
                                    .append(ALPHABET[k])
                                    .append(ALPHABET[l])
                                    .append(ALPHABET[m]).toString();
                            byte[] pre = combo.getBytes(StandardCharsets.UTF_8);
                            byte[] hash = md.digest(pre);
                            BigInteger numerical = new BigInteger(1, hash);
                            String string = numerical.toString(16);
                            if (string.equals(cypher)) {
                                return combo;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}