# Inventário ABC

Aplicativo Android para leitura e gerenciamento de códigos de barras em inventário.

## Funcionalidades

- **Leitura de Códigos**: Scanner automático usando câmera (EAN-13, EAN-8, CODE-128, CODE-39, QR)
- **Gestão de Inventário**: Lista editável com opções de editar e excluir
- **Persistência CSV**: Dados salvos automaticamente em formato CSV
- **Backups Automáticos**: Backup criado a cada 5 leituras confirmadas
- **Ergonomia**: Vibração e beep ao detectar código, foco automático no campo quantidade

## Requisitos

- Android 8.0 (API 26) ou superior
- Permissão de câmera
- Acesso a pasta de armazenamento via SAF (Storage Access Framework)

## Configuração Inicial

### 1. Permissões
Na primeira execução, o app solicitará:
- **Câmera**: Para escanear códigos de barras
- **Pasta INVENTARIO**: Para salvar arquivos CSV

### 2. Seleção da Pasta INVENTARIO
- Na primeira execução, será solicitado escolher/criar a pasta INVENTARIO
- Recomenda-se criar na raiz do armazenamento interno
- A permissão é persistida para futuras execuções

## Arquivos Gerados

### Pasta INVENTARIO
Todos os arquivos são salvos na pasta escolhida pelo usuário:

- **inventario.csv**: Arquivo principal com dados atuais
- **inventario_bkp_YYYYMMDD_HHMMSS.csv**: Backups automáticos

### Formato CSV
```
codigo,quantidade,data_hora_iso
1234567890123,5,2023-08-28T14:30:15-03:00
9876543210987,2,2023-08-28T14:31:22-03:00
```

## Como Usar

### Tela de Leitura
1. Aponte a câmera para o código de barras
2. O código será preenchido automaticamente
3. Digite a quantidade (campo numérico)
4. Toque em **Confirmar**
5. Para inserir código manualmente: menu ⋮ → "Inserir código manualmente"

### Tela de Lista
- Visualize todos os itens lidos
- **Editar**: Alterar quantidade de um item existente
- **Excluir**: Remover item (com confirmação)
- Navegue via botão "Ver Itens" na tela de leitura

### Códigos Duplicados
- Ao tentar confirmar um código já existente:
  - Opção "Alterar": Atualiza a quantidade e data/hora
  - Opção "Cancelar": Mantém dados originais

## Backups

- Criados automaticamente a cada 5 **novas leituras** confirmadas
- Edições e exclusões não contam para o contador
- Arquivo nomeado com timestamp: `inventario_bkp_20230828_143015.csv`
- Contador zerado após backup criado

## Restauração

Para restaurar um backup:
1. Localize o arquivo de backup desejado na pasta INVENTARIO
2. Renomeie para `inventario.csv` (substitua o atual)
3. Reinicie o aplicativo

## Solução de Problemas

### Erro ao Salvar CSV
- Verifique se a pasta INVENTARIO ainda existe
- O app solicitará nova seleção de pasta se necessário

### Perda de Permissão SAF
- Pode ocorrer após atualizações do sistema
- O app detectará e solicitará nova seleção da pasta

### Códigos Não Detectados
- Verifique iluminação adequada
- Mantenha distância apropriada do código
- Use inserção manual como alternativa (menu ⋮)

## Arquitetura Técnica

- **Arquitetura**: MVVM + Repository + Use Cases
- **UI**: Jetpack Navigation, Material 3, ViewBinding
- **Scanner**: CameraX + ML Kit Barcode Scanning
- **Persistência**: SAF (Storage Access Framework) + CSV
- **Linguagem**: 100% Kotlin

## Logs e Debug

Logs estão disponíveis apenas no repositório (nível DEBUG) e podem ser removidos por flag de produção.
