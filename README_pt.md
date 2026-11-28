**Português** | [简体中文](README.md) | [繁體中文](README_zh_TW.md) | [English](README_EN.md) | [日本語](README_ja.md) | [한국어](README_ko.md) | [Deutsch](README_de.md) | [Français](README_fr.md) | [Español](README_es.md) | [Русский](README_ru.md)

📖 **Manual do usuário**: [edição em português](docs/USER_GUIDE_pt.md)

# WatchSend

> A versão de relógio do OldSend. Mantém todas as capacidades de transferência e reconstrói a interface para telas pequenas e redondas como «roda 3D» e cartões pílula, no estilo do Wear OS.

[![Build Android APK](https://github.com/styisme/watchsend/actions/workflows/android.yml/badge.svg)](https://github.com/styisme/watchsend/actions/workflows/android.yml)
[![Version](https://img.shields.io/badge/version-1.9.0-512BD4)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Made with AI](https://img.shields.io/badge/made%20with-AI-FF6B6B)](docs/AI_DISCLOSURE.md)
[![Platform](https://img.shields.io/badge/platform-Android%202.3%2B-brightgreen)]()

---

## 📦 Versão

Versão atual: **v1.9.0**. 52 idiomas completos (357–358 entradas nos 10 idiomas base, 353 nos demais, 18.403 no total). Histórico em [CHANGELOG.md](CHANGELOG.md).

---

## ✨ Recursos

### ⌚ Otimização para relógio
- **Coroa giratória** (Wear OS 3+), **resposta tátil**, tela sempre acesa durante a transferência, entrada por voz, tiles (Android 7.0+).
- **Modo de baixo desempenho**: desliga quase todas as animações (roda 3D, entrada, toque, varredura de carregamento, respiração de três pontos, brilho do esqueleto, transição do interruptor, quique do painel), reduz a concorrência da busca de reserva e limita a velocidade. Ativa sozinho em aparelhos fracos (heap ≤256 MB / marca low-RAM / ≤2 núcleos) quando nunca foi configurado.
- **Limite de velocidade**: 2 MB/s no baixo desempenho, 512 KB/s com bateria abaixo de 15 % e sem carregar, tanto no envio quanto no recebimento.
- **Pausa por inatividade (desligada por padrão)**: 3 minutos após apagar a tela, as escutas HTTP e multicast são pausadas e retomadas ao acender; é adiada automaticamente durante uma transferência.

### 🛠️ Caixa de ferramentas geek
- **Instalação silenciosa (beta)**: Root ou Shizuku, proxy Binder IPC escrito à mão.
- **Extração ZIP nativa**: motor Java puro com proteção contra path traversal.
- **Histórico**: toque longo para **reenviar**, compartilhar, extrair ou excluir. «Reenviar» devolve o arquivo à lista de envio.
- **Web em 10 idiomas**: detecta o idioma do navegador, arrastar e soltar, sincronização da área de transferência.

### 🎨 Interface e idiomas
- Escala física da UI (0,3x–2,5x), tema de três estados, cor dinâmica no Android 12+, roda 3D, adaptação a tela redonda, gesto de voltar.
- **52 idiomas completos**: pelo menos 353 entradas localizadas por idioma, 18.403 no total, sem chaves cruas. Os dois textos "Ativar voltar no app" da 1.7.13 cobrem os 52 idiomas; os quatro da 1.7.12 seguem só nos 10 base.
- **Carregamento de idiomas sob demanda**: na inicialização apenas os pacotes base, o resto é montado uma vez após a escolha do idioma (economiza ~1 MB de heap e tempo de inicialização). Veja o [guia de idiomas](docs/LANGUAGES.md).
- **Wear-Uikit**: 44 widgets Java nativos + 6 contêineres de tela cheia, sem dependências externas.

### 🌐 Rede e compatibilidade
- PIN de 6 dígitos de uso único, FRP/Ngrok/ZeroTier, UPnP/STUN/IPv6.
- Compatível com LocalSend v1/v2.2, desde Android 2.3 (API 10), listas branca/negra e bloqueio de extensões perigosas.

---

## 📱 Início rápido
1. **Relógio**: mesmo Wi-Fi, iniciar o serviço, os aparelhos aparecem sozinhos, tocar para enviar.
2. **Web**: abrir `http://<IP do relógio>:53317` no navegador.
3. **Fora da LAN**: PIN de 6 dígitos ou conexão direta `[IP]:53317`.

---

## 🛠️ Compilação
**AGP 3.6.3 + Gradle 5.6.4 + JDK 8** (minSdk 10 mantido). A CI publica conforme a versão em `CHANGELOG.md`.

---

## 🚧 Limites e riscos
1. Funções fora da LAN dependem de serviços de terceiros ou de configuração pública própria.
2. Alguns aparelhos antigos não têm vibrador nem coroa; tiles desde Android 7.0.
3. Android 2.3 só suporta TLS 1.0: HTTPS falha.
4. Efeitos 3D exigem GPU; o modo de baixo desempenho ativa sozinho e também pode ser forçado.

---

## 📚 Documentos
- [CHANGELOG.md](CHANGELOG.md), [CONTRIBUTING.md](CONTRIBUTING.md)
- [docs/AI_DISCLOSURE.md](docs/AI_DISCLOSURE.md), [docs/DEVELOPMENT_CONSTRAINTS.md](docs/DEVELOPMENT_CONSTRAINTS.md)
- [docs/LANGUAGES.md](docs/LANGUAGES.md) — lista de idiomas, troca e regras de fallback
- [docs/USER_GUIDE_pt.md](docs/USER_GUIDE_pt.md) — o manual completo para usuários (10 idiomas)

## 📜 Licença
MIT License. Totalmente compatível com o protocolo LocalSend (implementação própria).
