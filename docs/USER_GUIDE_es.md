[简体中文](USER_GUIDE_zh.md) | [繁體中文](USER_GUIDE_zh_TW.md) | [English](USER_GUIDE_en.md) | [日本語](USER_GUIDE_ja.md) | [한국어](USER_GUIDE_ko.md) | [Deutsch](USER_GUIDE_de.md) | [Français](USER_GUIDE_fr.md) | **Español** | [Русский](USER_GUIDE_ru.md) | [Português](USER_GUIDE_pt.md)

# WatchSend — Guía del usuario

> Transferencia de archivos en el reloj · Manual

Válida para la versión 1.8.2 (versionCode 10802)   noviembre de 2026

## 1 Sobre esta guía

Esta guía está dirigida a quienes usan WatchSend: instalación, conexión, envío y recepción, y el efecto real de cada opción de la pantalla de Ajustes. Los nombres de botones y menús coinciden con la lengua de interfaz elegida; si la ha cambiado, lea la edición en esa misma lengua.

Solo trata el uso, no la compilación, el empaquetado ni las modificaciones. Cadena de herramientas, normas de contribución y mantenimiento de los paquetes de idioma están en CONTRIBUTING y la carpeta docs del repositorio. Las descripciones se basan en la implementación 1.8.2; el entorno de compilación en la nube no puede iniciar un emulador, así que la sensación de los gestos debe juzgarse en un dispositivo real.

## 2 Instalación y primer arranque

WatchSend se distribuye como APK, funciona desde Android 2.3 (API 10) y puede instalarse en un reloj o en un teléfono que actúe como emisor o receptor. En el reloj suele instalarse por adb o mediante una aplicación auxiliar en el teléfono. Después basta abrir WatchSend desde la lista de aplicaciones; no requiere cuenta ni inicio de sesión.

El primer arranque solicita permisos de notificaciones y almacenamiento. Las notificaciones avisan de archivos recibidos y del progreso en segundo plano; el almacenamiento permite escribir en la galería o en la carpeta del historial. Negarlos no cierra la aplicación, pero se pierden los avisos en segundo plano y algunas rutas de recepción pueden fallar, por lo que conviene conceder ambos.

Arriba en la pantalla inicial está el interruptor del servicio, con «Iniciar» o «Detener», el estado y la dirección local. Con el servicio detenido, otros equipos no le encuentran. Al pulsar iniciar, la aplicación abre un servicio HTTP en el puerto 53317 y comienza el descubrimiento multicast periódico; solo entonces se puede transferir.

## 3 Pantalla y controles básicos

La pantalla inicial muestra de arriba abajo estado y dirección, la lista de dispositivos y las entradas Enviar, Historial de recepción, Cross-Net, Ajustes y Registro. La lista se desplaza de dos maneras: desde Wear OS 3 con la corona, o con el dedo. Al desplazar, las tarjetas de los bordes se encogen y se atenúan para indicar la posición.

- Al tocar una tarjeta se entra al nivel siguiente. El botón Atrás de la parte inferior o el atrás del sistema abandona la página actual.
- El atrás por deslizamiento interno viene activado: deslice en horizontal desde el borde y la página sigue el dedo, se encoge, se desvanece y vuelve. Si prefiere el atrás del sistema, desactive Ajustes – General – Activar atrás interno; el atrás del sistema vuelve a funcionar y el deslizamiento deja de navegar.
- La escala de interfaz ajusta toda la aplicación entre 0,3x y 2,5x. En un reloj redondo, es lo primero que probar cuando el texto queda apretado.
- Los relojes redondos deberían activar la optimización de pantalla redonda; los que tienen mentón, como el Moto 360, también el margen inferior para que no se recorten los botones.

## 4 Enviar archivos

Enviar sigue cuatro pasos fijos: elegir contenido, elegir dispositivo, confirmar, esperar el progreso. Se puede volver atrás para cambiar la selección; lo elegido permanece en la lista de pendientes.

1. Toque Enviar en la pantalla inicial; se abre el menú de tipos: Archivo, Multimedia, Aplicación, Carpeta, Texto, Portapapeles.
2. Seleccione los elementos. Archivos y multimedia admiten varios; aplicaciones y carpetas, uno cada vez; texto y portapapeles toman el contenido actual.
3. Vuelva a la lista de dispositivos y toque el destino. La otra parte debe tener el servicio iniciado y estar en la misma red, o ya configurada para Cross-Net.
4. Espere a que termine la barra. El éxito avisa y vibra; el fallo muestra un código, con detalles en Registro.

El texto sale como mensaje de texto LocalSend y el receptor lo ve en una notificación o conversación. El portapapeles envía su contenido actual como un bloque de texto, nunca una imagen. La pantalla permanece encendida durante un envío grande y luego vuelve al ahorro de energía.

## 5 Recibir archivos

Mientras el servicio funciona, los equipos de la misma red le encuentran y le envían. Antes de recibir aparece una confirmación con el nombre del emisor, la cantidad de archivos y el tamaño total. Se puede aceptar o rechazar; rechazar avisa de inmediato al emisor en lugar de dejarle esperando.

«Guardado rápido» decide si se omite esa confirmación, en tres niveles: preguntar siempre, aceptar todo automáticamente, aceptar solo favoritos. Lo predeterminado es preguntar siempre; manténgalo hasta confiar en los emisores.

Los archivos grandes se reanudan: tras una interrupción, reenviar continúa desde los bytes ya escritos en lugar de empezar de cero. Las fotos y vídeos recibidos pueden ir a la galería y el resto al historial; ambos se desactivan por separado.

## 6 Interfaz web y transferencia Cross-Net

### 6.1 Interfaz web

Con el servicio iniciado, abra en un navegador del mismo LAN la dirección que muestra la pantalla inicial, por ejemplo http://192.168.1.100:53317. Aparece una página mínima: arrastrar archivos para subirlos, seguir el progreso por archivo e intercambiar texto del portapapeles con el reloj. Al tocar la dirección se copia; «URL web» muestra un código QR para escanear con el teléfono.

La página web sigue el idioma del navegador y trae 10: chino simplificado y tradicional, inglés, japonés, coreano, alemán, francés, español, ruso y portugués. La aplicación cubre 52; sin coincidencia, la página recurre al inglés.

### 6.2 Cross-Net

Fuera del mismo LAN hay dos vías. Modo contraseña: un lado genera un código de un solo uso de seis cifras y el otro lo introduce en la pantalla Cross-Net, sin IP pública. Modo directo: exponga el puerto con FRP, Ngrok o ZeroTier y escriba IP:53317 en el campo manual; la velocidad depende del túnel.

Cross-Net depende de infraestructura de terceros y de su red; la estabilidad no está garantizada por esta aplicación. Si la conexión no se establece, compruebe primero en Registro si la dirección y el puerto son alcanzables y luego si el túnel está en línea.

## 7 Historial

El historial de recepción lista archivos y textos intercambiados del más reciente al más antiguo, con marca visible en los ZIP. Una pulsación larga abre el menú de acciones; una pulsación simple muestra el detalle o el archivo.

- Reenviar devuelve los archivos de esa entrada a la lista de pendientes; vuelva a la lista de dispositivos y toque uno, sin recorrer carpetas.
- Compartir entrega el archivo a otra aplicación mediante el menú del sistema.
- Extraer solo está disponible para archivos comprimidos y usa el motor Java puro integrado, con protección contra salida de ruta, de modo que nada se escribe fuera de la carpeta destino.
- Eliminar quita la entrada; los archivos ya guardados en la galería no se ven afectados.

## 8 Ajustes

Los ajustes se agrupan como sigue. Los valores predeterminados describen una instalación nueva sin cambios; tras modificarlos, rige el valor que se muestra.

**Tabla 1  Ajustes de un vistazo**

| Grupo | Opción | Predeterminado | Efecto |
| --- | --- | --- | --- |
| General | Tema | Seguir sistema | Seguir sistema, forzar claro, forzar oscuro |
| General | Idioma | Seguir sistema | 52 idiomas; debug_keys al final de la lista muestra las claves en bruto para diagnóstico |
| General | Escala de interfaz | Media (1,0x) | Ajusta texto y controles entre 0,3x y 2,5x |
| General | Forma de pantalla | Auto | Detecta reloj redondo o cuadrado; se puede forzar |
| General | Margen inferior | No | Sube el contenido en relojes redondos con mentón, como el Moto 360 |
| General | Color dinámico | Sí | Sigue el fondo o la esfera (Android 12 y posteriores) |
| General | Activar atrás interno | Sí | Sí bloquea el atrás del sistema y usa el deslizamiento; no restaura el atrás del sistema y deja de interceptar el gesto |
| General | Modo bajo rendimiento | Auto | Reduce o corta animaciones; sin ajuste manual lo deciden memoria libre y núcleos |
| General | Pausar servicio inactivo | No | Tras 3 minutos con pantalla apagada pausa la escucha para ahorrar y se reanuda al encender; puede perder envíos entrantes |
| Transferencia | Guardado rápido | Preguntar siempre | Preguntar siempre, aceptar todo, solo favoritos |
| Transferencia | Dispositivos favoritos | Vacío | Memoriza equipos frecuentes y se combina con la opción anterior |
| Transferencia | Guardar en galería | Sí | Coloca fotos y vídeos recibidos en la galería |
| Transferencia | Guardar en historial | Sí | Escribe lo recibido en el historial |
| Red y automatización | Activar HTTPS | No | Transferencia por TLS; equipos muy antiguos solo admiten TLS 1.0 y pueden fallar |
| Red y automatización | Puerto | 53317 | Puerto del servicio; tras cambiarlo, emisor y receptor deben coincidir |
| Red y automatización | Activar transferencia web | Sí | Permite al navegador acceder a la dirección local del servicio |
| Red y automatización | URL web | — | Muestra dirección y código QR; el enlace se puede copiar |
| Red y automatización | Compartir descargas | No | Permite que equipos del LAN obtengan archivos de la carpeta local vía LocalSend |
| Red y automatización | Inicio automático | No | Arranca el servicio al encender; útil para un receptor permanente |
| Dispositivo y seguridad | Cambiar nombre | Modelo | Nombre que ven los demás equipos |
| Dispositivo y seguridad | Activar PIN | No | El emisor debe introducir el PIN antes de enviar |
| Dispositivo y seguridad | Cambiar PIN | 0000 | Cuatro dígitos; cámbielo por uno que solo usted conozca |
| Dispositivo y seguridad | Bloquear extensiones peligrosas | exe,bat,cmd,vbs,sh | Los archivos con estas extensiones se rechazan |
| Dispositivo y seguridad | Listas blanca y negra | Vacío | Pulsación larga en un equipo para bloquearlo o añadirlo a la lista blanca |
| Modo geek | Instalación silenciosa (beta) | No | Instala automáticamente un APK recibido mediante Root o Shizuku |
| Otros | Copia / restauración | — | Exporta los ajustes al portapapeles o los importa desde él |
| Otros | Acerca de WatchSend | — | Versión, protocolo y agradecimientos |

## 9 Problemas frecuentes

**Tabla 2  Síntomas y soluciones**

| Síntoma | Qué hacer |
| --- | --- |
| La lista sigue en «sin dispositivos, pulse escanear» | Compruebe que la otra parte inició el servicio y está en la misma Wi-Fi; desactive el aislamiento AP del router; reintente el escaneo; si sigue, introduzca la IP a mano |
| Todos los envíos de la otra parte devuelven 400 | Suele ser un campo de protocolo o un desajuste de HTTPS: desactive HTTPS en ambos, o actualice ambos a la misma versión y reintente, y lea el código en Registro |
| El navegador no abre la página web | Confirme servicio iniciado, puerto libre y misma subred; algunos routers bloquean el tráfico entre equipos |
| La tecla Atrás no responde o solo muestra un aviso | El atrás interno está activo; desactívelo y el atrás del sistema vuelve a la normalidad |
| Texto o botones recortados por el borde redondo | Active la optimización de pantalla redonda, añada margen inferior si hace falta y baje la escala un paso |
| No llega nada tras un rato con la pantalla apagada | Compruebe si «Pausar servicio inactivo» está activo (predeterminado no) y si el sistema optimiza la batería de WatchSend |
| La batería cae rápido durante la transferencia | Active el modo bajo rendimiento; por debajo del 15 % y sin carga la aplicación ya limita a 512 KB/s |

## 10 Seguridad, energía y límites conocidos

- El HTTP en claro es lo predeterminado y solo sirve para LAN de confianza. Para cifrar, active HTTPS y verifique la huella del certificado de la otra parte.
- El PIN vale 0000 de fábrica; ponga sus cuatro dígitos. Cross-Net usa un código de un solo uso de seis cifras que caduca al usarlo.
- Las extensiones peligrosas exe, bat, cmd, vbs y sh se bloquean por defecto; las listas blanca y negra se gestionan con pulsación larga en el equipo.
- El ahorro tiene tres niveles: el modo bajo rendimiento corta casi todas las animaciones y limita a 2 MB/s; por debajo del 15 % de batería sin carga, a 512 KB/s; tras 3 minutos con pantalla apagada el servicio se pausa (desactivado por defecto y aplazado durante una transferencia).
- Límites conocidos: Cross-Net depende de servicios de terceros y no garantiza estabilidad; la instalación silenciosa depende de la ROM; algunos relojes antiguos no tienen motor de vibración ni corona; extracción y gestos solo se han comprobado en pantallas reales pequeñas.
- El proyecto es de licencia MIT, creado con ayuda de IA y revisado y ajustado en dispositivo; se ofrece tal cual, sin garantía de idoneidad.

---

*WatchSend · MIT License · https://github.com/styisme/watchsend*
