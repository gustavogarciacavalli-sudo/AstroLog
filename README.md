# AstroLog 🔭 — Diário de Observação do Céu Noturno

O **AstroLog** é um aplicativo Android desenvolvido em **Kotlin** e **Jetpack Compose**, projetado para servir como um diário de bordo digital para astrônomos amadores e entusiastas da observação celeste.

O app permite cadastrar e gerenciar locais de observação favoritos, consultar em tempo real as condições meteorológicas (cobertura de nuvens, temperatura, velocidade do vento) usando a API **Open-Meteo**, registrar detalhes de cada observação astronômica e compartilhá-las diretamente com amigos e redes sociais.

---

## 🌟 Principais Funcionalidades

- **Gerenciamento de Locais (CRUD)**:
  - Cadastro de pontos de observação (sítio, terraço, praça, observatório).
  - Preenchimento e geocodificação de coordenadas (latitude/longitude) via busca por cidade/endereço.
  - Edição e remoção de locais cadastrados.

- **Previsão do Céu em Tempo Real**:
  - Consulta automática à API meteorológica baseada nas coordenadas do local.
  - Indicadores de temperatura, sensação térmica, cobertura de nuvens (%) e vento.
  - Diagnóstico visual intuitivo se as condições estão ideais para a observação astronômica.

- **Diário de Observações Celestes (CRUD)**:
  - Registro de eventos observados (planetas, constelações, chuvas de meteoros, nebulosas, etc.).
  - Vinculação de cada observação a um local cadastrado.
  - Avaliação da noite (estrelas/nota), data, anotações e registro das condições meteorológicas no momento da sessão.
  - Filtro por local para navegação simplificada no histórico.

- **Compartilhamento Integrado**:
  - Exportação e compartilhamento rápido do relato da observação com outros aplicativos (WhatsApp, Telegram, E-mail, etc.) via Android Sharesheet (`ACTION_SEND`).

---

## 🛠️ Tecnologias e Arquitetura

O projeto adota as melhores práticas de desenvolvimento Android moderno:

| Componente | Tecnologia / Abordagem | Descrição |
|---|---|---|
| **Linguagem** | Kotlin | Código conciso, seguro e idiomático |
| **Interface (UI)** | Jetpack Compose + Material 3 | Layout declarativo com tema escuro nativo (*Dark Mode*) |
| **Arquitetura** | MVVM (Model-View-ViewModel) | Separação clara de responsabilidades e retenção de estado em rotações |
| **Persistência de Dados** | Room Database (SQLite) | 2 tabelas (`Local` e `Observacao`) com relacionamento 1:N e `CASCADE` |
| **Reatividade** | Coroutines & StateFlow | UI atualizada automaticamente e de forma reativa a cada alteração no banco |
| **Comunicação Web / API** | Retrofit 2 + Gson | Requisições HTTP REST assíncronas para a API Open-Meteo (Clima e Geocoding) |
| **Integração do Sistema** | Android Intents | Compartilhamento nativo de texto formatado (`ACTION_SEND`) |

---

## 🗂️ Estrutura do Projeto

```text
app/src/main/java/br/edu/up/astrolog/
├── MainActivity.kt        # Ponto de entrada da aplicação
├── Telas.kt               # Telas e componentes Compose (Locais, Observações e Diálogos)
├── AstroViewModel.kt      # Gerenciamento de estado, regras de negócio e chamadas assíncronas
├── Data.kt                # Entidades Room (Local, Observacao), DAOs e AppDatabase
└── Api.kt                 # Configuração do Retrofit, interfaces e modelos Open-Meteo
```

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- **Android Studio** (versão Ladybug / Hedgehog ou superior recomendada).
- **JDK 17** ou superior.
- Dispositivo físico Android ou Emulador configurado (Android 8.0 / API 26+).

### Passos
1. Clone o repositório:
   ```bash
   git clone https://github.com/gustavogarciacavalli-sudo/AstroLog.git
   ```
2. Abra a pasta do projeto no **Android Studio**.
3. Aguarde o Android Studio sincronizar as dependências do **Gradle**.
4. Conecte um emulador ou dispositivo físico com a depuração USB ativada.
5. Clique em **Run** (`Shift + F10`) para compilar e iniciar o aplicativo.
