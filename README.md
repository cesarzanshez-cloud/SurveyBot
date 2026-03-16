# 🤖 SurveyBot — Llenador automático de encuestas con IA

## ¿Qué hace esta app?
SurveyBot usa el **Servicio de Accesibilidad** de Android para leer las preguntas
de cualquier app de encuestas y responderlas automáticamente usando **Claude AI**.

Soporta:
- ✅ Preguntas de texto libre
- ✅ Opciones múltiples
- ✅ Escalas numéricas (1-5, 1-10, etc.)
- ✅ Cualquier tipo de botón "Siguiente / Continuar"

---

## 📋 PASOS PARA COMPILAR E INSTALAR

### Paso 1 — Instala Android Studio (gratis)
Descarga en: https://developer.android.com/studio
Instala normalmente en tu PC o Mac.

### Paso 2 — Abre el proyecto
1. Abre Android Studio
2. Haz clic en **"Open"** (o File > Open)
3. Selecciona la carpeta **SurveyBot** (esta carpeta)
4. Espera que Android Studio descargue las dependencias (tarda 2-5 min la primera vez)

### Paso 3 — Compila la APK
1. Ve al menú: **Build > Build Bundle(s) / APK(s) > Build APK(s)**
2. Espera ~1 minuto
3. Haz clic en **"locate"** en la notificación que aparece
4. Encontrarás el archivo: `app/build/outputs/apk/debug/app-debug.apk`

### Paso 4 — Instala en tu Android
Método A (cable USB):
1. Conecta tu celular al PC
2. Activa "Depuración USB" en tu celular (Ajustes > Opciones de desarrollador)
3. En Android Studio haz clic en el botón ▶️ (Run)

Método B (WhatsApp / Drive):
1. Copia el archivo .apk a tu celular
2. Ábrelo desde el administrador de archivos
3. Acepta "Instalar desde fuentes desconocidas" si te lo pide

---

## ⚙️ CONFIGURACIÓN EN EL CELULAR

### 1. Ingresa tu API Key
- Abre SurveyBot
- Pega tu API Key de Anthropic (console.anthropic.com)
- Presiona "Guardar configuración"

### 2. Activa el servicio
- Presiona "Activar Servicio de Accesibilidad"
- Busca "SurveyBot" en la lista
- Actívalo y acepta el permiso

### 3. Usa tu app de encuestas
- Abre tu app de encuestas normalmente
- SurveyBot detectará las preguntas y las responderá solo
- Verás cómo avanza automáticamente ✨

---

## 🔑 Cómo conseguir API Key gratis
1. Ve a https://console.anthropic.com
2. Crea una cuenta (solo necesitas email)
3. Ve a "API Keys" > "Create Key"
4. Copia la clave (empieza con sk-ant-...)

Los $5 USD de crédito gratuito alcanzan para miles de encuestas.

---

## 💡 Personaliza tu perfil
En la app puedes escribir cómo eres para que la IA responda como tú:

Ejemplo:
> "Soy mujer de 28 años, trabajo en oficina, vivo en ciudad mediana,
>  uso tecnología a diario, compro online frecuentemente."

Cuanto más detallado sea el perfil, más naturales serán las respuestas.

---

## ⚠️ Nota importante
Este bot es para uso personal. Revisa los términos de servicio
de la app de encuestas que uses.
