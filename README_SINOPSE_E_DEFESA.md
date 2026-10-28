# AstroLog — Diário de Observação do Céu Noturno

## Sinopse (para o formulário Excel)
AstroLog é um diário de bordo para astrônomos amadores. O usuário cadastra seus pontos de
observação (sítio, praça, terraço...) e registra o que viu no céu (planetas, nebulosas, chuvas de
meteoros), com data, anotações e nota de qualidade. Para cada local, o app consulta uma API de
meteorologia e informa a cobertura de nuvens e a temperatura, dizendo se a noite está boa para
observar. Cada observação pode ser compartilhada com outros apps instalados (WhatsApp, e-mail...).

## Mapa dos requisitos
| Requisito | Onde está |
|---|---|
| Kotlin + Jetpack Compose | Telas.kt, MainActivity.kt |
| Room, 2 tabelas | Data.kt: `Local` e `Observacao` (relação 1:N com ForeignKey + CASCADE) |
| CRUD completo nas 2 tabelas | AstroViewModel.kt + diálogos de criar/editar/excluir e filtro em Telas.kt |
| Consumo de API | Api.kt (Retrofit -> Open-Meteo Clima e Geocodificação), preenchendo coordenadas e salvando condições do céu na observação |
| Compartilhar dados com app instalado | `compartilhar()` em Telas.kt (Intent ACTION_SEND + chooser) |
| Estado preservado e sem sobreposição | `rememberSaveable(inicial?.id)` + `verticalScroll` nos diálogos + `PaddingValues(bottom = 88.dp)` nas listas + ViewModel + Flow do Room |

## Perguntas prováveis na defesa (estude as respostas)
- Por que `Flow` nos DAOs? A lista na tela atualiza sozinha quando o banco muda.
- Por que `suspend` nos insert/update/delete? Room proíbe acesso ao banco na thread principal.
- O que o `ForeignKey ... CASCADE` faz? Ao excluir um local, suas observações são apagadas junto.
- Por que ViewModel? Sobrevive à rotação de tela e separa lógica da UI.
- O que é `stateIn`? Converte o Flow em StateFlow para a UI coletar com `collectAsState`.
- Como funciona o compartilhamento? Intent implícita ACTION_SEND; o Android lista os apps que aceitam text/plain.
- Como a API é consumida? Retrofit gera a implementação da interface; Gson converte o JSON em data class.
- Por que a permissão INTERNET? Sem ela, a chamada HTTP é bloqueada.
