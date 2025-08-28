# Pokémon Showdown 🎮

Um jogo de batalha Pokémon desenvolvido em Java com interface gráfica Swing, oferecendo experiências de combate épicas tanto localmente quanto online!

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/Swing-GUI-blue?style=for-the-badge)

## 🌟 Características

### 🎯 Modos de Jogo
- **Modo Local**: Batalhe com um amigo no mesmo computador
- **Modo Online**: Cliente-servidor para batalhas pela rede
- **Interface Intuitiva**: Launcher com menu principal para escolher o modo de jogo

### 🐾 Sistema de Pokémon
- **12 Pokémon Lendários** incluindo:
  - Kyogre, Groudon, Rayquaza (Trio de Hoenn)
  - Dialga, Palkia, Giratina (Trio de Sinnoh) 
  - Arceus, Darkrai, Lugia
  - Reshiram, Zekrom, Mewtwo
- **Sistema de Tipos**: Água, Fogo, Elétrico, Dragão, Psíquico e mais
- **Movimentos Únicos**: Cada Pokémon possui 4 movimentos exclusivos
- **Stats Base**: HP, Ataque, Defesa, Ataque Especial, Defesa Especial, Velocidade

### 🎨 Interface Gráfica
- **Sprites Animados**: Pokémon com sprites frontais e traseiros
- **Background Personalizado**: Cenário de batalha imersivo
- **Barras de HP**: Visualização em tempo real da saúde dos Pokémon
- **Log de Batalha**: Acompanhe todas as ações do combate
- **Tela de Vitória**: Celebre suas conquistas!

### 🌐 Sistema de Rede
- **Servidor Dedicado**: Hospede batalhas para outros jogadores
- **Cliente Multiplayer**: Conecte-se a servidores remotos
- **Sincronização em Tempo Real**: Movimentos e estados sincronizados
- **Interface de Conexão**: Fácil configuração de IP e porta

## 🚀 Como Executar

### Pré-requisitos
- **Java JDK 8+** instalado
- **IDE Java** (NetBeans recomendado) ou compilador Java

### Executando o Jogo

#### Opção 1: Usando NetBeans
1. Clone o repositório:
```bash
git clone https://github.com/vitaoh/PokemonShowdown.git
```
2. Abra o projeto no NetBeans
3. Execute a classe `GameLauncher.java` em `src/launcher/`

#### Opção 2: Linha de Comando
1. Compile o projeto:
```bash
javac -d build src/**/*.java
```
2. Execute o launcher:
```bash
java -cp build launcher.GameLauncher
```

#### Opção 3: Modo Clássico (Local Direto)
- Execute a classe `Main.java` em `src/battle/` para iniciar diretamente uma batalha local

## 🎮 Como Jogar

### 1. **Seleção de Time**
- Escolha 3 Pokémon lendários para sua equipe
- Cada jogador seleciona seu time antes da batalha começar

### 2. **Sistema de Batalha**
- **Turnos Alternados**: Os jogadores se revezam fazendo movimentos
- **4 Movimentos**: Cada Pokémon possui 4 ataques únicos
- **Sistema de HP**: Quando o HP chega a zero, o Pokémon desmaia
- **Vitória**: Vença fazendo todos os Pokémon adversários desmaiarem

### 3. **Controles**
- **Clique nos botões** de movimento para atacar
- **Log de batalha** mostra todas as ações
- **Barras de HP** indicam a saúde atual dos Pokémon

## 📁 Estrutura do Projeto

```
src/
├── launcher/           # Ponto de entrada do jogo
│   └── GameLauncher.java
├── battle/            # Sistema de combate
│   ├── BattleSwing.java
│   └── Main.java
├── pokemon/           # Classes dos Pokémon
│   ├── Species.java
│   ├── Move.java
│   ├── Type.java
│   └── ...
├── players/           # Sistema de jogadores
│   ├── Player.java
│   └── TeamSelectionFrame.java
├── server/            # Servidor multiplayer
│   ├── PokemonServer.java
│   └── ClientHandler.java
├── client/            # Cliente de rede
├── network/           # Comunicação de rede
├── gui/              # Interfaces gráficas
├── background/       # Assets de fundo
├── pokemonsFront/    # Sprites frontais
└── pokemonsBack/     # Sprites traseiros
```

## 🔧 Recursos Técnicos

### **Arquitetura**
- **Padrão MVC**: Separação clara entre modelo, visão e controle
- **Swing GUI**: Interface gráfica nativa Java
- **Threading**: Uso de timers para animações suaves
- **Sockets**: Comunicação cliente-servidor robusta

### **Design Patterns**
- **Enum Pattern**: Para Pokémon, movimentos e tipos
- **Observer Pattern**: Callbacks para seleção de time
- **Strategy Pattern**: Sistema de movimentos
- **Singleton**: Gerenciamento de recursos de rede

## 🎯 Funcionalidades Avançadas

- **Launcher Inteligente**: Menu principal com 3 opções de jogo
- **Sprites Dinâmicos**: Carregamento automático de imagens
- **Sistema de HP Visual**: Barras de progresso responsivas  
- **Log Detalhado**: Registro completo de todas as ações
- **Tela de Vitória**: Interface especial para comemorar
- **Suporte a Rede**: Batalhas online completas
- **Interface Servidor**: Monitor de conexões em tempo real

## 🤝 Contribuindo

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

## 📝 Licença

Este projeto é open source e está disponível sob a licença MIT.

## 🎮 Capturas de Tela

### Launcher Principal
O menu inicial permite escolher entre jogo local, servidor ou cliente.

### Seleção de Time  
Interface intuitiva para escolher seus 3 Pokémon lendários.

### Batalha em Ação
Sprites animados, barras de HP e sistema de movimentos completo.

---

**Divirta-se treinando e batalhe para se tornar o mestre Pokémon! 🏆**

*Desenvolvido com ❤️ em Java*
