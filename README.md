# Facial Recognition App

Aplicación Android para registrar e identificar personas mediante reconocimiento facial. La detección y comparación de rostros se realiza en un servicio externo (HuggingFace), la app solo se encarga de capturar las fotos y mostrar los resultados.

## Funcionalidades

- **Registrar persona:** toma o sube 5 fotos de una persona, asígnale un nombre y el sistema genera su perfil facial.
- **Verificar persona:** toma o sube una foto y la app indica si la persona está registrada o no.
- **Ver registrados:** muestra la lista de personas que ya están en el sistema.

## Requisitos

- Android Studio
- Android SDK (se instala automáticamente con Android Studio)
- Archivo `secrets.properties` en la raíz del proyecto con la API key del servicio

```
API_KEY=tu_api_key_aqui
```

## Cómo ejecutar

1. Clona el repositorio:
   ```bash
   git clone https://github.com/GuidoCh23/facial-recognition-app.git
   ```

2. Abre la carpeta en Android Studio y espera que Gradle sincronice.

3. Crea el archivo `secrets.properties` en la raíz con la API key.

4. Conecta un dispositivo o inicia un emulador y presiona **Run**.
