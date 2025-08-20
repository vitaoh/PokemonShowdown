package gui;

import client.PokemonClient;
import network.BattleStateDTO;
import pokemon.Move;
import pokemon.Species;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * BattleSwing – versão unificada.
 * Combina a GUI completa da BattleSwing local com o fluxo em rede.
 *
 * REQUISITOS DE RECURSOS:
 *  • /background/background.png
 *  • /pokemonsBack/<nome>.png   – costas do seu Pokémon
 *  • /pokemonsFront/<nome>.png  – frente do Pokémon adversário
 *
 * O fluxo de batalha é regido pelo servidor. Esta janela só:
 *  • Atualiza sprites/HP via {@link #updateBattleState(BattleStateDTO)}
 *  • Habilita escolha de golpes quando {@link #enableMoveSelection()} é chamado
 *  • Envia o índice do golpe ao servidor por {@link PokemonClient#sendMove(int)}
 */
public class BattleSwing extends JFrame {

    /* --- Dados recebidos do servidor --- */
    private final String playerName;
    private final String opponentName;
    private final List<Species> yourTeam;
    private final List<Species> opponentTeam;

    /* --- Estruturas internas --- */
    private List<PokemonInstance> team1;   // você
    private List<PokemonInstance> team2;   // oponente
    private PokemonInstance active1, active2;

    /* --- Componentes de GUI --- */
    private JLabel playerSprite, opponentSprite;
    private JLabel playerLabel, opponentLabel;
    private JProgressBar hpBarPlayer, hpBarOpponent;
    private JLabel hpTxtPlayer, hpTxtOpponent;
    private JTextArea battleLog;
    private JButton[] moveButtons = new JButton[4];
    private JLabel statusLabel;

    public BattleSwing(String playerName,
                       String opponentName,
                       List<Species> yourTeam,
                       List<Species> opponentTeam) {

        super("Batalha: " + playerName + " vs " + opponentName);
        this.playerName = playerName;
        this.opponentName = opponentName;
        this.yourTeam = yourTeam;
        this.opponentTeam = opponentTeam;

        this.team1 = createInstances(yourTeam);
        this.team2 = createInstances(opponentTeam);

        buildGUI();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Atualizado toda vez que o servidor envia um BATTLE_STATE.
     */
    public void updateBattleState(BattleStateDTO state) {
        /* HP */
        hpBarPlayer.setValue(state.hpP1);
        hpBarOpponent.setValue(state.hpP2);
        hpTxtPlayer.setText(state.hpP1 + "/100");
        hpTxtOpponent.setText(state.hpP2 + "/100");

        /* Sprites */
        active1 = team1.get(state.idxP1);
        active2 = team2.get(state.idxP2);
        refreshSprites();

        /* Log de turno */
        String turnMsg = state.p1Turn ? "Seu turno!" : "Turno do oponente!";
        log(turnMsg);
        statusLabel.setText(turnMsg);
    }

    /**
     * Chamado quando o servidor envia MOVE_REQUEST para este cliente.
     * Habilita os botões de golpe do Pokémon ativo.
     */
    public void enableMoveSelection() {
        if (active1 == null) return;
        Move[] moves = active1.getSpecies().getMoves();
        for (int i = 0; i < 4; i++) {
            if (i < moves.length) {
                moveButtons[i].setText(moves[i].getName() + " (" + moves[i].getPower() + ")");
                moveButtons[i].setEnabled(true);
                moveButtons[i].setVisible(true);
            } else {
                moveButtons[i].setEnabled(false);
                moveButtons[i].setVisible(false);
            }
        }
    }

    private void buildGUI() {
        BackgroundPanel bg = new BackgroundPanel();
        setContentPane(bg);
        bg.setLayout(new BorderLayout());

        /* Painel central com sprites sobrepostos */
        JLayeredPane layered = new JLayeredPane();
        bg.add(layered, BorderLayout.CENTER);
        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        layered.setPreferredSize(scr);

        /* Coordenadas base */
        int wSprite = 400, hSprite = 400;
        int pX = (int) (scr.width * 0.29);
        int oX = (int) (scr.width * 0.82);
        int pY = (int) (scr.height * 0.56);
        int oY = (int) (scr.height * 0.42);

        /* Sprites */
        playerSprite = new JLabel();
        opponentSprite = new JLabel();
        playerSprite.setBounds(pX - wSprite / 2, pY - hSprite / 2, wSprite, hSprite);
        opponentSprite.setBounds(oX - wSprite / 2, oY - hSprite / 2, wSprite, hSprite);
        layered.add(playerSprite, JLayeredPane.MODAL_LAYER);
        layered.add(opponentSprite, JLayeredPane.MODAL_LAYER);

        /* Informações de HP/nome */
        playerLabel = labelStyled();
        opponentLabel = labelStyled();
        hpTxtPlayer = labelStyledSmall();
        hpTxtOpponent = labelStyledSmall();
        hpBarPlayer = hpBarStyled();
        hpBarOpponent = hpBarStyled();

        JPanel pInfo = panelInfo(playerLabel, hpTxtPlayer, hpBarPlayer);
        JPanel oInfo = panelInfo(opponentLabel, hpTxtOpponent, hpBarOpponent);
        pInfo.setBounds(pX - 75, pY + hSprite / 2 + 10, 150, 60);
        oInfo.setBounds(oX - 75, oY - hSprite / 2 - 70, 150, 60);
        layered.add(pInfo, JLayeredPane.PALETTE_LAYER);
        layered.add(oInfo, JLayeredPane.PALETTE_LAYER);

        /* Log e controles */
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);

        statusLabel = new JLabel("Aguardando oponente...", SwingConstants.CENTER);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        south.add(statusLabel, BorderLayout.NORTH);

        JPanel movesPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        movesPanel.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            JButton btn = new JButton();
            btn.setVisible(false);
            btn.setEnabled(false);
            final int idx = i;
            btn.addActionListener(e -> chooseMove(idx));
            moveButtons[i] = btn;
            movesPanel.add(btn);
        }
        south.add(movesPanel, BorderLayout.CENTER);

        battleLog = new JTextArea(6, 30);
        battleLog.setEditable(false);
        battleLog.setForeground(Color.WHITE);
        battleLog.setBackground(new Color(0, 0, 0, 150));
        JScrollPane sp = new JScrollPane(battleLog);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        south.add(sp, BorderLayout.EAST);

        bg.add(south, BorderLayout.SOUTH);

        pack();
        refreshSprites();
    }

    private void chooseMove(int idx) {
        /* Desabilita imediatamente para evitar cliques duplos */
        for (JButton b : moveButtons) b.setEnabled(false);
        PokemonClient.getInstance().sendMove(idx);
    }

    private void refreshSprites() {
        if (active1 != null) {
            updateSprite(playerSprite, active1, "pokemonsBack", 400, 400);
            playerLabel.setText(active1.getSpecies().getName());
        }
        if (active2 != null) {
            updateSprite(opponentSprite, active2, "pokemonsFront", 240, 240);
            opponentLabel.setText(active2.getSpecies().getName());
        }
    }

    private void updateSprite(JLabel lbl, PokemonInstance pi, String folder, int w, int h) {
        String path = "/" + folder + "/" + pi.getSpecies().getName().toLowerCase() + ".png";
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in != null) {
                BufferedImage img = ImageIO.read(in);
                lbl.setIcon(new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
            } else {
                lbl.setIcon(null);
            }
        } catch (Exception e) {
            lbl.setIcon(null);
        }
    }

    private JLabel labelStyled() {
        JLabel l = new JLabel("", SwingConstants.CENTER);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Arial", Font.BOLD, 14));
        return l;
    }

    private JLabel labelStyledSmall() {
        JLabel l = new JLabel("", SwingConstants.CENTER);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Arial", Font.PLAIN, 12));
        return l;
    }

    private JProgressBar hpBarStyled() {
        JProgressBar b = new JProgressBar(0, 100);
        b.setStringPainted(false);
        b.setPreferredSize(new Dimension(100, 12));
        return b;
    }

    private JPanel panelInfo(JLabel name, JLabel hpTxt, JProgressBar bar) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(name);
        p.add(hpTxt);
        p.add(bar);
        return p;
    }

    private void log(String msg) {
        battleLog.append(msg + "\n");
        battleLog.setCaretPosition(battleLog.getDocument().getLength());
    }

    private List<PokemonInstance> createInstances(List<Species> list) {
        List<PokemonInstance> out = new ArrayList<>();
        for (Species s : list) out.add(new PokemonInstance(s));
        return out;
    }


    /** Painel de fundo com imagem ou gradiente. */
    private static class BackgroundPanel extends JPanel {
        private BufferedImage bg;

        BackgroundPanel() {
            try (InputStream in = getClass().getResourceAsStream("/background/background.png")) {
                if (in != null) bg = ImageIO.read(in);
            } catch (Exception ignore) {
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            } else {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, Color.DARK_GRAY, 0, getHeight(), Color.LIGHT_GRAY));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    /** Wrapper simplificado de Pokémon para controle local de sprites. */
    private static class PokemonInstance {
        private final Species species;

        PokemonInstance(Species s) { this.species = s; }

        public Species getSpecies() { return species; }
    }
}
