package pokemon;

import pokemon.Move;
import pokemon.Type;

public enum Species {
    
    KYOGRE("Kyogre", "382", new short[] {300, 100, 90, 150, 140, 90},
            new Move[] {Move.HYDRO_PUMP, Move.SHEER_COLD, Move.CALM_MIND, Move.ANCIENTPOWER}, Type.WATER),

    GROUDON("Groudon", "383", new short[] {300, 150, 140, 100, 90, 90},
        new Move[] {Move.EARTHQUAKE, Move.SOLARBEAM, Move.FISSURE, Move.LAVA_PLUME}, Type.GROUND),

    RAYQUAZA("Rayquaza", "384", new short[] {315, 150, 90, 150, 90, 95},
        new Move[] {Move.OUTRAGE, Move.DRAGON_DANCE, Move.HYPER_BEAM, Move.FLY}, Type.DRAGON, Type.FLYING),
    
    ARCEUS("Arceus", "493", new short[] {360, 120, 120, 120, 120, 120},
            new Move[] {Move.JUDGMENT, Move.FUTURE_SIGHT, Move.REFRESH, Move.PERISH_SONG}, Type.NORMAL),
    
    DARKRAI("Darkrai", "491", new short[] {210, 90, 90, 135, 90, 125},
            new Move[] {Move.DARK_VOID, Move.NASTY_PLOT, Move.DARK_PULSE, Move.HYPNOSIS}, Type.DARK),
    
    GIRATINA("Giratina", "487", new short[] {450, 100, 120, 100, 120, 90},
            new Move[] {Move.SHADOW_FORCE, Move.AURA_SPHERE, Move.DRAGON_CLAW, Move.SHADOW_CLAW}, Type.GHOST, Type.DRAGON),
    
    DIALGA("Dialga", "483", new short[] {300, 120, 120, 150, 100, 90},
            new Move[] {Move.ROAR_OF_TIME, Move.ANCIENTPOWER, Move.AURA_SPHERE, Move.METAL_BURST}, Type.STEEL, Type.DRAGON),

    PALKIA("Palkia", "484", new short[] {270, 120, 100, 150, 120, 100},
        new Move[] {Move.SPACIAL_REND, Move.AURA_SPHERE, Move.WATER_PULSE, Move.HYDRO_PUMP}, Type.WATER, Type.DRAGON),
    
    LUGIA("Lugia", "249", new short[] {318, 90, 130, 90, 154, 110},
            new Move[] {Move.AEROBLAST, Move.HYDRO_PUMP, Move.CALM_MIND, Move.SKY_ATTACK}, Type.PSYCHIC, Type.FLYING),
    
    RESHIRAM("Reshiram", "643", new short[] {300, 120, 100, 150, 120, 90},
            new Move[] {Move.FIRE_FANG, Move.FLAMETHROWER, Move.DRAGON_PULSE, Move.FUSION_FLARE, Move.OUTRAGE}, Type.DRAGON, Type.FIRE),
    
    ZEKROM("Zekrom", "644", new short[] {300, 150, 120, 120, 100, 90},
            new Move[] {Move.THUNDER_FANG, Move.THUNDERBOLT, Move.DRAGON_CLAW, Move.FUSION_BOLT, Move.OUTRAGE}, Type.DRAGON, Type.ELECTRIC),
    
    MEWTWO("Mewtwo", "150", new short[] {318, 110, 90, 154, 90, 130},
        new Move[] {Move.PSYSTRIKE, Move.AURA_SPHERE, Move.RECOVER, Move.PSYCHIC},
        Type.PSYCHIC);

    // === Campos ===
    private final String name;
    private final String natDexNumber;
    private final short[] baseStats;
    private final Move[] moves;
    private final Type[] types;

    // === Construtor ===
    Species(String name, String dexNumber, short[] baseStats, Move[] moves, Type... types) {
        if (types.length == 0 || types.length > 2)
            throw new IllegalArgumentException("Each Pokémon must have 1 or 2 types.");

        this.name = name;
        this.natDexNumber = dexNumber;
        this.baseStats = baseStats;
        this.moves = moves;
        this.types = types;
    }

    // === Getters ===
    public String getName() {
        return name;
    }

    public String getDexNumber() {
        return natDexNumber;
    }

    public short[] getBaseStats() {
        return baseStats;
    }

    public Move[] getMoves() {
        return moves;
    }

    public Type[] getTypes() {
        return types;
    }
}
