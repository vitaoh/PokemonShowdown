package client;

import gui.BattleEndFrame;
import network.BattleInitPayload;
import network.BattleStateDTO;
import network.Message;
import network.MessageType;
import network.NetworkConstants;
import network.NetworkManager;
import players.Player;
import players.TeamSelectionFrame;
import gui.BattleSwing;
import pokemon.Species;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import network.BattleEndData;
import network.RematchRequest;

/**
 * Cliente Pokémon: gerencia conexão, seleção de time e a batalha. Versão
 * ajustada para funcionar com a BattleSwing unificada.
 */
public class PokemonClient implements NetworkManager.MessageListener, NetworkManager.ConnectionListener {

    private String playerName;
    private String serverHost;
    private int serverPort;
    private boolean connected = false;
    private Socket socket;
    private NetworkManager networkManager;
    private JFrame battleFrame;
    private BattleEndFrame currentBattleEndFrame;
    // GUI e modelo de jogador local
    private Player localPlayer;
    private TeamSelectionFrame selectionFrame;
    private BattleSwing battleSwing;
    private JDialog waitingDialog;

    // Instância singleton para acesso global
    private static PokemonClient instance;

    public static PokemonClient getInstance() {
        return instance;
    }

    public PokemonClient(String playerName, String host, int port) {
        this.playerName = playerName;
        this.serverHost = host;
        this.serverPort = port;
        instance = this; // Armazena referência global
    }

    /**
     * Envia comando de movimento ao servidor
     */
    public void sendMove(int moveIndex) {
        if (!connected || networkManager == null) {
            System.err.println("Não conectado ao servidor - movimento ignorado");
            return;
        }

        System.out.println("Enviando movimento: " + moveIndex);
        networkManager.sendMessage(
                new Message(MessageType.MOVE_EXECUTE, playerName, moveIndex)
        );
    }

    /**
     * Verifica se está conectado ao servidor
     */
    public boolean isConnected() {
        return connected && networkManager != null && networkManager.isConnected();
    }

    private void fecharJanelaBatalha() {
        if (battleFrame != null) {
            battleFrame.dispose();
        }
    }

    /**
     * Conecta ao servidor e inicia comunicação.
     */
    public boolean connect() {
        try {
            System.out.println("Tentando conectar a " + serverHost + ":" + serverPort);
            socket = new Socket(serverHost, serverPort);
            networkManager = new NetworkManager(socket);
            networkManager.setMessageListener(this);
            networkManager.setConnectionListener(this);

            if (!networkManager.initialize()) {
                System.err.println("Falha ao inicializar NetworkManager");
                return false;
            }

            networkManager.start();

            // Envia pedido de conexão
            networkManager.sendMessage(new Message(MessageType.CONNECT_REQUEST, playerName, null));
            System.out.println("Solicitação de conexão enviada");
            return true;

        } catch (IOException e) {
            System.err.println("Erro de conexão: " + e.getMessage());
            SwingUtilities.invokeLater(()
                    -> JOptionPane.showMessageDialog(null,
                            "Não foi possível conectar ao servidor:\n" + e.getMessage(),
                            "Erro de Conexão", JOptionPane.ERROR_MESSAGE)
            );
            return false;
        }
    }

    /**
     * Desconecta do servidor e fecha recursos.
     */
    public void disconnect() {
        System.out.println("Desconectando do servidor...");

        if (networkManager != null) {
            networkManager.sendMessage(new Message(MessageType.DISCONNECT, playerName, null));
            networkManager.disconnect();
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignore) {
        }

        connected = false;
        System.out.println("Desconectado do servidor");
    }

    private void handleTeamSelectionRestart(Message msg) {
        System.out.println("Reiniciando seleção de time para revanche");

        SwingUtilities.invokeLater(() -> {
            // Fechar tela de batalha se estiver aberta
            if (battleSwing != null) {
                battleSwing.dispose();
                battleSwing = null;
            }

            // Criar novo Player e nova seleção
            localPlayer = new Player(playerName);
            selectionFrame = new TeamSelectionFrame(localPlayer);
            selectionFrame.addSelectionListener(team -> sendTeamSelection(team));
            selectionFrame.setVisible(true);

            // Mostrar mensagem de revanche
            JOptionPane.showMessageDialog(selectionFrame,
                    "🔄 REVANCHE INICIADA! 🔄\n\n"
                    + "Ambos jogadores solicitaram revanche.\n"
                    + "Escolha seus pokémon novamente!",
                    "Revanche - Nova Seleção",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    @Override
    public void onMessageReceived(Message message) {
        System.out.println("📨 Mensagem recebida: " + message.getType() + " de " + message.getSender());

        switch (message.getType()) {
            case TEAM_SELECTION_RESTART:
                handleTeamSelectionRestart(message);
                break;
            case CONNECT_RESPONSE:
                handleConnectResponse(message);
                break;
            case PLAYER_JOIN:
                handlePlayerJoin(message);
                break;
            case BATTLE_INIT:
                handleBattleInit(message);
                break;
            case BATTLE_START:
                handleBattleStart(message);
                break;
            case BATTLE_STATE:
                handleBattleState(message);
                break;
            case MOVE_REQUEST:
                handleMoveRequest(message);
                break;
            case BATTLE_END:
                handleBattleEnd(message);
                break;
            case ERROR:
                handleError(message);
                break;
            case REMATCH_RESPONSE:
                handleRematchResponse(message);
                break;
            case HEARTBEAT:
                if (networkManager != null) {
                    networkManager.sendMessage(new Message(MessageType.HEARTBEAT, playerName, "pong"));
                }
                break;
            default:
                System.out.println("Mensagem ignorada: " + message.getType());
        }
    }

    @Override
    public void onConnected() {
        System.out.println("Conexão estabelecida com o servidor");
    }

    @Override
    public void onDisconnected() {
        connected = false;
        System.out.println("Conexão perdida com o servidor");
        SwingUtilities.invokeLater(()
                -> JOptionPane.showMessageDialog(null,
                        NetworkConstants.CONNECTION_LOST_MSG,
                        "Conexão Perdida", JOptionPane.ERROR_MESSAGE)
        );
    }

    @Override
    public void onError(Exception e) {
        System.err.println("Erro de rede: " + e.getMessage());
        SwingUtilities.invokeLater(()
                -> JOptionPane.showMessageDialog(null,
                        "Erro na comunicação: " + e.getMessage(),
                        "Erro de Rede", JOptionPane.ERROR_MESSAGE)
        );
    }

    private void handleConnectResponse(Message msg) {
        Object data = msg.getData();
        if ("OK".equals(data)) {
            connected = true;
            System.out.println("Conectado ao servidor com sucesso!");

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "Conectado com sucesso!", "Status", JOptionPane.INFORMATION_MESSAGE);

                // Abre seleção de time
                localPlayer = new Player(playerName);
                selectionFrame = new TeamSelectionFrame(localPlayer);
                selectionFrame.addSelectionListener(team -> sendTeamSelection(team));
                selectionFrame.setVisible(true);
            });

            // Informa servidor sobre entrada no jogo
            networkManager.sendMessage(new Message(MessageType.PLAYER_JOIN, playerName, null));

        } else {
            System.err.println("Conexão rejeitada: " + data);
            SwingUtilities.invokeLater(()
                    -> JOptionPane.showMessageDialog(null,
                            "Conexão rejeitada: " + data,
                            "Erro de Conexão", JOptionPane.ERROR_MESSAGE)
            );
            disconnect();
        }
    }

    private void handlePlayerJoin(Message msg) {
        System.out.println("Jogador entrou: " + msg.getSender());
    }

    /**
     * Envia o time selecionado ao servidor e mostra diálogo de espera.
     */
    private void sendTeamSelection(List<Species> team) {
        if (!connected) {
            System.err.println("Não conectado - time não enviado");
            return;
        }

        System.out.println("Enviando time de " + team.size() + " Pokémon");
        networkManager.sendMessage(
                new Message(MessageType.TEAM_SELECTION_COMPLETE, playerName, team)
        );

        SwingUtilities.invokeLater(() -> {
            if (selectionFrame != null) {
                selectionFrame.setVisible(false);
            }

            JOptionPane pane = new JOptionPane(
                    "Time enviado! Aguardando oponente...",
                    JOptionPane.INFORMATION_MESSAGE
            );
            waitingDialog = pane.createDialog("Aguardando");
            waitingDialog.setModal(false);
            waitingDialog.setVisible(true);
        });
    }

    /**
     * Inicializa a batalha com dados recebidos do servidor
     */
    private void handleBattleInit(Message msg) {
        BattleInitPayload init = (BattleInitPayload) msg.getData();
        String opponent = msg.getSender();

        System.out.println("Inicializando batalha contra " + opponent);
        System.out.println("Seus Pokémon: " + init.teamA.size());
        System.out.println("Pokémon do oponente: " + init.teamB.size());

        SwingUtilities.invokeLater(() -> {
            // Fecha diálogo de espera
            if (waitingDialog != null) {
                waitingDialog.dispose();
                waitingDialog = null;
            }

            // Cria a janela de batalha unificada
            battleSwing = new BattleSwing(
                    playerName,
                    opponent,
                    init.teamA, // seu time
                    init.teamB // time do oponente
            );

            battleSwing.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            System.out.println("Janela de batalha criada");
        });
    }

    private void handleBattleStart(Message msg) {
        System.out.println("Batalha iniciada!");

        SwingUtilities.invokeLater(() -> {
            if (battleSwing != null) {
                battleSwing.setVisible(true);
                battleSwing.toFront();
                battleSwing.requestFocus();

                JOptionPane.showMessageDialog(
                        battleSwing,
                        "Batalha iniciada!",
                        "Batalha",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
    }

    /**
     * Atualiza estado da batalha recebido do servidor
     */
    private void handleBattleState(Message msg) {
        BattleStateDTO state = (BattleStateDTO) msg.getData();
        System.out.println("Estado da batalha atualizado - HP: " + state.hpP1 + "/" + state.hpP2);

        SwingUtilities.invokeLater(() -> {
            if (battleSwing != null) {
                battleSwing.updateBattleState(state);
            }
        });
    }

    /**
     * Servidor solicita escolha de movimento
     */
    private void handleMoveRequest(Message msg) {
        System.out.println("Solicitação de movimento recebida");

        SwingUtilities.invokeLater(() -> {
            if (battleSwing != null) {
                battleSwing.enableMoveSelection();
            }
        });
    }

    private void handleRematchResponse(Message msg) {
        boolean accepted = Boolean.TRUE.equals(msg.getData());

        if (!accepted) {
            System.out.println("Revanche recusada pelo oponente");

            SwingUtilities.invokeLater(() -> {
                if (currentBattleEndFrame != null) {
                    currentBattleEndFrame.handleRematchDeclined("O outro jogador recusou a revanche.");
                } else {
                    JOptionPane.showMessageDialog(null,
                            "O outro jogador recusou a revanche.",
                            "Revanche Recusada",
                            JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0); // Encerrar caso não tenha referência
                }
            });
        }
    }

    private void handleBattleEnd(Message msg) {
        BattleEndData data = (BattleEndData) msg.getData();
        String result = data.getResult();
        boolean isWinner = data.isWinner();

        System.out.println("Batalha finalizada - " + result);

        SwingUtilities.invokeLater(() -> {
            // Fechar janela de batalha
            if (battleSwing != null) {
                battleSwing.dispose();
                battleSwing = null;
            }

            // Criar e armazenar a janela de fim de batalha
            currentBattleEndFrame = new BattleEndFrame(
                    data.getPlayerName(),
                    data.getOpponentName(),
                    result,
                    isWinner
            );

            // 🔧 Esta parte é o callback que envia a resposta, incluindo a recusa
            currentBattleEndFrame.setRematchRequestCallback(wantsRematch -> {
                if (wantsRematch) {
                    // Enviar solicitação de revanche
                    RematchRequest request = new RematchRequest(
                            data.getPlayerName(),
                            data.getOpponentName()
                    );
                    networkManager.sendMessage(
                            new Message(MessageType.REMATCH_REQUEST, playerName, request)
                    );

                    // Fechar janela após pedir revanche
                    currentBattleEndFrame.dispose();
                } else {
                    // Enviar recusa de revanche (notificar adversário)
                    networkManager.sendMessage(
                            new Message(MessageType.REMATCH_RESPONSE, playerName, false)
                    );

                    System.out.println("Jogador recusou revanche - notificação enviada");
                    // Pode fechar também, se quiser
                    System.exit(0);
                }
            });

            currentBattleEndFrame.setVisible(true);
        });
    }

    private void handleError(Message msg) {
        String err = msg.getData() == null ? "Erro desconhecido" : msg.getData().toString();
        System.err.println("Erro do servidor: " + err);

        SwingUtilities.invokeLater(()
                -> JOptionPane.showMessageDialog(null, err, "Erro do Servidor", JOptionPane.ERROR_MESSAGE)
        );
    }

    public static void main(String[] args) {
        String name = args.length > 0 ? args[0] : "Player";
        String host = args.length > 1 ? args[1] : NetworkConstants.DEFAULT_HOST;
        int port = args.length > 2
                ? Integer.parseInt(args[2]) : NetworkConstants.DEFAULT_PORT;

        System.out.println("🎮 Iniciando cliente Pokémon...");
        System.out.println("👤 Jogador: " + name);
        System.out.println("🌐 Servidor: " + host + ":" + port);

        PokemonClient client = new PokemonClient(name, host, port);

        if (client.connect()) {
            System.out.println("Cliente iniciado com sucesso");

            // Hook para desconexão graciosa
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n Encerrando cliente...");
                client.disconnect();
            }));
        } else {
            System.err.println("Falha ao iniciar cliente");
            System.exit(1);
        }
    }
}
