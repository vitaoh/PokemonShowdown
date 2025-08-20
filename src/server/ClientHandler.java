package server;

import gui.BattleEndFrame;
import gui.RematchDialog;
import network.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.UUID;
import javax.swing.SwingUtilities;
import network.BattleInitPayload;

public class ClientHandler extends Thread implements NetworkManager.MessageListener, NetworkManager.ConnectionListener {

    private final Socket clientSocket;
    private final PokemonServer server;
    private NetworkManager networkManager;

    // Informações do cliente
    private String playerName;
    private String clientId;
    private final AtomicBoolean connected;

    // Estado do jogo
    private boolean teamReady = false;
    private java.util.List<pokemon.Species> playerTeam;
    private GameSession currentSession;

    // Implementar novos métodos no ClientHandler.java
    private BattleEndFrame battleEndFrame;

    public ClientHandler(Socket clientSocket, PokemonServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.clientId = UUID.randomUUID().toString().substring(0, 8);
        this.connected = new AtomicBoolean(false);

        setName("ClientHandler-" + clientId);
        setDaemon(true);
    }

    @Override
    public void run() {
        System.out.println("🔗 Iniciando handler para cliente: " + clientSocket.getRemoteSocketAddress());

        try {
            // Inicializar NetworkManager
            networkManager = new NetworkManager(clientSocket);
            networkManager.setMessageListener(this);
            networkManager.setConnectionListener(this);

            if (networkManager.initialize()) {
                connected.set(true);
                networkManager.start();

                // Aguardar mensagens
                networkManager.join();

            } else {
                System.err.println("❌ Falha ao inicializar comunicação com cliente");
            }

        } catch (Exception e) {
            System.err.println("❌ Erro no cliente handler: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    // Implementação de NetworkManager.MessageListener
    @Override
    public void onMessageReceived(Message message) {
        try {
            processMessage(message);
        } catch (Exception e) {
            System.err.println("❌ Erro ao processar mensagem: " + e.getMessage());
            sendError("Erro interno do servidor");
        }
    }

    private void handleRematchRequestToServer(Message message) {
        try {
            RematchRequest request = (RematchRequest) message.getData();
            server.handleRematchRequest(this, request);
        } catch (ClassCastException e) {
            sendError("Dados de solicitação de revanche inválidos");
        }
    }

    private void handleRematchResponseToServer(Message message) {
        try {
            Boolean accepted = (Boolean) message.getData();
//            server.handleRematchResponse(this, accepted);
        } catch (ClassCastException e) {
            sendError("Resposta de revanche inválida");
        }
    }

    private void handleTeamSelectionRestart(Message message) {
        System.out.println("🔄 Reiniciando seleção de time para revanche: " + playerName);

        SwingUtilities.invokeLater(() -> {
            // Enviar mensagem diretamente para o cliente para abrir nova seleção
            sendMessage(MessageType.TEAM_SELECTION_START, "Server", message.getData());
        });
    }

    /**
     * Processa mensagens recebidas do cliente
     */
    private void processMessage(Message message) {
        switch (message.getType()) {
            case TEAM_SELECTION_RESTART:
                handleTeamSelectionRestart(message);
                break;
            case REMATCH_REQUEST:
                handleRematchRequestToServer(message);
                break;
            case REMATCH_RESPONSE:
                handleRematchResponseToServer(message);
                break;
//            case REMATCH_OFFER:
//                handleRematchOffer(message);
//                break;
            case REMATCH_START:
                handleRematchStart(message);
                break;
//            case REMATCH_DECLINED:
//                handleRematchDeclined(message);
//                break;
            case BATTLE_END:
                handleBattleEnd(message);
                break;
            case CONNECT_REQUEST:
                handleConnectRequest(message);
                break;

            case PLAYER_JOIN:
                handlePlayerJoin(message);
                break;

            case TEAM_SELECTION_COMPLETE:
                handleTeamSelection(message);
                break;

            case MOVE_EXECUTE:
                handleMoveExecution(message);
                break;

            case DISCONNECT:
                handleDisconnect(message);
                break;

            case HEARTBEAT:
                handleHeartbeat(message);
                break;

            default:
                System.out.println("📨 Mensagem não tratada: " + message.getType() + " de " + playerName);
                break;
        }
    }

    private void handleBattleEnd(Message message) {
        try {
            BattleEndData data = (BattleEndData) message.getData();

            SwingUtilities.invokeLater(() -> {
                battleEndFrame = new BattleEndFrame(
                        data.getPlayerName(),
                        data.getOpponentName(),
                        data.getResult(),
                        data.isWinner()
                );

                battleEndFrame.setRematchRequestCallback(accepted -> {
                    if (accepted) {
                        // Enviar solicitação de revanche para o servidor
                        RematchRequest request = new RematchRequest(
                                data.getPlayerName(),
                                data.getOpponentName()
                        );
                        sendMessage(MessageType.REMATCH_REQUEST, playerName, request);
                    }
                });

                battleEndFrame.setVisible(true);
            });

        } catch (ClassCastException e) {
            sendError("Dados de fim de batalha inválidos");
        }
    }

// Modificar handleRematchRequest para não usar mais o sistema antigo
    private void handleRematchRequest(Message message) {
        // Esta mensagem vai do cliente para o servidor
        System.out.println("📤 Solicitação de revanche enviada por: " + playerName);
        // O servidor processará e verificará se ambos solicitaram
    }

//    private void handleRematchOffer(Message message) {
//        try {
//            RematchRequest request = (RematchRequest) message.getData();
//
//            SwingUtilities.invokeLater(() -> {
//                RematchDialog dialog = new RematchDialog(request.getRequesterName());
//
//                dialog.setResponseCallback(accepted -> {
//                    // Enviar resposta para o servidor
//                    sendMessage(MessageType.REMATCH_RESPONSE, playerName, accepted);
//                });
//
//                dialog.setVisible(true);
//            });
//
//        } catch (ClassCastException e) {
//            sendError("Dados de oferta de revanche inválidos");
//        }
//    }
    public void resetForRematch() {
        this.teamReady = false;
        this.playerTeam = null;
        this.currentSession = null;

        // Fechar BattleEndFrame se estiver aberta
        if (battleEndFrame != null) {
            SwingUtilities.invokeLater(() -> {
                battleEndFrame.dispose();
                battleEndFrame = null;
            });
        }
    }

//    private void handleRematchResponse(Message message) {
//        // Resposta processada pelo servidor
//        System.out.println("📝 Resposta de revanche processada");
//    }
    private void handleRematchStart(Message message) {
        System.out.println("🔄 Revanche iniciada!");

        // Fechar tela de fim de batalha
        if (battleEndFrame != null) {
            SwingUtilities.invokeLater(() -> {
                battleEndFrame.dispose();
                battleEndFrame = null;
            });
        }
    }

//    private void handleRematchDeclined(Message message) {
//        String reason = message.getData() != null ? message.getData().toString()
//                : "Revanche recusada";
//
//        System.out.println("❌ Revanche recusada: " + reason);
//
//        if (battleEndFrame != null) {
//            battleEndFrame.handleRematchDeclined(reason);
//        }
//    }
    private void handleConnectRequest(Message message) {
        this.playerName = message.getSender();

        System.out.println("🤝 Solicitação de conexão de: " + playerName);

        // Verificar se nome já está em uso (simplificado)
        // Em implementação real, verificaria lista de clientes ativos
        // Aceitar conexão
        sendMessage(MessageType.CONNECT_RESPONSE, "Server", "OK");

        System.out.println("✅ Cliente conectado: " + playerName + " (" + clientId + ")");
    }

    private void handlePlayerJoin(Message message) {
        System.out.println("👋 Jogador entrou no jogo: " + playerName);

        // Notificar outros clientes sobre novo jogador
        broadcastToOthers(MessageType.PLAYER_JOIN, playerName, playerName + " entrou no jogo");

        // Iniciar seleção de time
        sendMessage(MessageType.TEAM_SELECTION_START, "Server", "Inicie a seleção do seu time");
    }

    @SuppressWarnings("unchecked")
    private void handleTeamSelection(Message message) {
        try {
            this.playerTeam = (java.util.List<pokemon.Species>) message.getData();
            this.teamReady = true;

            System.out.println("📋 Time recebido de " + playerName + ": " + playerTeam.size() + " Pokémon");

            // Procurar outro jogador para iniciar batalha
            findOpponentAndStartBattle();

        } catch (ClassCastException e) {
            sendError("Dados de time inválidos");
        }
    }

    private void handleMoveExecution(Message message) {
        if (currentSession != null) {
            try {
                int moveIndex = (Integer) message.getData();
                currentSession.executeMove(this, moveIndex);

                System.out.println("⚡ Movimento executado por " + playerName + ": " + moveIndex);

            } catch (ClassCastException e) {
                sendError("Índice de movimento inválido");
            }
        }
    }

    private void handleDisconnect(Message message) {
        System.out.println("📤 Cliente desconectando: " + playerName);

        String reason = message.getData() != null ? message.getData().toString() : "Desconexão solicitada";
        System.out.println("📝 Motivo: " + reason);

        disconnect();
    }

    private void handleHeartbeat(Message message) {
        // Responder heartbeat
        sendMessage(MessageType.HEARTBEAT, "Server", "pong");
    }

    private void findOpponentAndStartBattle() {
        ClientHandler opponent = server.findAvailableOpponent(this);
        if (opponent == null) {
            System.out.println("🔍 Nenhum oponente disponível para " + playerName);
            sendMessage(MessageType.NOTIFICATION, "Server", "Aguardando outro jogador...");
            return;
        }

        System.out.println("🎯 Emparelhando " + playerName + " vs " + opponent.getPlayerName());

        GameSession session = server.createGameSession(this, opponent);
        this.currentSession = session;
        opponent.currentSession = session;

        // Criar payloads com os times e ordem do turno
        BattleInitPayload payloadP1 = new BattleInitPayload(
                this.playerTeam, opponent.playerTeam,
                session.isCurrentPlayer(this));

        BattleInitPayload payloadP2 = new BattleInitPayload(
                opponent.playerTeam, this.playerTeam,
                session.isCurrentPlayer(opponent));

        // Enviar dados iniciais da batalha
        sendMessage(MessageType.BATTLE_INIT, "Server", payloadP1);
        opponent.sendMessage(MessageType.BATTLE_INIT, "Server", payloadP2);

        // Confirmações visuais
        sendMessage(MessageType.BATTLE_START, "Server", "Batalha iniciada contra " + opponent.getPlayerName());
        opponent.sendMessage(MessageType.BATTLE_START, "Server", "Batalha iniciada contra " + playerName);

        // Iniciar a batalha na sessão
        session.startBattle();
    }

    // Implementação de NetworkManager.ConnectionListener
    @Override
    public void onConnected() {
        System.out.println("🔗 Conexão estabelecida com cliente");
    }

    @Override
    public void onDisconnected() {
        System.out.println("📤 Cliente desconectado: " + (playerName != null ? playerName : clientId));
        // Notifique o adversário caso haja sessão e estejamos em revanche pendente
        if (currentSession != null) {
            currentSession.notifyRematchDisconnected(this);
        }
        cleanup();
    }

    @Override
    public void onError(Exception e) {
        System.err.println("❌ Erro de conexão com " + (playerName != null ? playerName : clientId) + ": " + e.getMessage());
        disconnect();
    }

    // Métodos utilitários
    public void sendMessage(MessageType type, String sender, Object data) {
        if (networkManager != null && connected.get()) {
            Message message = new Message(type, sender, data);
            networkManager.sendMessage(message);
        }
    }

    void sendError(String error) {
        sendMessage(MessageType.ERROR, "Server", error);
    }

    private void broadcastToOthers(MessageType type, String sender, Object data) {
        // Implementação simplificada - servidor deveria ter lista de todos os clientes
        System.out.println("📡 Broadcast: " + type + " de " + sender);
    }

    public void disconnect() {
        connected.set(false);

        if (networkManager != null) {
            networkManager.disconnect();
        }

        // Notificar sessão atual se existir
        if (currentSession != null) {
            currentSession.playerDisconnected(this);
        }

        cleanup();
    }

    private void cleanup() {
        // Remover cliente do servidor
        server.removeClient(this);

        // Limpar referências
        currentSession = null;
        playerTeam = null;

        System.out.println("🧹 Limpeza concluída para cliente: " + (playerName != null ? playerName : clientId));
    }

    // Getters
    public String getPlayerName() {
        return playerName;
    }

    public String getClientId() {
        return clientId;
    }

    public boolean isConnected() {
        return connected.get();
    }

    public boolean isTeamReady() {
        return teamReady;
    }

    public java.util.List<pokemon.Species> getPlayerTeam() {
        return playerTeam;
    }

    public GameSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(GameSession session) {
        this.currentSession = session;
    }
}
