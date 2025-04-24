package pokemon.showdown;

public class BattleEngine {

    public static void battle(Pokemon p1, Pokemon p2) {
        System.out.println("🔵 " + p1.getName() + " vs 🔴 " + p2.getName());
        while (!p1.isFainted() && !p2.isFainted()) {
            System.out.println("\n=== Novo Turno ===");
            // Decide a ordem com base na velocidade
            if (p1.getSpeed() >= p2.getSpeed()) {
                takeTurn(p1, p2);
                if (!p2.isFainted()) {
                    takeTurn(p2, p1);
                }
            } else {
                takeTurn(p2, p1);
                if (!p1.isFainted()) {
                    takeTurn(p1, p2);
                }
            }

            // Mostra HPs após o turno
            System.out.println(p1.getName() + " HP: " + p1.getCurrentHP());
            System.out.println(p2.getName() + " HP: " + p2.getCurrentHP());
        }

        if (p1.isFainted()) {
            System.out.println(p1.getName() + " desmaiou!");
        }
        if (p2.isFainted()) {
            System.out.println(p2.getName() + " desmaiou!");
        }

        System.out.println("🏁 Fim da batalha!");
    }

    private static void takeTurn(Pokemon attacker, Pokemon defender) {
        // Seleciona o primeiro ataque da lista (simplificado)
        Move move = attacker.getMoves().get(0);

        System.out.println(attacker.getName() + " usou " + move.getName() + "!");

        // Calcula dano simples (sem tipo ou precisão ainda)
        int damage = (attacker.getAttack() + move.getPower()) - defender.getDefense();
        if (damage < 0) damage = 1;

        defender.takeDamage(damage);
        System.out.println(defender.getName() + " recebeu " + damage + " de dano!");
    }
}

