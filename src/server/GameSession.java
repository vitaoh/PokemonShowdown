package server;

import network.MessageType;
import network.BattleStateDTO;
import pokemon.Species;
import pokemon.Move;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;
import logging.BattleLogIntegration;
import logging.BattleLogger;
import logging.LogReader;
import network.BattleEndData;

/**
 * Gerencia uma sessão de jogo entre dois jogadores VERSÃO CORRIGIDA -
 * implementa lógica real de batalha com HP e dano
 */
public class GameSession {

    private final String sessionId;
    private final ClientHandler player1;
    private final ClientHandler player2;

    // Estado da sessão
    private final AtomicBoolean active;
    private boolean battleStarted = false;
    private boolean currentPlayerTurn; // true = player1, false = player2

    // === ESTADO REAL DA BATALHA ===
    private List<PokemonBattleInstance> team1; // Time do player1
    private List<PokemonBattleInstance> team2; // Time do player2
    private int player1ActiveIndex = 0; // Índice do Pokémon ativo do player1  
    private int player2ActiveIndex = 0; // Índice do Pokémon ativo do player2
    private boolean battleEnded = false;
    
    private BattleLogger battleLogger;
    private int logTurnCounter = 0;

    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.sessionId = UUID.randomUUID().toString().substring(0, 12);
        this.player1 = player1;
        this.player2 = player2;
        this.active = new AtomicBoolean(true);
        this.currentPlayerTurn = new java.util.Random().nextBoolean();

        // Inicializar times com PokemonBattleInstance
        initializeTeams();

        this.battleLogger = BattleLogIntegration.initializeBattleLogging(player1, player2);
        
        System.out.println("🎲 Nova sessão criada: " + sessionId);
        System.out.println("👥 Jogadores: " + player1.getPlayerName() + " vs " + player2.getPlayerName());
    }

    /**
     * Inicializa os times convertendo Species para PokemonBattleInstance
     */
    private void initializeTeams() {
        team1 = new ArrayList<>();
        team2 = new ArrayList<>();

        // Converter time do player1
        for (Species species : player1.getPlayerTeam()) {
            team1.add(new PokemonBattleInstance(species));
        }

        // Converter time do player2  
        for (Species species : player2.getPlayerTeam()) {
            team2.add(new PokemonBattleInstance(species));
        }

        System.out.println("✅ Times inicializados:");
        System.out.println("   " + player1.getPlayerName() + ": " + team1.size() + " Pokémon");
        System.out.println("   " + player2.getPlayerName() + ": " + team2.size() + " Pokémon");
    }

    /**
     * Inicia a batalha
     */
    public void startBattle() {
        if (!active.get() || battleStarted) {
            return;
        }

        if (!player1.isTeamReady() || !player2.isTeamReady()) {
            System.err.println("❌ Nem todos os jogadores têm times prontos");
            return;
        }

        battleStarted = true;
        System.out.println("⚔️ Iniciando batalha na sessão: " + sessionId);
        System.out.println("🎯 Jogador inicial: " + getCurrentPlayer().getPlayerName());

        // Enviar estado inicial
        sendBattleState();

        // Solicitar primeiro movimento
        ClientHandler first = getCurrentPlayer();
        first.sendMessage(MessageType.MOVE_REQUEST, "Server", "É sua vez de jogar!");
    }

    /**
     * Executa movimento de um jogador - LÓGICA REAL DE BATALHA
     */
    public void executeMove(ClientHandler player, int moveIndex) {
        if (!active.get() || battleEnded) {
            return;
        }

        // Verificar turno
        if (getCurrentPlayer() != player) {
            player.sendMessage(MessageType.ERROR, "Server", "Não é seu turno!");
            return;
        }

        // Obter Pokémon atacante e defensor
        PokemonBattleInstance attacker, defender;
        String attackerName, defenderName;

        if (player == player1) {
            attacker = team1.get(player1ActiveIndex);
            defender = team2.get(player2ActiveIndex);
            attackerName = player1.getPlayerName();
            defenderName = player2.getPlayerName();
        } else {
            attacker = team2.get(player2ActiveIndex);
            defender = team1.get(player1ActiveIndex);
            attackerName = player2.getPlayerName();
            defenderName = player1.getPlayerName();
        }

        // Verificar se atacante não desmaiou
        if (attacker.isFainted()) {
            player.sendMessage(MessageType.ERROR, "Server", "Seu Pokémon desmaiou!");
            return;
        }

        // Obter movimento
        Move move = attacker.getMove(moveIndex);
        if (move == null) {
            player.sendMessage(MessageType.INVALID_MOVE, "Server", "Movimento inválido!");
            return;
        }

        // === EXECUTAR MOVIMENTO COM DANO REAL ===
        executeBattleMove(attacker, defender, move, attackerName, defenderName);

        // Verificar se defensor desmaiou
        if (defender.isFainted()) {
            handlePokemonFainted(defender, player == player1 ? player2 : player1);
        }

        // Alternar turno
        currentPlayerTurn = !currentPlayerTurn;

        // Enviar estado atualizado
        sendBattleState();

        // Verificar fim da batalha
        if (checkBattleEnd()) {
            endBattle();
        } else {
            // Próximo movimento
            requestMove();
        }
    }

    /**
     * Executa o movimento com cálculo real de dano
     */
    private void executeBattleMove(PokemonBattleInstance attacker, PokemonBattleInstance defender,
            Move move, String attackerName, String defenderName) {

        // Calcular dano
        int damage = defender.calculateDamageReceived(move, true);
        int actualDamage = defender.takeDamage(damage);

        // Criar mensagem de resultado
        String moveResult;
        if (actualDamage > 0) {
            moveResult = String.format("%s (%s) usou %s contra %s (%s) e causou %d de dano! "
                    + "HP restante: %d/%d",
                    attacker.getSpecies().getName(), attackerName,
                    move.getName(),
                    defender.getSpecies().getName(), defenderName,
                    actualDamage,
                    defender.getCurrentHp(), defender.getMaxHp());
        } else {
            moveResult = String.format("%s (%s) usou %s, mas não causou dano.",
                    attacker.getSpecies().getName(), attackerName,
                    move.getName());
        }
        
        logTurnCounter++;
        if (battleLogger != null) {
            ClientHandler attackerClient = attackerName.equals(player1.getPlayerName()) ? player1 : player2;
            BattleLogIntegration.logMove(
                battleLogger,
                logTurnCounter,
                attackerClient,
                attacker.getSpecies().getName(),
                move,
                actualDamage,
                defender.getCurrentHp(),
                defender.getSpecies().getName()
            );
        }

        // Enviar resultado para ambos os jogadores
        player1.sendMessage(MessageType.MOVE_RESULT, "Server", moveResult);
        player2.sendMessage(MessageType.MOVE_RESULT, "Server", moveResult);

        System.out.println("💥 " + moveResult);
    }

    /**
     * Lida com Pokémon que desmaiou
     */
    private void handlePokemonFainted(PokemonBattleInstance fainted, ClientHandler owner) {
        String faintMsg = fainted.getSpecies().getName() + " (" + owner.getPlayerName() + ") desmaiou!";

        player1.sendMessage(MessageType.POKEMON_FAINT, "Server", faintMsg);
        player2.sendMessage(MessageType.POKEMON_FAINT, "Server", faintMsg);

        System.out.println("💀 " + faintMsg);

        // Trocar para próximo Pokémon não desmaiado
        if (owner == player1) {
            player1ActiveIndex = findNextAlivePokemon(team1, player1ActiveIndex);
        } else {
            player2ActiveIndex = findNextAlivePokemon(team2, player2ActiveIndex);
        }
    }

    /**
     * Encontra próximo Pokémon vivo no time
     */
    private int findNextAlivePokemon(List<PokemonBattleInstance> team, int currentIndex) {
        for (int i = 0; i < team.size(); i++) {
            if (!team.get(i).isFainted()) {
                return i;
            }
        }
        return currentIndex; // Todos desmaiaram
    }

    /**
     * Verifica se a batalha acabou
     */
    private boolean checkBattleEnd() {
        boolean team1AllFainted = team1.stream().allMatch(PokemonBattleInstance::isFainted);
        boolean team2AllFainted = team2.stream().allMatch(PokemonBattleInstance::isFainted);

        boolean disconnected = !player1.isConnected() || !player2.isConnected();

        return team1AllFainted || team2AllFainted || disconnected;
    }

    /**
     * Envia estado atual da batalha para ambos os jogadores
     */
    private void sendBattleState() {
        if (!active.get()) {
            return;
        }

        // Obter HP percentual dos Pokémon ativos
        int hpP1 = team1.get(player1ActiveIndex).getHpPercentage();
        int hpP2 = team2.get(player2ActiveIndex).getHpPercentage();

        // Estado personalizado para cada jogador
        BattleStateDTO stateP1 = new BattleStateDTO(hpP1, hpP2, player1ActiveIndex, player2ActiveIndex, currentPlayerTurn);
        BattleStateDTO stateP2 = new BattleStateDTO(hpP2, hpP1, player2ActiveIndex, player1ActiveIndex, !currentPlayerTurn);

        player1.sendMessage(MessageType.BATTLE_STATE, "Server", stateP1);
        player2.sendMessage(MessageType.BATTLE_STATE, "Server", stateP2);

        System.out.println("📊 Estado enviado - P1: " + hpP1 + "% HP, P2: " + hpP2 + "% HP");
    }

    /**
     * Solicita movimento do jogador atual
     */
    private void requestMove() {
        if (!active.get() || battleEnded) {
            return;
        }

        ClientHandler currentPlayer = getCurrentPlayer();
        currentPlayer.sendMessage(MessageType.MOVE_REQUEST, "Server", "É sua vez de jogar!");
        System.out.println("🎯 Solicitando movimento de: " + currentPlayer.getPlayerName());
    }

    private void endBattle() {
        battleEnded = true;

        // Determinar vencedor baseado nos Pokémon vivos
        ClientHandler winner = null;
        boolean team1HasAlive = team1.stream().anyMatch(p -> !p.isFainted());
        boolean team2HasAlive = team2.stream().anyMatch(p -> !p.isFainted());

        if (team1HasAlive && !team2HasAlive) {
            winner = player1;
        } else if (!team1HasAlive && team2HasAlive) {
            winner = player2;
        } else if (!player1.isConnected()) {
            winner = player2;
        } else if (!player2.isConnected()) {
            winner = player1;
        }

        System.out.println("🏁 Batalha finalizada na sessão: " + sessionId);

        if (winner != null) {
            System.out.println("🏆 Vencedor: " + winner.getPlayerName());

            ClientHandler loser = (winner == player1) ? player2 : player1;

            // Enviar resultado para ambos com tela de fim de jogo
            winner.sendMessage(MessageType.BATTLE_END, "Server",
                    createBattleEndData(winner.getPlayerName(), loser.getPlayerName(), "Você venceu!", true));

            if (loser.isConnected()) {
                loser.sendMessage(MessageType.BATTLE_END, "Server",
                        createBattleEndData(loser.getPlayerName(), winner.getPlayerName(), "Você perdeu!", false));
            }
        } else {
            System.out.println("🤝 Batalha terminou em empate");

            BattleEndData dataP1 = createBattleEndData(player1.getPlayerName(), player2.getPlayerName(), "Empate", false);
            BattleEndData dataP2 = createBattleEndData(player2.getPlayerName(), player1.getPlayerName(), "Empate", false);

            player1.sendMessage(MessageType.BATTLE_END, "Server", dataP1);
            player2.sendMessage(MessageType.BATTLE_END, "Server", dataP2);
        }
        
        if (battleLogger != null) {
            String winnerName = (winner != null) ? winner.getPlayerName() : "Empate";
            BattleLogIntegration.finalizeBattleLog(battleLogger, winnerName);
        }

        LogReader.readBattleLogs();
        // NÃO encerrar sessão ainda - aguardar possível revanche
        //endSession(); // Comentar esta linha
    }

// Novo método para criar dados de fim de batalha
    private BattleEndData createBattleEndData(String playerName, String opponentName, String result, boolean isWinner) {
        return new BattleEndData(playerName, opponentName, result, isWinner);
    }

// Novo método para reiniciar batalha (revanche)
//    public void restartBattle() {
//        if (!active.get()) {
//            return;
//        }
//
//        // Resetar estado da batalha
//        battleEnded = false;
//        currentPlayerTurn = new java.util.Random().nextBoolean();
//
//        // Reinicializar times (curar todos os Pokémon)
//        for (PokemonBattleInstance pokemon : team1) {
//            pokemon.fullHeal();
//        }
//        for (PokemonBattleInstance pokemon : team2) {
//            pokemon.fullHeal();
//        }
//
//        // Resetar índices ativos
//        player1ActiveIndex = 0;
//        player2ActiveIndex = 0;
//
//        System.out.println("🔄 Revanche iniciada na sessão: " + sessionId);
//
//        // Notificar início da revanche
//        player1.sendMessage(MessageType.REMATCH_START, "Server", "Revanche iniciada!");
//        player2.sendMessage(MessageType.REMATCH_START, "Server", "Revanche iniciada!");
//
//        // Começar nova batalha
//        sendBattleState();
//        requestMove();
//    }
    // === MÉTODOS DE UTILIDADE ===
    public boolean isCurrentPlayer(ClientHandler player) {
        if (player == player1) {
            return currentPlayerTurn;
        } else if (player == player2) {
            return !currentPlayerTurn;
        }
        return false;
    }

    private ClientHandler getCurrentPlayer() {
        return currentPlayerTurn ? player1 : player2;
    }

    public boolean shouldEndDueToInactivity() {
        // Implementar lógica de timeout se necessário
        return false;
    }

    public void endSession() {
        active.set(false);
        battleEnded = true;

        if (player1 != null) {
            player1.setCurrentSession(null);
        }
        if (player2 != null) {
            player2.setCurrentSession(null);
        }

        System.out.println("🔚 Sessão finalizada: " + sessionId);
    }

    public void playerDisconnected(ClientHandler player) {
        System.out.println("📤 Jogador desconectado da sessão: " + player.getPlayerName());

        ClientHandler otherPlayer = (player == player1) ? player2 : player1;
        if (otherPlayer != null && otherPlayer.isConnected()) {
            otherPlayer.sendMessage(MessageType.BATTLE_END, "Server", otherPlayer.getPlayerName());
            otherPlayer.sendMessage(MessageType.NOTIFICATION, "Server",
                    "Seu oponente desconectou. Você venceu por W.O.!");
        }

        endSession();
    }
    
    public void notifyRematchDisconnected(ClientHandler disconnectedPlayer) {
    ClientHandler waitingPlayer = getOpponent(disconnectedPlayer);
    if (waitingPlayer != null && waitingPlayer.isConnected()) {
        // Envia recusa explícita de revanche para quem estava esperando
        waitingPlayer.sendMessage(network.MessageType.REMATCH_RESPONSE, "Server", false);
    }
}

    // Getters
    public String getSessionId() {
        return sessionId;
    }

    public ClientHandler getPlayer1() {
        return player1;
    }

    public ClientHandler getPlayer2() {
        return player2;
    }

    public boolean isActive() {
        return active.get();
    }

    public boolean isBattleStarted() {
        return battleStarted;
    }

    public boolean isBattleEnded() {
        return battleEnded;
    }

    public boolean hasPlayer(ClientHandler player) {
        return player == player1 || player == player2;
    }

    public ClientHandler getOpponent(ClientHandler player) {
        if (player == player1) {
            return player2;
        } else if (player == player2) {
            return player1;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("GameSession{id='%s', players=[%s vs %s], active=%s, started=%s}",
                sessionId,
                player1 != null ? player1.getPlayerName() : "null",
                player2 != null ? player2.getPlayerName() : "null",
                active.get(),
                battleStarted);
    }
}
