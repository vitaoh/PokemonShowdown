package network;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Gerenciador de comunicação de rede thread-safe
 * Responsável por enviar e receber mensagens via socket
 */
public class NetworkManager extends Thread {
    
    private final Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    
    private final BlockingQueue<Message> messageQueue;
    private final AtomicBoolean running;
    private final AtomicBoolean connected;
    
    private MessageListener messageListener;
    private ConnectionListener connectionListener;
    
    // Interfaces para callbacks
    public interface MessageListener {
        void onMessageReceived(Message message);
    }
    
    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onError(Exception e);
    }
    
    public NetworkManager(Socket socket) {
        this.socket = socket;
        this.messageQueue = new LinkedBlockingQueue<>(NetworkConstants.MESSAGE_QUEUE_SIZE);
        this.running = new AtomicBoolean(false);
        this.connected = new AtomicBoolean(false);
        
        setName("NetworkManager-" + socket.getRemoteSocketAddress());
        setDaemon(true);
    }
    
    /**
     * Inicializa as streams de entrada e saída
     */
    public boolean initialize() {
        try {
            // Importante: ObjectOutputStream primeiro!
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            
            input = new ObjectInputStream(socket.getInputStream());
            
            connected.set(true);
            
            if (connectionListener != null) {
                connectionListener.onConnected();
            }
            
            return true;
            
        } catch (IOException e) {
            System.err.println("Erro ao inicializar NetworkManager: " + e.getMessage());
            if (connectionListener != null) {
                connectionListener.onError(e);
            }
            return false;
        }
    }
    
    /**
     * Thread principal para recepção de mensagens
     */
    @Override
    public void run() {
        running.set(true);
        
        while (running.get() && connected.get()) {
            try {
                // Receber mensagem
                Object obj = input.readObject();
                if (obj instanceof Message) {
                    Message message = (Message) obj;
                    
                    // Processar mensagem via listener
                    if (messageListener != null) {
                        messageListener.onMessageReceived(message);
                    }
                    
                } else {
                    System.err.println("Objeto recebido não é uma mensagem válida: " + obj.getClass());
                }
                
            } catch (EOFException e) {
                // Conexão fechada normalmente
                break;
                
            } catch (SocketException e) {
                // Conexão perdida
                System.out.println("Conexão perdida: " + e.getMessage());
                break;
                
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro na comunicação: " + e.getMessage());
                if (connectionListener != null) {
                    connectionListener.onError(e);
                }
                break;
            }
        }
        
        disconnect();
    }
    
    /**
     * Envia uma mensagem pela rede
     */
    public synchronized boolean sendMessage(Message message) {
        if (!connected.get() || output == null) {
            return false;
        }
        
        try {
            output.writeObject(message);
            output.flush();
            return true;
            
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
            if (connectionListener != null) {
                connectionListener.onError(e);
            }
            disconnect();
            return false;
        }
    }
    
    /**
     * Envia mensagem de forma assíncrona
     */
    public boolean sendMessageAsync(MessageType type, String sender, Object data) {
        Message message = new Message(type, sender, data);
        return messageQueue.offer(message);
    }
    
    /**
     * Desconecta e limpa recursos
     */
    public void disconnect() {
        running.set(false);
        connected.set(false);
        
        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException e) {
            // Ignorar erro no fechamento
        }
        
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            // Ignorar erro no fechamento
        }
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignorar erro no fechamento
        }
        
        if (connectionListener != null) {
            connectionListener.onDisconnected();
        }
    }
    
    // Getters
    public boolean isConnected() {
        return connected.get();
    }
    
    public boolean isRunning() {
        return running.get();
    }
    
    public String getRemoteAddress() {
        return socket.getRemoteSocketAddress().toString();
    }
    
    // Setters para listeners
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }
    
    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }
    
    /**
     * Envia heartbeat para manter conexão viva
     */
    public boolean sendHeartbeat(String sender) {
        return sendMessage(new Message(MessageType.HEARTBEAT, sender, "ping"));
    }
}