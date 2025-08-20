package server;

import network.*;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;

public class PokemonServer extends Thread {

    private final int port;
    private ServerSocket serverSocket;
    private final AtomicBoolean running;

    private final List<RematchRequest> pendingRequests;

    // Thread pool para gerenciar clientes
    private final ExecutorService clientThreadPool;
    private final List<ClientHandler> clients;
    private final List<GameSession> activeSessions;

    private final Map<String, Set<String>> rematchRequests = new HashMap<>();

    // Interface gráfica do servidor (opcional)
    private Object serverFrame; // Usando Object para evitar dependência circular

    public PokemonServer() {
        this(NetworkConstants.DEFAULT_PORT);
    }

    public PokemonServer(int port) {
        this.port = port;
        this.running = new AtomicBoolean(false);
        this.clients = new ArrayList<>();
        this.activeSessions = new ArrayList<>();

        // Thread pool para clientes
        this.clientThreadPool = Executors.newFixedThreadPool(NetworkConstants.MAX_CLIENTS);

        this.pendingRequests = new ArrayList<>();

        setName("PokemonServer-" + port);
        setDaemon(false);
    }

    public boolean startServer() {
        try {
            serverSocket = new ServerSocket(port, NetworkConstants.SERVER_BACKLOG);
            running.set(true);

            System.out.println("🎮 Servidor Pokémon iniciado na porta " + port);
            System.out.println("📡 Aguardando conexões de clientes...");

            // Iniciar interface gráfica do servidor (se disponível)
            updateServerFrameIfExists("Rodando na porta " + port, 0, 0);

            return true;

        } catch (IOException e) {
            System.err.println("❌ Erro ao iniciar servidor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Thread principal do servidor - aceita conexões
     */
    @Override
    public void run() {
        if (!startServer()) {
            return;
        }

        while (running.get()) {
            try {
                // Aceitar nova conexão
                Socket clientSocket = serverSocket.accept();

                // Verificar limite de clientes
                if (clients.size() >= NetworkConstants.MAX_CLIENTS) {
                    rejectClient(clientSocket, "Servidor lotado");
                    continue;
                }

                // Criar handler para o cliente
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);

                synchronized (clients) {
                    clients.add(clientHandler);
                }

                // Executar handler no thread pool
                clientThreadPool.execute(clientHandler);

                System.out.println("✅ Nova conexão aceita: " + clientSocket.getRemoteSocketAddress());
                updateServerStats();

            } catch (IOException e) {
                if (running.get()) {
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        }

        shutdown();
    }

    /**
     * Para o servidor
     */
    public void stopServer() {
        running.set(false);

        // Notificar todos os clientes
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.disconnect();
            }
            clients.clear();
        }

        // Fechar socket do servidor
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar servidor: " + e.getMessage());
        }

        System.out.println("🛑 Servidor Pokémon parado");
    }

    /**
     * Shutdown completo
     */
    private void shutdown() {
        // Parar thread pool
        clientThreadPool.shutdown();
        try {
            if (!clientThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientThreadPool.shutdownNow();
        }

        // Fechar sessões ativas
        synchronized (activeSessions) {
            for (GameSession session : activeSessions) {
                session.endSession();
            }
            activeSessions.clear();
        }

        System.out.println("🔧 Servidor finalizado completamente");
    }

// Modificar handleRematchRequest para rastrear ambos os jogadores
    public void handleRematchRequest(ClientHandler requester, RematchRequest request) {
        synchronized (activeSessions) {
            GameSession session = findSessionByPlayer(requester);
            if (session == null) {
                requester.sendError("Sessão não encontrada");
                return;
            }

            ClientHandler opponent = session.getOpponent(requester);
            if (opponent == null || !opponent.isConnected()) {
                requester.sendMessage(MessageType.REMATCH_DECLINED, "Server",
                        "Oponente não está mais conectado");
                return;
            }

            String sessionId = session.getSessionId();

            // Inicializar set se não existir
            rematchRequests.putIfAbsent(sessionId, new HashSet<>());
            Set<String> requesters = rematchRequests.get(sessionId);

            // Adicionar solicitação do jogador atual
            requesters.add(requester.getPlayerName());

            System.out.println("🔄 Revanche solicitada por: " + requester.getPlayerName()
                    + " (Sessão: " + sessionId + ")");

            // Verificar se ambos solicitaram
            if (requesters.size() == 2) {
                // Ambos solicitaram - iniciar processo de nova seleção
                System.out.println("✅ Ambos jogadores solicitaram revanche! Iniciando nova seleção...");

                // Limpar solicitações
                rematchRequests.remove(sessionId);

                // Encerrar sessão atual
                session.endSession();

                // Resetar estado dos jogadores
                requester.resetForRematch();
                opponent.resetForRematch();

                // Enviar mensagem para ambos iniciarem nova seleção
                requester.sendMessage(MessageType.TEAM_SELECTION_RESTART, "Server",
                        "Revanche aceita! Escolha seus pokémon novamente.");
                opponent.sendMessage(MessageType.TEAM_SELECTION_RESTART, "Server",
                        "Revanche aceita! Escolha seus pokémon novamente.");

            } else {
                // Apenas um solicitou - notificar que está aguardando o outro
                requester.sendMessage(MessageType.NOTIFICATION, "Server",
                        "Aguardando " + opponent.getPlayerName() + " também solicitar revanche...");

                // Notificar oponente que alguém quer revanche
                opponent.sendMessage(MessageType.NOTIFICATION, "Server",
                        requester.getPlayerName() + " quer revanche! Você também quer?");
            }
        }
    }

    // Adicionar método para limpar solicitações quando jogador sai
    public void clearRematchRequests(String sessionId) {
        synchronized (activeSessions) {
            rematchRequests.remove(sessionId);
        }
    }

    private ClientHandler findClientByName(String playerName) {
        synchronized (clients) {
            return clients.stream()
                    .filter(client -> client.getPlayerName().equals(playerName))
                    .findFirst()
                    .orElse(null);
        }
    }

    private GameSession findSessionByPlayer(ClientHandler player) {
        return activeSessions.stream()
                .filter(session -> session.hasPlayer(player))
                .findFirst()
                .orElse(null);
    }

// Limpeza periódica de solicitações expiradas
    public void cleanupExpiredRequests() {
        synchronized (activeSessions) {
            pendingRequests.removeIf(RematchRequest::isExpired);
        }
    }

    /**
     * Rejeita cliente (servidor lotado)
     */
    private void rejectClient(Socket clientSocket, String reason) {
        try {
            NetworkManager tempManager = new NetworkManager(clientSocket);
            if (tempManager.initialize()) {
                Message rejection = new Message(MessageType.ERROR, "Server", reason);
                tempManager.sendMessage(rejection);
            }
            clientSocket.close();

            System.out.println("❌ Cliente rejeitado: " + reason);

        } catch (IOException e) {
            // Ignorar erro na rejeição
        }
    }

    /**
     * Remove cliente da lista
     */
    public void removeClient(ClientHandler client) {
        synchronized (clients) {
            clients.remove(client);
        }
        updateServerStats();

        System.out.println("📤 Cliente removido. Total: " + clients.size());
    }

    /**
     * Procura oponente disponível para um jogador
     */
    public ClientHandler findAvailableOpponent(ClientHandler player) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != player
                        && client.isConnected()
                        && client.isTeamReady()
                        && client.getCurrentSession() == null) {
                    return client;
                }
            }
        }
        return null;
    }

    /**
     * Cria nova sessão de jogo
     */
    public GameSession createGameSession(ClientHandler player1, ClientHandler player2) {
        GameSession session = new GameSession(player1, player2);

        synchronized (activeSessions) {
            activeSessions.add(session);
        }

        System.out.println("🎲 Nova sessão de jogo criada: " + session.getSessionId());
        updateServerStats();
        return session;
    }

    /**
     * Remove sessão de jogo
     */
    public void removeGameSession(GameSession session) {
        synchronized (activeSessions) {
            activeSessions.remove(session);
        }

        System.out.println("🏁 Sessão finalizada: " + session.getSessionId());
        updateServerStats();
    }

    /**
     * Atualiza estatísticas do servidor
     */
    private void updateServerStats() {
        updateServerFrameIfExists(null, clients.size(), activeSessions.size());
    }

    /**
     * Atualiza interface do servidor se existir (usando reflection para evitar
     * dependências)
     */
    private void updateServerFrameIfExists(String status, int clientCount, int sessionCount) {
        if (serverFrame != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    if (status != null) {
                        // serverFrame.setServerStatus(status);
                        java.lang.reflect.Method setStatus = serverFrame.getClass().getMethod("setServerStatus", String.class);
                        setStatus.invoke(serverFrame, status);
                    }

                    if (clientCount >= 0) {
                        // serverFrame.updateClientCount(clientCount);
                        java.lang.reflect.Method updateClient = serverFrame.getClass().getMethod("updateClientCount", int.class);
                        updateClient.invoke(serverFrame, clientCount);
                    }

                    if (sessionCount >= 0) {
                        // serverFrame.updateSessionCount(sessionCount);
                        java.lang.reflect.Method updateSession = serverFrame.getClass().getMethod("updateSessionCount", int.class);
                        updateSession.invoke(serverFrame, sessionCount);
                    }

                } catch (Exception e) {
                    // Ignorar erros de reflection - interface opcional
                }
            });
        }
    }

    // Getters
    public int getPort() {
        return port;
    }

    public int getClientCount() {
        synchronized (clients) {
            return clients.size();
        }
    }

    public int getActiveSessionCount() {
        synchronized (activeSessions) {
            return activeSessions.size();
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    // Setter para GUI
    public void setServerFrame(Object serverFrame) {
        this.serverFrame = serverFrame;
    }

    public static void main(String[] args) {
        int port = NetworkConstants.DEFAULT_PORT;

        // Parse argumentos de linha de comando
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Porta inválida, usando padrão: " + NetworkConstants.DEFAULT_PORT);
            }
        }

        // Criar e iniciar servidor
        PokemonServer server = new PokemonServer(port);

        // Adicionar hook para shutdown gracioso
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Parando servidor...");
            server.stopServer();
        }));

        // Iniciar servidor
        server.start();

        // Manter thread principal viva
        try {
            server.join();
        } catch (InterruptedException e) {
            System.out.println("Servidor interrompido");
        }
    }
}
