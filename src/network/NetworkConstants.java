package network;

/**
 * Constantes de rede para configuração do sistema cliente-servidor
 */
public class NetworkConstants {
    
    // Configurações de conexão
    public static final int DEFAULT_PORT = 12345;
    public static final String DEFAULT_HOST = "localhost";
    public static final int CONNECTION_TIMEOUT = 30000; // 30 segundos
    public static final int READ_TIMEOUT = 5000; // 5 segundos
    
    // Configurações do servidor
    public static final int MAX_CLIENTS = 50;
    public static final int SERVER_BACKLOG = 10;
    public static final long HEARTBEAT_INTERVAL = 10000; // 10 segundos
    public static final long CLIENT_TIMEOUT = 30000; // 30 segundos
    
    // Buffer sizes
    public static final int BUFFER_SIZE = 8192;
    public static final int MESSAGE_QUEUE_SIZE = 100;
    
    // Códigos de resposta
    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 400;
    public static final int TIMEOUT_CODE = 408;
    public static final int SERVER_FULL_CODE = 503;
    
    // Mensagens do sistema
    public static final String SERVER_FULL_MSG = "Servidor lotado. Tente novamente mais tarde.";
    public static final String CONNECTION_LOST_MSG = "Conexão perdida com o servidor.";
    public static final String INVALID_MESSAGE_MSG = "Mensagem inválida recebida.";
    public static final String TIMEOUT_MSG = "Timeout de operação.";
    
    // Configurações de jogo
    public static final int BATTLE_TIMEOUT = 60000; // 1 minuto por jogada
    public static final int TEAM_SELECTION_TIMEOUT = 300000; // 5 minutos para seleção de time
    
    private NetworkConstants() {
        // Previne instanciação
    }
}