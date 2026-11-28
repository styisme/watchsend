[简体中文](USER_GUIDE_zh.md) | [繁體中文](USER_GUIDE_zh_TW.md) | [English](USER_GUIDE_en.md) | [日本語](USER_GUIDE_ja.md) | [한국어](USER_GUIDE_ko.md) | [Deutsch](USER_GUIDE_de.md) | [Français](USER_GUIDE_fr.md) | [Español](USER_GUIDE_es.md) | [Русский](USER_GUIDE_ru.md) | **Português**

# WatchSend — Manual do usuário

> Transferência de arquivos no relógio · Guia

Válido para a versão 1.7.13 (versionCode 10713)   novembro de 2026

## 1 Sobre este guia

Este guia é para quem usa o WatchSend: instalação, conexão, envio e recebimento, e o efeito real de cada opção da tela de Ajustes. Os nomes de botões e menus seguem o idioma de interface escolhido; se você trocou o idioma, leia a edição nesse mesmo idioma.

Ele trata apenas do uso, não de compilação, empacotamento ou modificações. A cadeia de ferramentas, as regras de contribuição e a manutenção dos pacotes de idioma estão no CONTRIBUTING e na pasta docs do repositório. As descrições refletem a implementação 1.7.13; o ambiente de build na nuvem não inicia emulador, então a sensação dos gestos deve ser avaliada em aparelho real.

## 2 Instalação e primeira execução

O WatchSend é distribuído como APK, funciona a partir do Android 2.3 (API 10) e pode ser instalado em um relógio ou em um telefone como emissor ou receptor. No relógio, a instalação normalmente é feita por adb ou por um aplicativo auxiliar no telefone. Depois basta abrir o WatchSend na lista de aplicativos; não há login nem conta.

Na primeira execução, o aplicativo pede permissões de notificação e armazenamento. As notificações avisam sobre arquivos recebidos e o progresso em segundo plano; o armazenamento permite gravar na galeria ou na pasta de histórico. Recusar não fecha o aplicativo, mas você perde os avisos em segundo plano e alguns caminhos de recebimento podem falhar, então convém permitir ambos.

No topo da tela inicial fica o interruptor do serviço, com “Iniciar” ou “Parar”, o estado e o endereço local. Com o serviço parado, outros aparelhos não encontram você. Ao tocar em iniciar, o aplicativo abre um serviço HTTP na porta 53317 e começa a descoberta multicast periódica; só então a transferência é possível.

## 3 Tela e controles básicos

A tela inicial mostra, de cima para baixo, estado e endereço, a lista de dispositivos e as entradas Enviar, Histórico de recebimento, Cross-Net, Ajustes e Log. A lista rola de duas formas: no Wear OS 3 ou superior pela coroa, ou com o dedo. Ao rolar, os cartões das bordas encolhem e ficam translúcidos para indicar a posição.

- Toque em um cartão para entrar no nível seguinte. O botão Voltar na parte inferior ou o voltar do sistema sai da página atual.
- O voltar por gesto interno vem ativado: deslize na horizontal a partir da borda e a página acompanha o dedo, encolhe, some e volta. Se preferir o voltar do sistema, desative Ajustes – Geral – Ativar voltar no app; o voltar do sistema volta ao normal e o gesto deixa de navegar.
- A escala da interface ajusta todo o aplicativo entre 0,3x e 2,5x. Em relógio redondo, é o primeiro ajuste a tentar quando o texto fica apertado.
- Relógios redondos devem ativar a otimização de tela redonda; modelos com queixo, como o Moto 360, também o recuo inferior para os botões não serem cortados.

## 4 Enviar arquivos

O envio segue quatro passos fixos: escolher o conteúdo, escolher o dispositivo, confirmar, aguardar o progresso. Dá para voltar e mudar a seleção; o que foi escolhido permanece na lista de pendentes.

1. Toque em Enviar na tela inicial; abre o menu de tipos: Arquivo, Mídia, Aplicativo, Pasta, Texto, Área de transferência.
2. Selecione os itens. Arquivos e mídia aceitam vários; aplicativos e pastas, um por vez; texto e área de transferência usam o conteúdo atual.
3. Volte à lista de dispositivos e toque no destino. O outro lado precisa estar com o serviço iniciado e na mesma rede, ou já configurado para Cross-Net.
4. Aguarde a barra terminar. O sucesso avisa e vibra; a falha mostra um código, com detalhes no Log.

O texto sai como mensagem de texto do LocalSend e o destinatário vê em notificação ou conversa. A área de transferência envia o conteúdo atual como um bloco de texto, nunca uma imagem. A tela fica acesa durante um envio grande e depois volta à economia de energia.

## 5 Receber arquivos

Enquanto o serviço estiver ativo, aparelhos da mesma rede encontram você e enviam. Antes de receber aparece uma confirmação com o nome do emissor, a quantidade de arquivos e o tamanho total. Dá para aceitar ou recusar; recusar avisa o emissor na hora, sem deixá-lo esperando.

“Salvamento rápido” decide se essa confirmação é pulada, em três níveis: perguntar sempre, aceitar tudo automaticamente, aceitar só favoritos. O padrão é perguntar sempre; mantenha até confiar nos emissores.

Arquivos grandes retomam: após uma interrupção, reenviar continua dos bytes já gravados em vez de recomeçar. Fotos e vídeos recebidos podem ir para a galeria e o restante para o histórico; ambos podem ser desativados separadamente.

## 6 Interface web e transferência Cross-Net

### 6.1 Interface web

Com o serviço ativo, abra no navegador de um computador da mesma LAN o endereço mostrado na tela inicial, por exemplo http://192.168.1.100:53317. Aparece uma página mínima: arrastar arquivos para enviar, acompanhar o progresso por arquivo e trocar texto da área de transferência com o relógio. Tocar no endereço copia; “URL web” mostra um QR code para escanear com o telefone.

A página web segue o idioma do navegador e traz 10: chinês simplificado e tradicional, inglês, japonês, coreano, alemão, francês, espanhol, russo e português. O aplicativo cobre 52 idiomas; sem correspondência, a página recorre ao inglês.

### 6.2 Cross-Net

Fora da mesma LAN há dois caminhos. Modo senha: um lado gera um código de uso único de seis dígitos e o outro o digita na tela Cross-Net, sem IP público. Modo direto: exponha a porta via FRP, Ngrok ou ZeroTier e digite IP:53317 no campo manual; a velocidade depende do túnel.

O Cross-Net depende de infraestrutura de terceiros e da sua rede, então a estabilidade não é garantida pelo aplicativo. Se a conexão não se estabelece, verifique primeiro no Log se o endereço e a porta são alcançáveis e depois se o túnel está no ar.

## 7 Histórico

O histórico de recebimento lista arquivos e textos trocados do mais recente ao mais antigo, com marca visível nos ZIP. Um toque longo abre o menu de ações; um toque simples mostra o detalhe ou o arquivo.

- Reenviar devolve os arquivos daquele item à lista de pendentes; volte à lista de dispositivos e toque em um, sem procurar pastas.
- Compartilhar entrega o arquivo a outro aplicativo pelo compartilhamento do sistema.
- Extrair só vale para arquivos compactados e usa o motor Java puro embutido, com proteção contra travessia de caminho, então nada é gravado fora da pasta de destino.
- Excluir remove o item; arquivos já salvos na galeria não são afetados.

## 8 Ajustes

Os ajustes são agrupados como abaixo. Os padrões descrevem uma instalação nova sem alterações; depois de mexer, vale o valor que aparece.

**Tabela 1  Ajustes em resumo**

| Grupo | Opção | Padrão | Efeito |
| --- | --- | --- | --- |
| Geral | Tema | Seguir sistema | Seguir sistema, forçar claro, forçar escuro |
| Geral | Idioma | Seguir sistema | 52 idiomas; debug_keys no fim da lista mostra as chaves cruas para diagnóstico |
| Geral | Escala da interface | Média (1,0x) | Ajusta texto e controles entre 0,3x e 2,5x |
| Geral | Formato da tela | Auto | Detecta relógio redondo ou quadrado; pode ser forçado |
| Geral | Recuo inferior | Desligado | Sobe o conteúdo em relógios redondos com queixo, como o Moto 360 |
| Geral | Cor dinâmica | Ligado | Segue o papel de parede ou mostrador (Android 12 e superior) |
| Geral | Ativar voltar no app | Ligado | Ligado bloqueia o voltar do sistema e usa o gesto; desligado restaura o voltar do sistema e para de interceptar o gesto |
| Geral | Modo de baixo desempenho | Auto | Reduz ou corta animações; sem ajuste manual, decide memória livre e núcleos |
| Geral | Pausar serviço ocioso | Desligado | Após 3 minutos com a tela apagada, a escuta pausa para economizar e volta ao acender; envios recebidos podem ser perdidos |
| Transferência | Salvamento rápido | Perguntar sempre | Perguntar sempre, aceitar tudo, só favoritos |
| Transferência | Dispositivos favoritos | Vazio | Memoriza aparelhos frequentes e combina com a opção acima |
| Transferência | Salvar na galeria | Ligado | Coloca fotos e vídeos recebidos na galeria |
| Transferência | Salvar no histórico | Ligado | Grava o conteúdo recebido no histórico |
| Rede e automação | Ativar HTTPS | Desligado | Transferência por TLS; aparelhos muito antigos só têm TLS 1.0 e podem falhar |
| Rede e automação | Porta | 53317 | Porta do serviço; depois de mudar, emissor e receptor precisam coincidir |
| Rede e automação | Ativar transferência web | Ligado | Permite ao navegador acessar o endereço local do serviço |
| Rede e automação | URL web | — | Mostra endereço e QR code; o link pode ser copiado |
| Rede e automação | Compartilhar downloads | Desligado | Permite que aparelhos da LAN baixem arquivos da pasta local via LocalSend |
| Rede e automação | Iniciar ao ligar | Desligado | Sobe o serviço na inicialização; bom para um receptor fixo |
| Dispositivo e segurança | Alterar nome | Modelo | Nome que os outros aparelhos veem |
| Dispositivo e segurança | Ativar PIN | Desligado | O emissor precisa digitar o PIN antes de enviar |
| Dispositivo e segurança | Alterar PIN | 0000 | Quatro dígitos; troque por um valor só seu |
| Dispositivo e segurança | Bloquear extensões perigosas | exe,bat,cmd,vbs,sh | Arquivos com essas extensões são recusados |
| Dispositivo e segurança | Listas branca e negra | Vazio | Toque longo em um aparelho para bloquear ou pôr na lista branca |
| Modo geek | Instalação silenciosa (beta) | Desligado | Instala automaticamente um APK recebido via Root ou Shizuku |
| Outros | Backup / restauração | — | Exporta os ajustes para a área de transferência ou importa deles |
| Outros | Sobre o WatchSend | — | Versão, protocolo e agradecimentos |

## 9 Problemas comuns

**Tabela 2  Sintomas e soluções**

| Sintoma | O que fazer |
| --- | --- |
| A lista fica em “nenhum dispositivo, toque em escanear” | Confirme que o outro lado iniciou o serviço e está no mesmo Wi-Fi; desligue o isolamento AP do roteador; repita a busca; se continuar, digite o IP à mão |
| Todo envio do outro lado volta com erro 400 | Geralmente campo de protocolo ou HTTPS divergente: desligue HTTPS nos dois lados, ou atualize ambos para a mesma versão e tente de novo, e leia o código no Log |
| O navegador não abre a página web | Confirme serviço ativo, porta livre e mesma sub-rede; alguns roteadores bloqueiam tráfego entre aparelhos |
| A tecla Voltar não responde ou só mostra um aviso | O voltar no app está ativo; desligue e o voltar do sistema volta ao normal |
| Texto ou botões cortados pela borda redonda | Ative a otimização de tela redonda, adicione recuo inferior se preciso e baixe a escala um nível |
| Nada chega após um tempo com a tela apagada | Verifique se “Pausar serviço ocioso” está ativo (padrão desligado) e se o sistema otimiza a bateria do WatchSend |
| A bateria cai rápido durante a transferência | Ative o modo de baixo desempenho; abaixo de 15% e sem carregador o app já limita a 512 KB/s |

## 10 Segurança, energia e limites conhecidos

- O padrão é HTTP sem criptografia, adequado só a LAN confiável. Para cifrar, ative HTTPS e confira a impressão digital do certificado do outro lado.
- O PIN de fábrica é 0000; defina seus quatro dígitos. O Cross-Net usa um código de uso único de seis dígitos que expira depois de usado.
- As extensões perigosas exe, bat, cmd, vbs e sh são bloqueadas por padrão; listas branca e negra ficam no menu de toque longo no aparelho.
- A economia tem três camadas: o modo de baixo desempenho corta quase toda animação e limita a 2 MB/s; abaixo de 15% de bateria sem carregar, a 512 KB/s; após 3 minutos com a tela apagada o serviço pausa (desligado por padrão e adiado durante transferência).
- Limites conhecidos: o Cross-Net depende de serviços de terceiros e não garante estabilidade; a instalação silenciosa depende da ROM; alguns relógios antigos não têm motor de vibração nem coroa; extração e gestos só foram verificados em telas reais pequenas.
- O projeto é licenciado sob MIT, feito com apoio de IA e revisado e ajustado em aparelho; é fornecido como está, sem garantia de adequação.

---

*WatchSend · MIT License · https://github.com/styisme/watchsend*
