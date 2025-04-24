package pokemon.showdown;

import java.util.List;

public class Pokemon {
    private String name;
    private int level;
    private String type1;
    private String type2;
    private int maxHP;
    private int currentHP;
    private int attack;
    private int defense;
    private int speed;
    private List<Move> moves;

    public Pokemon(String name, int level, String type1, String type2,
                   int maxHP, int attack, int defense, int speed, List<Move> moves) {
        this.name = name;
        this.level = level;
        this.type1 = type1;
        this.type2 = type2;
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.moves = moves;
    }

    public void takeDamage(int damage) {
        currentHP -= damage;
        if (currentHP < 0) currentHP = 0;
    }

    public boolean isFainted() {
        return currentHP <= 0;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public String getName() {
        return name;
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public int getSpeed() {
        return speed;
    }

    public String getType1() {
        return type1;
    }

    public String getType2() {
        return type2;
    }

    // Outros getters, setters e métodos auxiliares aqui
}
