# Sharingan (NeoForge 1.21.1)

Mod que adiciona somente o Sharingan do Naruto: 1, 2 e 3 tomoe, Mangekyou (Itachi, Sasuke, Obito, Shisui) e Mangekyou Eterno.

## Compilar no GitHub
1. Crie um repositório e envie o conteúdo desta pasta (incluindo `.github`).
2. Aba **Actions** > workflow **Build mod** (roda sozinho a cada push).
3. Baixe o `.jar` em **Artifacts > sharingan-mod** e coloque em `mods/` com NeoForge 21.1.x.

## Teclas (reconfiguráveis em Controles > Sharingan; o HUD e as mensagens mostram a tecla real)
| Padrão | Ação |
|---|---|
| J | Aba Sharingan (olho em uso, retirar, partir o olho, layout) |
| V | Ativar/desativar Sharingan (som de vidro quebrando) |
| B | Ativar/desativar Mangekyou |
| \ , Z, X, C | Habilidades 1 a 4 (a 4 é o Susanoo) |

## Chakra
Sharingan só funciona com chakra (precisa de pelo menos 10% para ligar). Desligado, recupera a barra inteira em 90 s. Apertar a tecla sem chakra não acelera nada: cada tentativa pausa a recuperação por 3 s.

| Estágio | Gasto com o Sharingan ligado | Chakra máx. | Duração com a barra cheia |
|---|---|---|---|
| 1 tomoe | 2 por segundo | 240 | 2 min |
| 2 tomoe | 1,5 por segundo | 360 | 4 min |
| 3 tomoe | 1 por segundo | 360 | 6 min |
| Mangekyou | 2 por segundo | 960 | 8 min |
| Eterno | 2 por segundo | 1200 | 10 min |

(Quem tem o Mangekyou gasta 2 por segundo mesmo com ele desligado, só no Sharingan.)
Cada habilidade do Mangekyou gasta chakra (valor no HUD) e também desgasta o olho. O Susanoo custa 100 para ligar e gasta mais enquanto dura.

### Efeitos colaterais de chakra baixo (cumulativos)
Chakra ≤ 40: fome. ≤ 30: lentidão. ≤ 20: náusea. ≤ 10: fraqueza.

### Sharingan ativo limpa efeitos ruins
Com o Sharingan ligado (qualquer tomoe) somem náusea, escuridão e cegueira. A náusea do chakra baixo (≤ 20) continua.

## Clã Uchiha e despertar
Todo jogador recebe o **Emblema do Clã Uchiha** ao entrar pela primeira vez no mundo (textura oficial). Clique direito para pertencer ao clã. **Só quem é do Clã Uchiha pode despertar o Sharingan** (30% de chance ao sobreviver a um golpe quase fatal) e **só uma vez por mundo**: quem já teve um Sharingan, mesmo retirado, só consegue outro por implante. Sem o clã: +50% de recarga e de custo de chakra, +25% de desgaste ocular.

## Progressão
- **Despertar**: sobreviver a um golpe que deixe você com 2 corações ou menos (Uchiha, uma vez).
- **1 > 2 tomoe**: 150 XP. **2 > 3 tomoe**: 250 XP. Com o Sharingan ativo: +10 por monstro morto, XP de orbes coletados e +1 por segundo.
- **Mangekyou**: com 3 tomoe, mate qualquer mob domesticado: o Mangekyou que vem é aleatório (Itachi, Sasuke, Obito ou Shisui).
- **Eterno**: com os dois olhos inteiros do Mangekyou de Itachi ou Sasuke, implante o olho do outro. O Eterno devolve a visão enquanto estiver nos seus olhos (a cegueira volta se você tirá-lo) e não acumula desgaste.

## Retirar, partir e implantar
- **Retirar o Sharingan**: o botão da aba J entrega o item na sua mão em qualquer estágio. 1/2/3 tomoe viram o item do estágio (guarda o XP). O Mangekyou e o Eterno saem junto com o 3 tomoe, num item só, e você fica sem Sharingan. Desgaste e cegueira continuam com você.
- **Partir o olho**: qualquer Sharingan (1/2/3 tomoe, Mangekyou, Eterno) pode ser partido. O olho que sai é sorteado, esquerdo ou direito, e vira um item na sua mão para dar a outro jogador. Você fica só com o outro olho: recargas +50%, desgaste -50%, **sem Susanoo** (o Susanoo exige os dois olhos), e só aquele olho aparece no seu rosto. Usar o item do olho que falta (mesmo Sharingan) une os dois de novo.
- **Implantar**: clique direito no item. O item **substitui** o Sharingan que você tiver (o antigo volta como item). Quem recebe fica com o mesmo XP que o dono tinha; sem o Clã Uchiha vêm as penalidades acima.

## Diminuir os ticks (tick rate do jogo)
Com o Sharingan ativo o jogo inteiro roda a (20 - N) ticks por segundo, igual ao `/tick rate`: 18 (1 tomoe), 17, 16, 15 (Mangekyou) e 14 (Eterno). Isso desacelera tudo, inclusive você, e vale para o servidor todo (com vários jogadores vale o maior N). Ao desligar volta a 20. O chakra é compensado, então a duração em tempo real não muda.

## Aba J e layout
A aba mostra o Sharingan em uso: com o Sharingan ligado aparece o de tomoe (e diz se o Mangekyou está desbloqueado); com o Mangekyou ligado aparece o Mangekyou; desligado, o olho fica escuro e marcado como (desativado).
O botão **Layout** abre o editor: arraste os olhos no rosto, mude o tamanho (controle ou roda do mouse sobre o rosto) e arraste os blocos do HUD (status/chakra e habilidades). Por padrão as habilidades ficam no canto inferior direito. O layout é salvo em `config/sharingan-layout.properties`.

## Rosto
Os olhos aparecem no rosto do jogador (visível para todos em terceira pessoa) enquanto o Sharingan está ativo, com o desenho do estágio atual. Com um olho só, aparece só o esquerdo ou só o direito.

## Habilidades
- **Itachi**: Tsukuyomi, Amaterasu, Karasu Bunshin.
- **Sasuke**: Chidori Corrente, Enton: Kagutsuchi, Kirin.
- **Obito**: Kamui Intangibilidade (sem ficar invisível), Kamui Teletransporte, Kamui Sucção (o ponto de sucção fica bem longe de você, então um creeper puxado não explode em você).
- **Shisui**: Kotoamatsukami, Shunshin, Olhar Revelador.
- **Eterno**: Tsukuyomi, Amaterasu, Kirin e Susanoo Perfeito.
- **Susanoo** (tecla 4): só com os dois olhos.

## Cegueira (permanente)
O desgaste ocular nunca diminui. Em média cada habilidade soma cerca de 1% (uns 100 usos até cegar). Ao chegar a 100% você fica com o efeito de cegueira para sempre e a tela preta; com o Sharingan ativo o efeito some e você só vê silhuetas escuras. O Mangekyou Eterno devolve a visão enquanto estiver nos olhos.

## Comandos de teste (OP)
`/sharingan stage <0-3>`, `/sharingan mangekyou <itachi|sasuke|obito|shisui|eterno>`, `/sharingan xp <n>`, `/sharingan chakra`, `/sharingan uchiha <true|false>`, `/sharingan blind`, `/sharingan cure`, `/sharingan reset` (volta a poder despertar).

## Texturas
Os olhos e o emblema vêm das imagens em `tools/source` (convertidas por `tools/process_images.py`).
