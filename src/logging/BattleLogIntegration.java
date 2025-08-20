package logging;

import server.GameSession;
import server.ClientHandler;
import server.PokemonBattleInstance;
import pokemon.Species;
import pokemon.Move;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;

/**
 * Classe utilitária para integrar o sistema de logging XML com as classes existentes
 * Fornece métodos para facilitar a integração do BattleLogger com GameSession e ClientHandler
 */
public class BattleLogIntegration {
    
    /**
     * Cria um BattleLogger e configura os participantes da batalha
     * Deve ser chamado no início de uma nova GameSession
     */
    public static BattleLogger initializeBattleLogging(ClientHandler player1, ClientHandler player2) {
        BattleLogger logger = new BattleLogger();
        
        // Extrair IP dos jogadores
        String player1Ip = extractClientIp(player1);
        String player2Ip = extractClientIp(player2);
        
        // Criar informações dos jogadores
        BattleLogger.PlayerInfo p1Info = new BattleLogger.PlayerInfo(
            player1.getPlayerName(),
            player1Ip,
            player1.getPlayerTeam()
        );
        
        BattleLogger.PlayerInfo p2Info = new BattleLogger.PlayerInfo(
            player2.getPlayerName(),
            player2Ip,
            player2.getPlayerTeam()
        );
        
        // Configurar participantes no logger
        logger.setBattleParticipants(p1Info, p2Info);
        
        System.out.println("BattleLogger configurado para: " + 
                          player1.getPlayerName() + " vs " + player2.getPlayerName());
        
        return logger;
    }
    
    /**
     * Extrai o endereço IP de um ClientHandler
     */
    private static String extractClientIp(ClientHandler client) {
        try {
            // Usando reflection para acessar o socket privado
            java.lang.reflect.Field socketField = ClientHandler.class.getDeclaredField("clientSocket");
            socketField.setAccessible(true);
            Socket socket = (Socket) socketField.get(client);
            
            if (socket != null && socket.getRemoteSocketAddress() != null) {
                String fullAddress = socket.getRemoteSocketAddress().toString();
                // Remover "/127.0.0.1:12345" -> "127.0.0.1"
                if (fullAddress.startsWith("/")) {
                    fullAddress = fullAddress.substring(1);
                }
                if (fullAddress.contains(":")) {
                    return fullAddress.split(":")[0];
                }
                return fullAddress;
            }
        } catch (Exception e) {
            System.err.println("Erro ao extrair IP do cliente: " + e.getMessage());
        }
        
        return "IP_DESCONHECIDO";
    }
    
    /**
     * Registra um movimento no BattleLogger
     * Deve ser chamado toda vez que um movimento é executado em GameSession
     */
    public static void logMove(BattleLogger logger, int turnNumber, 
                              ClientHandler attacker, String pokemonName, 
                              Move move, int damage, int targetHp, String targetName) {
        
        if (logger == null) return;
        
        BattleLogger.BattleMove battleMove = new BattleLogger.BattleMove(
            turnNumber,
            attacker.getPlayerName(),
            pokemonName,
            move.getName(),
            damage,
            targetHp,
            targetName
        );
        
        logger.logBattleMove(battleMove);
    }
    
    /**
     * Finaliza o log da batalha
     * Deve ser chamado quando a batalha termina em GameSession.endBattle()
     */
    public static void finalizeBattleLog(BattleLogger logger, String winner) {
        if (logger != null) {
            logger.finalizeBattle(winner);
        }
    }
    
    /**
     * Lê e exibe estatísticas de um arquivo de log XML
     */
    public static void displayBattleStatistics(String logFilePath) {
        BattleLogData logData = BattleLogger.readBattleLog(logFilePath);
        
        if (logData != null) {
            logData.printSummary();
            System.out.println();
            System.out.println(logData.getStatistics());
        } else {
            System.err.println("Não foi possível ler o arquivo de log: " + logFilePath);
        }
    }
    
    /**
     * Busca e lista todos os arquivos de log XML existentes
     */
    public static List<String> findAllBattleLogs() {
        List<String> logFiles = new ArrayList<>();
        java.io.File logDir = new java.io.File("battle_logs");
        
        if (logDir.exists() && logDir.isDirectory()) {
            java.io.File[] files = logDir.listFiles((dir, name) -> name.endsWith(".xml"));
            if (files != null) {
                for (java.io.File file : files) {
                    logFiles.add(file.getAbsolutePath());
                }
            }
        }
        
        return logFiles;
    }
    
    /**
     * Gera um relatório consolidado de múltiplas batalhas
     */
    public static void generateBattleReport() {
        List<String> logFiles = findAllBattleLogs();
        
        if (logFiles.isEmpty()) {
            System.out.println("Nenhum arquivo de log encontrado.");
            return;
        }
        
        System.out.println("RELATÓRIO DE BATALHAS (" + logFiles.size() + " batalhas)");
        System.out.println("================================================");
        
        int totalBattles = 0;
        int totalTurns = 0;
        long totalDuration = 0;
        java.util.Map<String, Integer> playerWins = new java.util.HashMap<>();
        java.util.Map<String, Integer> pokemonUsage = new java.util.HashMap<>();
        
        for (String logFile : logFiles) {
            BattleLogData data = BattleLogger.readBattleLog(logFile);
            if (data != null) {
                totalBattles++;
                totalTurns += data.totalTurns;
                totalDuration += data.duration;
                
                // Contar vitórias
                playerWins.put(data.winner, playerWins.getOrDefault(data.winner, 0) + 1);
                
                // Contar uso de pokémon
                for (String pokemon : data.player1Team) {
                    pokemonUsage.put(pokemon, pokemonUsage.getOrDefault(pokemon, 0) + 1);
                }
                for (String pokemon : data.player2Team) {
                    pokemonUsage.put(pokemon, pokemonUsage.getOrDefault(pokemon, 0) + 1);
                }
            }
        }
        
        System.out.println("Total de batalhas: " + totalBattles);
        System.out.println("Turnos médios por batalha: " + (totalBattles > 0 ? totalTurns / totalBattles : 0));
        System.out.println("Duração média por batalha: " + (totalBattles > 0 ? totalDuration / totalBattles : 0) + " segundos");
        System.out.println();
        
        System.out.println("TOP JOGADORES:");
        playerWins.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(5)
            .forEach(entry -> System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " vitórias"));
        
        System.out.println();
        System.out.println("POKÉMON MAIS USADOS:");
        pokemonUsage.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(5)
            .forEach(entry -> System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " vezes"));
        
        System.out.println("================================================");
    }
    
    /**
     * Exemplo de como usar o sistema de logging em uma GameSession modificada
     * Este é um exemplo de código que deveria ser integrado à classe GameSession
     */
    public static class GameSessionExample {
        
        private BattleLogger battleLogger;
        private int turnCounter = 0;
        
        public void initializeBattle(ClientHandler player1, ClientHandler player2) {
            // Inicializar o logger de batalha
            battleLogger = BattleLogIntegration.initializeBattleLogging(player1, player2);
            System.out.println("Sistema de logging inicializado para a batalha");
        }
        
        public void executeMove(ClientHandler attacker, ClientHandler defender, 
                               String attackerPokemon, String defenderPokemon,
                               Move move, int damage, int remainingHp) {
            
            turnCounter++;
            
            // Registrar o movimento no log
            BattleLogIntegration.logMove(
                battleLogger,
                turnCounter,
                attacker,
                attackerPokemon,
                move,
                damage,
                remainingHp,
                defenderPokemon
            );
            
            System.out.println("Movimento registrado no log: " + 
                             attacker.getPlayerName() + " usou " + move.getName());
        }
        
        public void endBattle(String winner) {
            // Finalizar o log
            BattleLogIntegration.finalizeBattleLog(battleLogger, winner);
            
            // Opcional: exibir estatísticas
            if (battleLogger != null) {
                System.out.println("Log salvo em: " + battleLogger.getLogFilePath());
            }
        }
    }
    
    /**
     * Método main para testar o sistema de logging
     */
    public static void main(String[] args) {
        System.out.println("TESTE DO SISTEMA DE LOGGING XML");
        System.out.println("===================================");
        
        // Gerar relatório de batalhas existentes
        generateBattleReport();
        
        // Listar arquivos de log
        List<String> logs = findAllBattleLogs();
        System.out.println("\nArquivos de log encontrados:");
        for (String log : logs) {
            System.out.println("  " + log);
        }
        
        // Se houver logs, exibir o primeiro
        if (!logs.isEmpty()) {
            System.out.println("\nExemplo de leitura de log:");
            displayBattleStatistics(logs.get(0));
        }
    }
}