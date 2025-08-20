
package players;

// Interface de callback para notificar quando a seleção de time está completa
public interface TeamSelectionCallback {
    
    // Chamado quando um jogador completa a seleção do seu time
    // @param player O jogador que completou a seleção
     
    void onTeamSelectionComplete(Player player);
}