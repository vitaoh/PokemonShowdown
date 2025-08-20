package logging;

import java.util.List;

public class LogReader {
    
    /**
     * Exemplo de como ler e exibir logs de batalha
     */
    public static void readBattleLogs() {
        System.out.println("LENDO LOGS DE BATALHA...");
        
        // Listar todos os arquivos de log
        List<String> logFiles = BattleLogIntegration.findAllBattleLogs();
        
        if (logFiles.isEmpty()) {
            System.out.println("Nenhum log de batalha encontrado.");
            return;
        }
        
        System.out.println("Encontrados " + logFiles.size() + " arquivos de log:");
        
        // Ler cada arquivo de log
        for (String logFile : logFiles) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("Arquivo: " + logFile);
            
            BattleLogData data = BattleLogger.readBattleLog(logFile);
            if (data != null) {
                // Imprimir resumo
                data.printSummary();
                
                // Estatísticas adicionais
                System.out.println("\nESTATÍSTICAS:");
                System.out.println("- Movimento mais poderoso: " + 
                    (data.getHighestDamageMove() != null ? 
                     data.getHighestDamageMove().moveName + " (" + 
                     data.getHighestDamageMove().damage + " dano)" : "N/A"));
                
                System.out.println("- Movimentos do " + data.player1Name + ": " + 
                    data.countMovesFor(data.player1Name));
                System.out.println("- Movimentos do " + data.player2Name + ": " + 
                    data.countMovesFor(data.player2Name));
            }
        }
        
        // Gerar relatório consolidado
        System.out.println("\n" + "=".repeat(60));
        BattleLogIntegration.generateBattleReport();
    }
    
    public static void main(String[] args) {
        System.out.println("\n Lendo Logs Existentes");
        LogReader.readBattleLogs();
    }
}