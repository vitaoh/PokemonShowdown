package logging;

import java.util.List;
import java.util.ArrayList;

/**
 * Classe para armazenar dados lidos de um arquivo de log XML
 * Usado pela BattleLogger.readBattleLog() para retornar informações estruturadas
 */
public class BattleLogData {
    
    // Informações básicas da batalha
    public String battleId;
    public String startTimestamp;
    public String endTimestamp;
    
    // Informações dos jogadores
    public String player1Name;
    public String player1Ip;
    public List<String> player1Team;
    
    public String player2Name;
    public String player2Ip;
    public List<String> player2Team;
    
    // Resultado da batalha
    public String winner;
    public int duration; // em segundos
    public int totalTurns;
    public int totalDamageP1;
    public int totalDamageP2;
    
    // Lista de movimentos da batalha
    public List<BattleLogger.BattleMove> moves;
    
    public BattleLogData() {
        this.player1Team = new ArrayList<>();
        this.player2Team = new ArrayList<>();
        this.moves = new ArrayList<>();
    }
    
    /**
     * Imprime um resumo dos dados da batalha
     */
    public void printSummary() {
        System.out.println("=== RESUMO DA BATALHA ===");
        System.out.println("ID da Batalha: " + battleId);
        System.out.println();
        
        System.out.println("JOGADORES:");
        System.out.println("  " + player1Name + " (" + player1Ip + ")");
        System.out.println("    Time: " + String.join(", ", player1Team));
        System.out.println("  " + player2Name + " (" + player2Ip + ")");
        System.out.println("    Time: " + String.join(", ", player2Team));
        System.out.println();
        
        System.out.println("RESULTADO:");
        System.out.println("  Vencedor: " + winner);
        System.out.println("  Duração: " + duration + " segundos");
        System.out.println("  Total de turnos: " + totalTurns);
        System.out.println("  Dano total " + player1Name + ": " + totalDamageP1);
        System.out.println("  Dano total " + player2Name + ": " + totalDamageP2);
        System.out.println();
        
        System.out.println("MOVIMENTOS (" + moves.size() + " total):");
        for (BattleLogger.BattleMove move : moves) {
            System.out.println("  Turno " + move.turn + ": " + move.playerName + 
                             " (" + move.pokemonName + ") usou " + move.moveName + 
                             " -> " + move.damage + " de dano");
        }
        System.out.println("=========================");
    }
    
    /**
     * Retorna estatísticas da batalha em formato texto
     */
    public String getStatistics() {
        StringBuilder stats = new StringBuilder();
        
        // Calcular médias
        double avgDamagePerTurn = totalTurns > 0 ? (double)(totalDamageP1 + totalDamageP2) / totalTurns : 0;
        double damageRatio = totalDamageP2 > 0 ? (double)totalDamageP1 / totalDamageP2 : 0;
        
        stats.append("ESTATÍSTICAS DA BATALHA\n");
        stats.append("Duração total: ").append(duration).append(" segundos\n");
        stats.append("Turnos por minuto: ").append(duration > 0 ? (totalTurns * 60.0 / duration) : 0).append("\n");
        stats.append("Dano médio por turno: ").append(String.format("%.1f", avgDamagePerTurn)).append("\n");
        stats.append("Razão de dano (P1/P2): ").append(String.format("%.2f", damageRatio)).append("\n");
        
        // Estatísticas por jogador
        stats.append("\n ").append(player1Name).append(":\n");
        stats.append("  Dano total: ").append(totalDamageP1).append("\n");
        stats.append("  Dano por turno: ").append(totalTurns > 0 ? totalDamageP1 / totalTurns : 0).append("\n");
        
        stats.append("\n ").append(player2Name).append(":\n");
        stats.append("  Dano total: ").append(totalDamageP2).append("\n");
        stats.append("  Dano por turno: ").append(totalTurns > 0 ? totalDamageP2 / totalTurns : 0).append("\n");
        
        return stats.toString();
    }
    
    /**
     * Verifica se um jogador específico venceu
     */
    public boolean didPlayerWin(String playerName) {
        return playerName.equals(winner);
    }
    
    /**
     * Retorna o oponente de um jogador específico
     */
    public String getOpponent(String playerName) {
        if (playerName.equals(player1Name)) {
            return player2Name;
        } else if (playerName.equals(player2Name)) {
            return player1Name;
        }
        return null;
    }
    
    /**
     * Conta quantos movimentos um jogador específico fez
     */
    public int countMovesFor(String playerName) {
        return (int) moves.stream()
                .filter(move -> move.playerName.equals(playerName))
                .count();
    }
    
    /**
     * Retorna o total de dano causado por um jogador específico
     */
    public int getTotalDamageFor(String playerName) {
        if (playerName.equals(player1Name)) {
            return totalDamageP1;
        } else if (playerName.equals(player2Name)) {
            return totalDamageP2;
        }
        return 0;
    }
    
    /**
     * Encontra o movimento que causou mais dano
     */
    public BattleLogger.BattleMove getHighestDamageMove() {
        return moves.stream()
                .max((m1, m2) -> Integer.compare(m1.damage, m2.damage))
                .orElse(null);
    }
    
    /**
     * Retorna lista de movimentos de um jogador específico
     */
    public List<BattleLogger.BattleMove> getMovesFor(String playerName) {
        return moves.stream()
                .filter(move -> move.playerName.equals(playerName))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}