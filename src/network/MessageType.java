package network;

/**
 * Enum que define todos os tipos de mensagens possíveis na comunicação
 * cliente-servidor Cada tipo representa uma ação específica no protocolo do
 * jogo
 */
public enum MessageType {
    // Mensagens de Conexão
    CONNECT_REQUEST, // Cliente solicita conexão ao servidor
    CONNECT_RESPONSE, // Servidor responde à solicitação de conexão
    DISCONNECT, // Notificação de desconexão
    HEARTBEAT, // Mensagem para manter conexão viva

    REMATCH_BOTH_REQUESTED, // Ambos jogadores solicitaram revanche
    TEAM_SELECTION_RESTART, // Reiniciar seleção de time para revanche
    REMATCH_REQUEST, // Cliente solicita revanche
    REMATCH_OFFER, // Servidor envia oferta para oponente  
    REMATCH_RESPONSE, // Oponente responde à oferta
    REMATCH_START, // Revanche aceita, iniciar nova batalha
    REMATCH_DECLINED, // Revanche foi recusada

    // Mensagens de Autenticação
    PLAYER_JOIN, // Jogador entra no servidor
    PLAYER_LEAVE, // Jogador sai do servidor
    PLAYER_LIST, // Lista de jogadores conectados

    // Mensagens de Seleção de Time
    TEAM_SELECTION_START, // Inicia seleção de time
    TEAM_SELECTION_UPDATE, // Atualização durante seleção
    TEAM_SELECTION_COMPLETE,// Seleção de time completada
    TEAM_READY, // Time pronto para batalha

    // Mensagens de Batalha
    BATTLE_REQUEST, // Solicitação de batalha
    BATTLE_INIT,
    BATTLE_START, // Início de batalha
    BATTLE_STATE, // Estado atual da batalha
    BATTLE_UPDATE, // Atualização de batalha
    BATTLE_END, // Fim de batalha

    // Mensagens de Movimentos
    MOVE_REQUEST, // Solicitação de movimento
    MOVE_EXECUTE, // Execução de movimento
    MOVE_RESULT, // Resultado do movimento

    // Mensagens de Game State
    GAME_STATE_UPDATE, // Atualização do estado do jogo
    POKEMON_FAINT, // Pokémon desmaiou
    POKEMON_SWITCH, // Troca de Pokémon
    HP_UPDATE, // Atualização de HP

    // Mensagens de Erro
    ERROR, // Mensagem de erro genérico
    INVALID_MOVE, // Movimento inválido
    TIMEOUT, // Timeout de operação

    // Mensagens de Sistema
    SERVER_STATUS, // Status do servidor
    GAME_LOG, // Log do jogo
    NOTIFICATION            // Notificação geral
}
