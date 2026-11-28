[简体中文](USER_GUIDE_zh.md) | [繁體中文](USER_GUIDE_zh_TW.md) | [English](USER_GUIDE_en.md) | [日本語](USER_GUIDE_ja.md) | [한국어](USER_GUIDE_ko.md) | **Deutsch** | [Français](USER_GUIDE_fr.md) | [Español](USER_GUIDE_es.md) | [Русский](USER_GUIDE_ru.md) | [Português](USER_GUIDE_pt.md)

# WatchSend Benutzerhandbuch

> Dateiübertragung auf der Uhr · Anleitung

Gilt für Version 1.8.0 (versionCode 10800)   November 2026

## 1 Über diese Anleitung

Diese Anleitung richtet sich an Nutzer von WatchSend und beschreibt Installation, Verbindung, Senden und Empfangen sowie die tatsächliche Wirkung jeder Option in den Einstellungen. Schaltflächen und Menüs sind so benannt, wie die App sie in der gewählten Sprache zeigt. Nach einem Sprachwechsel sollte die Ausgabe in derselben Sprache gelesen werden.

Behandelt wird nur die Bedienung, nicht das Bauen, Paketieren oder Erweitern. Werkzeugkette, Beitragsregeln und Pflege der Sprachpakete stehen in CONTRIBUTING und im Ordner docs des Repositorys. Alle Angaben beruhen auf der Umsetzung von 1.8.0; die Cloud-Build-Umgebung kann keinen Emulator starten, daher sollte das Gefühl von Gesten am echten Gerät beurteilt werden.

## 2 Installation und erster Start

WatchSend wird als APK verteilt, läuft ab Android 2.3 (API 10) und lässt sich auf einer Uhr oder einem Telefon als Sender bzw. Empfänger installieren. Auf der Uhr erfolgt die Installation meist über adb oder über eine Begleit-App am Telefon. Danach genügt es, WatchSend in der App-Liste zu öffnen; Anmeldung und Konto sind nicht nötig.

Beim ersten Start fragt die App nach Benachrichtigungs- und Speicherberechtigungen. Benachrichtigungen melden eingehende Dateien und Fortschritt im Hintergrund, der Speicher nimmt Dateien in Galerie oder Verlaufsordner auf. Eine Ablehnung stürzt die App nicht ab, lässt aber Hintergrundmeldungen fehlen und kann einzelne Empfangswege scheitern lassen; beide Rechte sind zu empfehlen.

Oben auf dem Startbildschirm sitzt der Dienstschalter mit „Start“ oder „Stopp“, dem Laufzustand und der lokalen Adresse. Im gestoppten Zustand finden andere Geräte die Uhr nicht. Ein Tippen auf Start öffnet einen HTTP-Dienst auf Port 53317 und beginnt die periodische Multicast-Suche; erst dann ist Übertragen möglich.

## 3 Bildschirm und Grundbedienung

Der Startbildschirm zeigt von oben nach unten Status und Adresse, die Geräteliste sowie die Einstiege Senden, Empfangsverlauf, Cross-Net, Einstellungen und Protokoll. Die Liste scrollt auf zwei Wegen: ab Wear OS 3 per Krone oder mit dem Finger. Beim Scrollen schrumpfen die Karten am Rand und werden blasser, um die Position anzuzeigen.

- Ein Tippen auf eine Karte öffnet die nächste Ebene. Die Zurück-Schaltfläche unten oder die System-Zurück-Taste verlässt die aktuelle Seite.
- In-App-Zurück per Wischen ist standardmäßig aktiv: Vom Bildschirmrand seitlich wischen, die Seite folgt dem Finger, schrumpft, blendet aus und kehrt zurück. Wer die System-Zurück-Geste bevorzugt, schaltet unter Einstellungen – Allgemein – In-App-Zurück aktivieren ab; danach arbeitet System-Zurück wieder normal und Wischen löst keine Navigation mehr aus.
- Die Schnittstellenskalierung stellt die gesamte App zwischen 0.3x und 2.5x ein. Bei runder Uhr ist dies der erste Griff, wenn Text gedrängt wirkt.
- Runde Uhren sollten die Rundbildschirm-Optimierung aktivieren; bei Uhren mit Kinn, etwa der Moto 360, zusätzlich die untere Aussparung, damit Schaltflächen nicht abgeschnitten werden.

## 4 Dateien senden

Senden läuft in vier festen Schritten: Inhalt wählen, Gerät wählen, bestätigen, Fortschritt abwarten. Die Auswahl lässt sich unterwegs ändern, gewählte Einträge bleiben in der Warteliste.

1. Auf dem Startbildschirm „Senden“ antippen; das Typmenü öffnet sich: Datei, Medien, App, Ordner, Text, Zwischenablage.
2. Einträge auswählen. Dateien und Medien erlauben Mehrfachauswahl, Apps und Ordner je einzeln, Text und Zwischenablage übernehmen den aktuellen Inhalt.
3. Zur Geräteliste zurückkehren und das Ziel antippen. Die Gegenseite muss den Dienst gestartet haben und im selben Netz liegen oder für Cross-Net eingerichtet sein.
4. Den Fortschrittsbalken abwarten. Erfolg meldet sich mit Hinweis und Vibration, ein Fehler mit Code; Einzelheiten stehen im Protokoll.

Text geht als LocalSend-Textnachricht hinaus und erscheint beim Empfänger in Mitteilung oder Unterhaltung. Die Zwischenablage sendet den aktuellen Inhalt als einen Textblock, keine Bilder. Bei großen Dateien bleibt der Bildschirm an und kehrt danach zur Energiesparlogik zurück.

## 5 Dateien empfangen

Solange der Dienst läuft, finden Geräte im selben Netz die Uhr und senden. Vor dem Empfang erscheint eine Bestätigung mit Absendername, Dateianzahl und Gesamtgröße; Annehmen oder Ablehnen ist möglich, und eine Ablehnung wird sofort gemeldet, statt die Gegenseite warten zu lassen.

„Schnellspeichern“ bestimmt, ob diese Bestätigung entfällt, in drei Stufen: jedes Mal fragen, alles automatisch annehmen, nur Favoriten automatisch annehmen. Standard ist jedes Mal fragen; dabei bleiben, bis die Absender vertrauenswürdig sind.

Große Dateien setzen fort: Nach einer Unterbrechung läuft der erneute Versand ab der bereits geschriebenen Stelle weiter statt von vorn. Empfangene Fotos und Videos können in die Galerie wandern, alles andere landet im Verlauf; beides lässt sich getrennt abschalten.

## 6 Weboberfläche und Cross-Net-Übertragung

### 6.1 Weboberfläche

Läuft der Dienst, öffnet ein Browser im selben LAN die auf dem Startbildschirm angezeigte Adresse, etwa http://192.168.1.100:53317, und erhält eine minimale Seite: Dateien per Ziehen senden, Fortschritt je Datei verfolgen, Zwischenablagetexte mit der Uhr austauschen. Die Adresse lässt sich per Tippen kopieren; „Web-URL“ zeigt einen QR-Code zum Abscannen mit dem Telefon.

Die Webseite folgt der Browsersprache und bringt 10 mit: vereinfachtes und traditionelles Chinesisch, Englisch, Japanisch, Koreanisch, Deutsch, Französisch, Spanisch, Russisch, Portugiesisch. Die App selbst deckt 52 Sprachen ab; ohne Treffer fällt die Webseite auf Englisch zurück.

### 6.2 Cross-Net

Außerhalb eines LAN gibt es zwei Wege. Passwortmodus: Eine Seite erzeugt einen sechsstelligen Einmalcode, die andere gibt ihn im Cross-Net-Bildschirm ein, ohne öffentliche IP. Direktmodus: Port über FRP, Ngrok oder ZeroTier veröffentlichen und im manuellen Feld IP:53317 eintragen; die Geschwindigkeit hängt am Tunnel.

Cross-Net hängt an fremder Infrastruktur und am eigenen Netz, Stabilität garantiert diese App nicht. Baut sich keine Verbindung auf, zuerst im Protokoll prüfen, ob Adresse und Port erreichbar sind, dann ob der Tunnel läuft.

## 7 Verlauf

Der Empfangsverlauf listet übertragene Dateien und Texte absteigend nach Zeit, ZIP-Dateien mit deutlicher Markierung. Langes Drücken öffnet das Aktionsmenü, Tippen Details oder die Datei.

- Erneut senden legt die Dateien des Eintrags zurück in die Warteliste; zurück zur Geräteliste und ein Gerät antippen, ohne Ordner zu suchen.
- Teilen reicht die Datei über das System-Teilen an andere Apps weiter.
- Entpacken gilt nur für Archive und nutzt die eingebaute reine Java-Engine mit Schutz gegen Pfadausbruch, schreibt also nicht außerhalb des Zielordners.
- Löschen entfernt den Eintrag; bereits in der Galerie gespeicherte Dateien bleiben unberührt.

## 8 Einstellungen

Die Einstellungen sind wie unten gruppiert. Standardwerte gelten für eine frische Installation ohne Änderungen; nach Anpassungen zählt der tatsächlich angezeigte Wert.

**Tabelle 1  Einstellungen im Überblick**

| Gruppe | Eintrag | Standard | Wirkung |
| --- | --- | --- | --- |
| Allgemein | Design | System folgen | System folgen, hell erzwingen, dunkel erzwingen |
| Allgemein | Sprache | System folgen | 52 Sprachen; debug_keys am Listenende zeigt Rohschlüssel zur Fehlersuche |
| Allgemein | Schnittstellenskalierung | Mittel (1.0x) | Skaliert Text und Bedienelemente zwischen 0.3x und 2.5x |
| Allgemein | Bildschirmform | Automatisch | Erkennt runde oder eckige Uhr, kann erzwungen werden |
| Allgemein | Untere Aussparung | Aus | Schiebt Inhalte bei runden Uhren mit Kinn, etwa Moto 360, nach oben |
| Allgemein | Dynamische Farbe | An | Folgt Hintergrundbild oder Zifferblatt (ab Android 12) |
| Allgemein | In-App-Zurück aktivieren | An | An sperrt System-Zurück und nutzt Wischen; aus stellt System-Zurück wieder her und fängt Wischen nicht ab |
| Allgemein | Geringe Leistung | Automatisch | Schwächt oder stoppt UI-Animationen; ohne manuelle Wahl entscheiden freier Speicher und Kerne |
| Allgemein | Dienst bei Inaktivität pausieren | Aus | Nach 3 Minuten Bildschirm aus pausiert der Empfang zum Stromsparen, bei Aktivierung wieder an; eingehende Sendungen können entgehen |
| Übertragung | Schnellspeichern | Jedes Mal fragen | Jedes Mal fragen, alles automatisch, nur Favoriten automatisch |
| Übertragung | Favoriten | Leer | Merkt häufige Geräte, kombiniert mit obigem Eintrag für automatischen Empfang |
| Übertragung | In Galerie speichern | An | Legt empfangene Fotos und Videos in die Galerie |
| Übertragung | Im Verlauf speichern | An | Schreibt Empfangenes in den Verlauf |
| Netzwerk & Automatisierung | HTTPS aktivieren | Aus | Übertragung per TLS; sehr alte Geräte können nur TLS 1.0 und scheitern |
| Netzwerk & Automatisierung | Port | 53317 | Dienstport; nach Änderung müssen Sender und Empfänger übereinstimmen |
| Netzwerk & Automatisierung | Webübertragung aktivieren | An | Erlaubt dem Browser den Zugriff auf die lokale Dienstadresse |
| Netzwerk & Automatisierung | Web-URL | — | Zeigt Adresse und QR-Code, Link kopierbar |
| Netzwerk & Automatisierung | Download-Freigabe | Aus | LAN-Geräte dürfen per LocalSend Dateien aus dem lokalen Ordner holen |
| Netzwerk & Automatisierung | Autostart beim Booten | Aus | Startet den Dienst beim Booten; passend für Dauerempfänger |
| Gerät & Sicherheit | Gerätenamen ändern | Gerätemodell | Name, den andere Geräte in ihrer Liste sehen |
| Gerät & Sicherheit | PIN aktivieren | Aus | Gegenseite muss vor dem Senden die PIN eingeben |
| Gerät & Sicherheit | PIN ändern | 0000 | Vier Ziffern; auf einen nur selbst bekannten Wert ändern |
| Gerät & Sicherheit | Gefährliche Endungen blockieren | exe,bat,cmd,vbs,sh | Dateien mit diesen Endungen werden abgelehnt |
| Gerät & Sicherheit | Weiß-/Schwarzliste | Leer | Gerät lange drücken zum Sperren oder für Weißliste mit Schnellspeichern |
| Geek-Modus | Stille Installation (Beta) | Aus | Installiert eine empfangene APK über Root oder Shizuku automatisch |
| Sonstiges | Sicherung / Wiederherstellung | — | Einstellungen in die Zwischenablage exportieren oder von dort importieren |
| Sonstiges | Über WatchSend | — | Version, Protokoll und Danksagung |

## 9 Häufige Probleme

**Tabelle 2  Symptome und Abhilfe**

| Symptom | Abhilfe |
| --- | --- |
| Die Liste bleibt bei „keine Geräte, scannen antippen“ | Prüfen, ob die Gegenseite den Dienst gestartet hat und im selben WLAN liegt; AP-Isolation im Router abschalten; erneut scannen; sonst IP manuell eintragen |
| Jede Sendung der Gegenseite endet mit 400 | Meist ein Protokollfeld oder HTTPS-Konflikt: beidseitig HTTPS abschalten oder beide auf dieselbe Version bringen und erneut versuchen, dann den Statuscode im Protokoll lesen |
| Der Browser öffnet die Webseite nicht | Dienststart, freien Port und gemeinsames Subnetz prüfen; manche Router blockieren Geräte-zu-Gerät-Verkehr |
| Zurück-Taste reagiert nicht oder zeigt nur einen Hinweis | In-App-Zurück ist aktiv; abschalten stellt System-Zurück wieder her |
| Text oder Schaltflächen werden am runden Rand abgeschnitten | Rundbildschirm-Optimierung einschalten, bei Bedarf untere Aussparung, und die Skalierung eine Stufe senken |
| Nach längerem Bildschirm-aus kommt nichts an | Prüfen, ob „Dienst bei Inaktivität pausieren“ aktiv ist (Standard aus) und ob das System WatchSend stromoptimiert |
| Der Akku fällt beim Übertragen schnell | Geringe Leistung einschalten; unter 15 % und ohne Ladegerät drosselt die App bereits auf 512 KB/s |

## 10 Sicherheit, Strom und bekannte Grenzen

- Standard ist unverschlüsseltes HTTP und nur für vertrauenswürdige LAN geeignet. Für Verschlüsselung HTTPS einschalten und den Zertifikatsfingerabdruck der Gegenseite prüfen.
- Die PIN steht standardmäßig auf 0000; eigene vier Ziffern setzen. Cross-Net nutzt einen sechsstelligen Einmalcode, der nach Gebrauch verfällt.
- Gefährliche Endungen exe, bat, cmd, vbs, sh werden standardmäßig blockiert; Weiß- und Schwarzliste liegen im Langdruck-Menü der Geräte.
- Stromsparen hat drei Stufen: Geringe Leistung stoppt fast alle Animationen und begrenzt auf 2 MB/s; unter 15 % Akku ohne Laden auf 512 KB/s; nach 3 Minuten Bildschirm aus pausiert der Dienst (Standard aus, bei laufender Übertragung verschoben).
- Bekannte Grenzen: Cross-Net hängt an Fremddiensten, Stabilität ist nicht garantiert; stille Installation unterliegt der ROM; älteren Uhren fehlen Vibrationsmotor oder Krone; Entpacken und Gesten wurden nur auf kleinen echten Bildschirmen geprüft.
- Das Projekt ist MIT-lizenziert, mit KI-Unterstützung entstanden und am Gerät geprüft; es wird wie besehen ohne Zusicherung der Eignung bereitgestellt.

---

*WatchSend · MIT License · https://github.com/styisme/watchsend*
