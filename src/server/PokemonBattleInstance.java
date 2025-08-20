package server;

import pokemon.Species;
import pokemon.Move;
import java.util.Random;

/**
 * Representa um Pokémon em uma batalha no servidor
 * Mantém HP atual, espécie e outros dados necessários para o combate
 */
public class PokemonBattleInstance {
    private final Species species;
    private int currentHp;
    private final int maxHp;
    private boolean fainted;
    private final Random random = new Random();

    public PokemonBattleInstance(Species species) {
        this.species = species;
        this.maxHp = species.getBaseStats()[0]; // HP é o primeiro stat
        this.currentHp = maxHp; // Inicia com HP máximo
        this.fainted = false;
    }

    /**
     * Aplica dano ao Pokémon
     * @param damage Quantidade de dano a ser aplicada
     * @return Dano realmente aplicado
     */
    public int takeDamage(int damage) {
        int actualDamage = Math.min(damage, currentHp);
        currentHp -= actualDamage;
        
        if (currentHp <= 0) {
            currentHp = 0;
            fainted = true;
        }
        
        return actualDamage;
    }

    /**
     * Calcula dano de um movimento contra este Pokémon
     * @param move O movimento usado
     * @param isPhysical Se o ataque é físico (para futura implementação de stats)
     * @return Dano calculado
     */
    public int calculateDamageReceived(Move move, boolean isPhysical) {
        // Fórmula simplificada de dano
        int baseDamage = move.getPower();
        if (baseDamage <= 0) return 0; // Movimentos sem dano (status moves)
        
        // Adiciona variação aleatória (85% - 100%)
        double variation = 0.85 + (random.nextDouble() * 0.15);
        int finalDamage = (int) (baseDamage * variation);
        
        return Math.max(1, finalDamage); // Mínimo 1 de dano
    }

    /**
     * Executa um movimento escolhido aleatoriamente
     * @return O movimento escolhido
     */
    public Move getRandomMove() {
        Move[] moves = species.getMoves();
        return moves[random.nextInt(moves.length)];
    }

    /**
     * Obtém movimento por índice
     * @param index Índice do movimento (0-3)
     * @return Movimento ou null se inválido
     */
    public Move getMove(int index) {
        Move[] moves = species.getMoves();
        if (index >= 0 && index < moves.length) {
            return moves[index];
        }
        return null;
    }

    /**
     * Retorna HP como porcentagem (0-100) para enviar ao cliente
     */
    public int getHpPercentage() {
        if (maxHp == 0) return 0;
        return (currentHp * 100) / maxHp;
    }

    /**
     * Cura o Pokémon completamente (para testes)
     */
    public void fullHeal() {
        currentHp = maxHp;
        fainted = false;
    }

    // Getters
    public Species getSpecies() { return species; }
    public int getCurrentHp() { return currentHp; }
    public int getMaxHp() { return maxHp; }
    public boolean isFainted() { return fainted; }
    
    @Override
    public String toString() {
        return String.format("%s (%d/%d HP)", 
            species.getName(), currentHp, maxHp);
    }
}
