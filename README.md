# 🎛️ Osciloscopio API REST

API REST desarrollada con Spring Boot que captura audio en tiempo real desde el micrófono del sistema y expone los datos de señal para su consumo por cualquier cliente web o herramienta como Postman.

---

## 📋 Especificaciones Técnicas

| Parámetro | Valor |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.1.5 |
| Gestor de dependencias | Maven 3.8.4 |
| Servidor embebido | Apache Tomcat 10.1 |
| Puerto por defecto | 8080 |
| Formato de respuesta | JSON |

---

## 📁 Estructura del Proyecto

```
osciloscopio/
├── pom.xml
└── src/
    └── main/
        ├── java/org/example/
        │   ├── Main.java                        # Clase principal Spring Boot
        │   ├── controller/
        │   │   └── OsciloscopioController.java  # Endpoints REST
        │   ├── model/
        │   │   ├── OsciloscopioConfig.java       # Modelo de configuración
        │   │   └── OsciloscopioData.java         # Modelo de datos de señal
        │   └── service/
        │       └── OsciloscopioService.java      # Lógica de captura de audio
        └── resources/
            └── application.properties           # Configuración de la aplicación
```

---

## ⚙️ Requisitos Previos

- Java 17 instalado y configurado en el PATH
- Maven 3.8.4 o superior instalado
- Micrófono disponible en el sistema
- Puerto 8080 disponible

Verificar versiones instaladas:
```bash
java -version
mvn -version
```

---

## 🔧 Compilación

Clonar o descargar el proyecto y desde la raíz del mismo ejecutar:

```bash
# Compilar y empaquetar (genera el JAR en /target)
mvn clean install

# Solo compilar sin ejecutar tests
mvn clean compile
```

El artefacto generado quedará en:
```
target/osciloscopio-1.0-SNAPSHOT.jar
```

---

## 🚀 Formas de Iniciar la Aplicación

### Opción 1: Con Maven (recomendado en desarrollo)
```bash
mvn spring-boot:run
```

### Opción 2: Ejecutando el JAR directamente
```bash
java -jar target/osciloscopio-1.0-SNAPSHOT.jar
```

### Opción 3: Desde IntelliJ IDEA
Abrir la clase `Main.java` y ejecutar con el botón ▶ o con `Shift + F10`.

La aplicación está lista cuando aparece en consola:
```
Tomcat started on port(s): 8080 (http)
Started Main in X seconds
```

---

## 🛑 Formas de Detener la Aplicación

### Detener solo la captura de audio (servidor sigue activo)
```
POST http://localhost:8080/api/osciloscopio/detener
```

### Detener completamente el servidor
```bash
Ctrl + C
```

---

## 🌐 Servicios Expuestos

Base URL: `http://localhost:8080/api/osciloscopio`

---

### GET `/estado`
Retorna si la captura de audio está activa y el sample rate configurado.

**Request:**
```
GET http://localhost:8080/api/osciloscopio/estado
```

**Response:**
```json
{
  "capturando": true,
  "sampleRate": 44100.0
}
```

---

### GET `/config`
Retorna la configuración actual del osciloscopio.

**Request:**
```
GET http://localhost:8080/api/osciloscopio/config
```

**Response:**
```json
{
  "sampleRate": 44100.0,
  "escalaYIndex": 2,
  "msPerDivIndex": 2,
  "congelado": false,
  "offsetX": 0,
  "offsetY": 0
}
```

---

### GET `/datos`
Retorna las muestras de audio actuales del buffer procesadas, junto con la configuración activa, la frecuencia estimada y la amplitud máxima.

**Request:**
```
GET http://localhost:8080/api/osciloscopio/datos
```

**Response:**
```json
{
  "muestras": [0, 1, -2, 3, ...],
  "config": {
    "sampleRate": 44100.0,
    "escalaYIndex": 2,
    "msPerDivIndex": 2,
    "congelado": false,
    "offsetX": 0,
    "offsetY": 0
  },
  "frecuenciaEstimadaHz": 2406.33,
  "amplitudMaxima": 0.0
}
```

**Descripción de campos:**

| Campo | Descripción |
|---|---|
| `muestras` | Lista de hasta 1000 valores de amplitud normalizados (-100 a 100) |
| `frecuenciaEstimadaHz` | Frecuencia estimada por detección de cruces por cero |
| `amplitudMaxima` | Valor máximo de amplitud en la muestra actual |

---

### PUT `/config`
Actualiza la configuración del osciloscopio. Si cambia el `sampleRate`, reinicia la captura automáticamente.

**Request:**
```
PUT http://localhost:8080/api/osciloscopio/config
Content-Type: application/json
```

**Body:**
```json
{
  "sampleRate": 44100.0,
  "escalaYIndex": 1,
  "msPerDivIndex": 2,
  "congelado": false,
  "offsetX": 0,
  "offsetY": 0
}
```

**Valores válidos:**

| Campo | Valores | Descripción |
|---|---|---|
| `sampleRate` | 8000, 16000, 44100, 48000, 96000 | Frecuencia de muestreo en Hz |
| `escalaYIndex` | 0, 1, 2 | 0=0.1V/div, 1=0.5V/div, 2=1.0V/div |
| `msPerDivIndex` | 0, 1, 2, 3, 4 | 0=1ms, 1=5ms, 2=10ms, 3=20ms, 4=50ms |
| `congelado` | true, false | Congela o reanuda la captura |
| `offsetX` | -2000 a 2000 | Desplazamiento horizontal de la señal |
| `offsetY` | -400 a 400 | Desplazamiento vertical de la señal |

**Response:** devuelve la configuración actualizada en el mismo formato del body.

---

### POST `/iniciar`
Inicia la captura de audio desde el micrófono del sistema.

**Request:**
```
POST http://localhost:8080/api/osciloscopio/iniciar
```

**Response:**
```json
{
  "estado": "captura iniciada"
}
```

---

### POST `/detener`
Detiene la captura de audio. El servidor Spring Boot permanece activo.

**Request:**
```
POST http://localhost:8080/api/osciloscopio/detener
```

**Response:**
```json
{
  "estado": "captura detenida"
}
```

---

## 📌 Resumen de Endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/osciloscopio/estado` | Estado de la captura |
| GET | `/api/osciloscopio/config` | Configuración actual |
| GET | `/api/osciloscopio/datos` | Muestras de señal en tiempo real |
| PUT | `/api/osciloscopio/config` | Actualizar configuración |
| POST | `/api/osciloscopio/iniciar` | Iniciar captura de audio |
| POST | `/api/osciloscopio/detener` | Detener captura de audio |
