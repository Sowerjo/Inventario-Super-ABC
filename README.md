## 🐛 Solução de Problemas
# Inventário ABC
### App não abre
- Verifique se o dispositivo tem Android 8.0+
- Reinstale o aplicativo
Aplicativo Android para leitura e gerenciamento de códigos de inventário com inserção manual e armazenamento em CSV.
### Lista vazia
- Verifique se há arquivo `inventario.csv` na pasta configurada
- Certifique-se que o CSV está no formato correto
- Evite editar o CSV com Excel (use editor de texto)
## 📱 Sobre o App
### Erro ao salvar
- Toque na engrenagem para reconfigurar a pasta
- Verifique permissões de escrita na pasta

### CSV corrompido
- O Excel pode alterar o formato, use editor de texto
- Mantenha a codificação UTF-8
- Preserve o formato de data exato

## 📱 Compatibilidade

- **Android 8.0** (API 26) ao **Android 15** (API 35)
- **Orientação**: Retrato (portrait)
- **Teclado**: Físico e virtual
- **Armazenamento**: SAF (Storage Access Framework)

## 🔒 Permissões

- **VIBRATE**: Feedback tátil ao confirmar ações
- **Storage Access**: Via SAF, solicitado apenas quando necessário

## 🏷️ Versão

- **Versão**: 1.0
- **Build**: 1
- **Package**: com.mobitech.inventarioabc

O Inventário ABC é um aplicativo desenvolvido em Kotlin que permite:
- Inserção manual de códigos de produtos
- Controle de quantidades
- Armazenamento automático em CSV
- Lista editável de produtos lidos
- Backup automático a cada 5 leituras

## 🛠️ Requisitos Técnicos

### Desenvolvimento
- **Android Studio**: Jellyfish | 2023.3.1 ou superior
- **Gradle**: 8.0+
- **Kotlin**: 1.9.0+
- **Java**: JDK 11

### Dispositivo
- **Android**: 8.0 (API 26) ou superior
- **Compilação**: API 36
- **Target**: API 35
- **Permissões**: VIBRATE (para feedback tátil)

## 🏗️ Arquitetura

O projeto segue a arquitetura **MVVM + Repository + UseCases**:

```
app/
├── data/
│   ├── csv/                    # Gerenciamento de arquivos CSV
│   └── repository/             # Camada de dados
├── domain/
│   ├── model/                  # Modelos de dados
│   └── usecase/               # Casos de uso
├── ui/
│   ├── leitura/               # Tela de leitura de códigos
│   └── lista/                 # Tela de lista de produtos
└── util/                      # Utilitários
```

## 🚀 Como Compilar

### 1. Clone o repositório
```bash
git clone [URL_DO_REPOSITORIO]
cd inventarioABC
```

### 2. Abrir no Android Studio
- Abra o Android Studio
- File → Open → Selecione a pasta do projeto
- Aguarde a sincronização do Gradle

### 3. Compilar Debug
```bash
./gradlew assembleDebug
```

### 4. Compilar Release
Para compilar a versão de produção, configure as chaves de assinatura:

1. Crie o arquivo `local.properties` na raiz do projeto:
```properties
RELEASE_STORE_FILE=caminho/para/seu/keystore.jks
RELEASE_STORE_PASSWORD=sua_senha_keystore
RELEASE_KEY_ALIAS=seu_alias
RELEASE_KEY_PASSWORD=sua_senha_chave
```

2. Execute:
```bash
./gradlew assembleRelease
```

### 5. Instalar no dispositivo
```bash
./gradlew installDebug
```

## 📋 Funcionalidades

### Tela Principal (Leitura)
- **Campo Código**: Inserção manual de códigos de produtos
- **Campo Quantidade**: Entrada numérica obrigatória
- **Botão Confirmar**: Salva o produto no inventário
- **Ícone Teclado**: Abre teclado virtual no campo código
- **Engrenagem**: Acesso às configurações (seleção de pasta)
- **Navegação**: Enter no código pula para quantidade, Enter na quantidade confirma

### Tela de Lista
- **Lista de Produtos**: Exibe todos os itens salvos
- **Campo de Busca**: Pesquisa por código
- **Contador Total**: Mostra quantidade de produtos únicos
- **Ações por Item**: Editar quantidade e Excluir
- **Ordenação**: Últimos produtos inseridos aparecem no topo

### Armazenamento
- **Pasta Configurável**: Usuário escolhe via engrenagem
- **Formato CSV**: `codigo,quantidade,data_hora_iso`
- **Backup Automático**: A cada 5 confirmações
- **Escrita Atômica**: Previne corrupção de dados

## 📁 Configuração de Pasta

### Primeira Execução
1. Toque na engrenagem (⚙️) no canto superior direito
2. Selecione ou crie a pasta "INVENTARIO"
3. Confirme a seleção

### Estrutura de Arquivos
```
INVENTARIO/
├── inventario.csv                    # Arquivo principal
└── inventario_bkp_YYYYMMDD_HHMMSS.csv  # Backups automáticos
```

### Formato do CSV
```csv
codigo,quantidade,data_hora_iso
1234567890,15,2025-09-01T10:30:00-03:00
9876543210,8,2025-09-01T10:31:15-03:00
```

## 💡 Como Usar

### Inserir Produto
1. Digite o código no campo "Código do Produto"
2. Use Tab/Enter ou toque no campo quantidade
3. Digite a quantidade desejada
4. Pressione "Confirmar" ou Enter na quantidade

### Editar Produto Existente
- Se o código já existe, será perguntado se deseja alterar a quantidade
- Escolha "Alterar" e insira a nova quantidade

### Gerenciar Lista
1. Toque em "Ver Itens" na tela principal
2. Use a busca para encontrar produtos específicos
3. Toque "Editar" para alterar quantidade
4. Toque "Excluir" para remover (com confirmação)

### Navegar entre Campos
- **Enter no código**: Pula para quantidade
- **Enter na quantidade**: Confirma e volta para código
- **Tab**: Navega entre campos
- **Ícone teclado**: Abre teclado virtual no campo código

## 🔧 Configurações Avançadas

### Backup Manual
- Backups são criados automaticamente a cada 5 confirmações
- Para backup manual, copie o arquivo `inventario.csv`

### Importar Dados Existentes
1. Prepare um CSV no formato correto
2. Nomeie como `inventario.csv`
3. Coloque na pasta INVENTARIO configurada
4. Reinicie o app

### Formato de Data
- **Padrão**: `yyyy-MM-dd'T'HH:mm:ssXXX`
- **Exemplo**: `2025-09-01T14:30:15-03:00`
- **Fuso**: Horário local do dispositivo


## Logs e Debug

Logs estão disponíveis apenas no repositório (nível DEBUG) e podem ser removidos por flag de produção.
