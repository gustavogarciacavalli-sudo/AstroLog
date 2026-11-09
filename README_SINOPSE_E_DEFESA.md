# AstroLog — Diário de Observação do Céu Noturno (Guia da Equipe para Defesa A1)

## 1. Sinopse do Projeto (Formulário Excel)
> **AstroLog** é um diário de bordo para astrônomos amadores. O usuário cadastra seus pontos de observação (sítio, praça, terraço...) e registra o que viu no céu (planetas, nebulosas, chuvas de meteoros), com data, anotações e nota de qualidade. Para cada local, o app consulta uma API de meteorologia e informa a cobertura de nuvens e a temperatura, dizendo se a noite está boa para observar. Cada observação pode ser compartilhada com outros apps instalados (WhatsApp, e-mail...).

---

## 2. Checklist de Compatibilidade com o Edital (5,0 pontos)

| Requisito / Critério do PDF | Pontos | Como foi implementado no AstroLog | Arquivo / Linha |
|---|---|---|---|
| **Android em Kotlin + Jetpack Compose** | Obrigatório | 100% da interface construída em Compose com Material 3 (`darkColorScheme`), `Scaffold`, `NavigationBar`, `LazyColumn` e `AlertDialog`. | `MainActivity.kt` e `Telas.kt` |
| **Utilização do Room (2 tabelas)** | **1,00 pt** | Banco `AppDatabase` com 2 entidades: `Local` (`locais`) e `Observacao` (`observacoes`), relacionadas em **1:N** com `ForeignKey` e `onDelete = ForeignKey.CASCADE`. | `Data.kt` |
| **CRUD Completo nas 2 tabelas** | **1,50 pt** | `@Insert`, `@Query` (`Flow`), `@Update` e `@Delete` tanto em `LocalDao` quanto em `ObservacaoDao`, todos acionáveis pela interface gráfica. | `Data.kt`, `AstroViewModel.kt` e `Telas.kt` |
| **Integração com API** | **1,00 pt** | Consumo da API pública **Open-Meteo** com **Retrofit + Gson** em 2 frentes: (1) busca de coordenadas por nome da cidade e (2) consulta de nuvens (%) e temperatura (°C) para avaliar o céu e salvar junto com a observação. | `Api.kt` e `AstroViewModel.kt` |
| **Compartilhamento de Dados** | **0,50 pt** | Função `compartilhar()` usa `Intent(Intent.ACTION_SEND)` (`text/plain`) + `Intent.createChooser` para enviar a observação formatada para WhatsApp, Gmail, mensagens, etc. | `Telas.kt` (função `compartilhar`) |
| **Interface e Experiência do Usuário** | **1,00 pt** | Sem perda de estado ao girar a tela (`rememberSaveable` + `ViewModel`), sem sobreposição de botões (`PaddingValues(bottom = 88.dp)` para o FAB e `verticalScroll` nos diálogos), validação de campos e confirmação antes de excluir. | `Telas.kt` |

---

## 3. Explicação dos 5 Arquivos (Divisão sugerida para a equipe)

O código foi mantido enxuto em apenas **5 arquivos `.kt`** dentro de `app/src/main/java/br/edu/up/astrolog/`:

1. **`MainActivity.kt` (18 linhas)**:
   - É a porta de entrada do app. No `onCreate`, chama `setContent` do Jetpack Compose aplicando o tema escuro (`darkColorScheme()`) e carrega o componente principal `AstroApp()`.

2. **`Data.kt` (Entidades, DAOs e Room Database)**:
   - **`Local` (`@Entity`)**: Tabela `locais` com `id` (chave primária autogerada), `nome`, `latitude` e `longitude`.
   - **`Observacao` (`@Entity`)**: Tabela `observacoes` com `id`, `localId` (chave estrangeira apontando para `Local`), `objeto`, `data`, `notas`, `nota` (1 a 5) e `condicaoCeu`.
   - **`ForeignKey.CASCADE`**: Se um local for excluído, o próprio SQLite/Room apaga automaticamente todas as observações vinculadas a ele.
   - **`LocalDao` e `ObservacaoDao` (`@Dao`)**: Interfaces que definem o CRUD (`@Query`, `@Insert`, `@Update`, `@Delete`).
   - **`AppDatabase` (`@Database`)**: Classe abstrata que cria o banco `astrolog.db` usando o padrão **Singleton** (`@Volatile` + `synchronized`) para nunca abrir duas conexões ao mesmo tempo.

3. **`Api.kt` (Consumo de API REST com Retrofit)**:
   - Define as *data classes* que espelham o JSON retornado pela API **Open-Meteo** (`ClimaResposta`, `ClimaAtual`, `GeoResposta`, `GeoLocal`).
   - Define as interfaces `ClimaApi` (`@GET("v1/forecast")`) e `GeoApi` (`@GET("v1/search")`).
   - O `RetrofitClient` (`object`) instancia o Retrofit com `GsonConverterFactory` para converter o JSON automaticamente em objetos Kotlin.

4. **`AstroViewModel.kt` (Gerenciamento de Estado e Regras de Negócio)**:
   - Estende `AndroidViewModel` para ter acesso ao contexto da aplicação e inicializar o banco Room.
   - Converte os `Flow` dos DAOs em `StateFlow` usando `.stateIn(...)` para que a UI atualize sozinha sempre que um item for criado, editado ou excluído.
   - No `salvarLocal` e `salvarObservacao`, verifica `if (id == 0L)`: se for `0`, chama `inserir()` (**Create**); se já tiver ID, chama `atualizar()` (**Update**).
   - Faz as chamadas de API dentro de `viewModelScope.launch` com `try/catch` para não travar a tela nem fechar o app se estiver sem internet.

5. **`Telas.kt` (Interface Jetpack Compose + Compartilhamento)**:
   - **`AstroApp`**: Possui a barra superior (`CenterAlignedTopAppBar`) e a barra inferior (`NavigationBar`) para alternar entre as abas **Locais** e **Observações**.
   - **`compartilhar()`**: Cria uma `Intent(Intent.ACTION_SEND)` do tipo `text/plain` com os dados da observação e abre a gaveta de compartilhamento nativa do Android (`Intent.createChooser`).
   - **`LocaisTela` e `LocalDialog`**: Lista os locais em `LazyColumn`, permite consultar a API de clima no card, abrir diálogo para criar/editar (com busca automática de latitude/longitude pela API) e excluir com confirmação.
   - **`ObservacoesTela` e `ObservacaoDialog`**: Lista as observações com filtro por local (`FilterChip`), permite criar/editar (buscando o clima atual da API para salvar no registro), excluir e compartilhar.

---

## 4. Perguntas e Respostas para o FER (Avaliação Individual)

Na defesa, a nota individual é multiplicada pelo **FER (1.0, 0.5 ou 0.25)** conforme as respostas às perguntas do professor. Estudem estes pontos:

1. **Onde estão as duas tabelas do Room e como elas se relacionam?**
   - *Resposta:* Estão no arquivo `Data.kt`: as classes `Local` (tabela `locais`) e `Observacao` (tabela `observacoes`), anotadas com `@Entity`. Elas têm um relacionamento de **1 para N** (um local pode ter várias observações), configurado na anotação `ForeignKey` da classe `Observacao`, ligando `localId` ao `id` de `Local` com `onDelete = ForeignKey.CASCADE`.

2. **Onde está implementado o CRUD completo nas duas tabelas?**
   - *Resposta:* No `Data.kt`, dentro de `LocalDao` e `ObservacaoDao`:
     - **Create:** `@Insert suspend fun inserir(...)`
     - **Read:** `@Query("SELECT ...") fun todos(): Flow<List<...>>` (além do filtro por local na tela de observações)
     - **Update:** `@Update suspend fun atualizar(...)`
     - **Delete:** `@Delete suspend fun excluir(...)`
     No `AstroViewModel.kt`, as funções `salvarLocal` e `salvarObservacao` decidem entre inserir ou atualizar olhando se o `id == 0L`.

3. **Por que os métodos `@Insert`, `@Update` e `@Delete` têm a palavra `suspend`, e o `@Query` retorna `Flow`?**
   - *Resposta:* O Room proíbe operações de banco de dados na *Main Thread* (thread principal) para não congelar a interface do usuário. Funções `suspend` rodam dentro de *Coroutines* (`viewModelScope.launch`) em segundo plano. Já o `Flow` na consulta é um fluxo reativo: toda vez que um dado muda no SQLite, o Room emite a nova lista automaticamente e o Compose redesenha a tela sozinho.

4. **Como o app consome a API e como os dados dela são usados de forma útil?**
   - *Resposta:* Usamos o **Retrofit** com **Gson** no arquivo `Api.kt` para acessar a API pública **Open-Meteo**. Usamos os dados de duas maneiras práticas:
     1. Ao cadastrar um local, o usuário pode digitar só o nome da cidade (ex: "Curitiba") e clicar em buscar; a API de Geocodificação devolve a latitude e a longitude automaticamente.
     2. Com a latitude e longitude, consultamos a API de previsão do tempo que retorna a porcentagem de nuvens (`cloud_cover`) e a temperatura (`temperature_2m`). O app interpreta se o céu está bom (`< 30%` de nuvens = ótimo) e permite salvar essa condição climática dentro do registro da observação no banco.

5. **Por que foi necessário colocar permissão no `AndroidManifest.xml`?**
   - *Resposta:* Adicionamos `<uses-permission android:name="android.permission.INTERNET" />` porque o Android bloqueia qualquer conexão de rede (HTTP/HTTPS) se o aplicativo não declarar explicitamente essa permissão no Manifesto.

6. **Como funciona o compartilhamento de dados com outros aplicativos do celular?**
   - *Resposta:* Na função `compartilhar()` em `Telas.kt`, criamos uma **Intent implícita** com a ação `Intent.ACTION_SEND` e tipo MIME `"text/plain"`. Colocamos o texto formatado da observação no `Intent.EXTRA_TEXT` e chamamos `Intent.createChooser(...)`. O próprio Android exibe a lista de apps compatíveis instalados no aparelho (WhatsApp, Gmail, Bloco de Notas, Mensagens) para o usuário escolher.

7. **Como vocês garantiram que a interface não perde estado ao girar a tela e não tem sobreposição?**
   - *Resposta:*
     - **Sem perda de estado:** Os dados do banco ficam no `AstroViewModel` (que sobrevive à rotação de tela) e os estados de tela/formulário (aba ativa, ID em edição, campos de texto dos diálogos) usam `rememberSaveable` em vez de apenas `remember`.
     - **Sem sobreposição:** Nas listas (`LazyColumn`), colocamos `contentPadding = PaddingValues(bottom = 88.dp)` para que o botão flutuante (`FloatingActionButton` `+`) nunca cubra os botões de editar/excluir do último card. E nos diálogos de cadastro usamos `Modifier.verticalScroll(rememberScrollState())` para que o formulário role normalmente mesmo com o teclado aberto ou com a tela na horizontal.

---

## 5. Roteiro Rápido de Demonstração na Hora da Defesa (3 minutos)

1. **Aba Locais (Create + API 1):**
   - Toque no botão **`+`**, digite `Curitiba` e toque em **"Buscar coordenadas pelo nome (API)"**. Mostre que a latitude e longitude preencheram sozinhas via Retrofit. Toque em **Salvar**.
2. **Consulta de Clima (API 2) + Update:**
   - No card de Curitiba, toque em **"Condições do céu (API)"** e mostre as nuvens, temperatura e recomendação.
   - Toque no ícone de **lápis (Editar)**, mude o nome para `Curitiba - Parque Barigui` e salve (mostrando o **Update** da Tabela 1).
3. **Aba Observações (Create + API + Read/Filtro):**
   - Vá na aba **Observações**, toque em **`+`**, digite `Saturno`, note que a condição do céu da API já aparece (ou clique em *"Consultar clima atual na API"*), escolha nota 5 estrelas e **Salve**.
   - Mostre o **filtro por local** nos chips do topo.
4. **Update + Compartilhamento + Delete:**
   - Edite a observação de Saturno no ícone de **lápis** para adicionar uma anotação (mostrando o **Update** da Tabela 2).
   - Toque no ícone de **Compartilhar** e mostre o menu do Android abrindo para enviar a outros apps.
   - Por fim, exclua a observação (ou exclua o local mostrando o aviso do `CASCADE`).

