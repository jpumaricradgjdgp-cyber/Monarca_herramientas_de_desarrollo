# Ejercicio controlado de conflicto de merge

## Estado y propósito

No se documentó un conflicto real en las fusiones del proyecto revisadas. Esta práctica reproduce el ejercicio del Manual_Practico_Merge_Conflict.pdf para que el equipo pueda ejecutar, comprender y explicar la resolución. Es un laboratorio separado: no debe presentarse como un incidente ocurrido en Monarca.

## Ejecución en la laptop compartida

Desde la raíz del repositorio, en PowerShell:

```powershell
.\scripts\Practicar-Conflicto.ps1 -Participante "Nombre de quien realiza la práctica"
```

Si PowerShell bloquea scripts, puede ejecutarse el archivo mediante una sesión dedicada:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Practicar-Conflicto.ps1 -Participante "Nombre de quien realiza la práctica"
```

El script crea una carpeta nueva dentro del directorio temporal del usuario. Dentro hay un repositorio de laboratorio y una carpeta de evidencias. No altera las ramas de Monarca ni sube nada a GitHub.

1. Crea main con `mensaje.txt` y un commit inicial.
2. Crea feature/saludo y modifica la misma línea.
3. Vuelve a main y escribe otro saludo en esa línea.
4. Intenta el merge y verifica que Git informa un conflicto en mensaje.txt.
5. Guarda el contenido con marcadores como evidencia.
6. Integra ambos saludos, elimina los marcadores y confirma la resolución.
7. Comprueba que el repositorio de laboratorio termina limpio y guarda el historial.

La resolución del script es automática y explica una decisión didáctica: conservar ambos saludos. Cada integrante debe revisar el conflicto y poder justificar esa decisión; ejecutar el script por sí solo no sustituye comprender la práctica.

## Archivos generados

- `transcripcion.txt`: comandos y resultados del ejercicio.
- `conflicto.txt`: contenido con marcadores antes de resolver.
- `resolucion.txt`: contenido final integrado.
- `historial.txt`: gráfico de commits y ramas del laboratorio.
- `resultado.json`: fecha, participante declarado y estado del ejercicio.

El script muestra la ubicación final. Revisar esos archivos antes de adjuntarlos en el PR o en el documento del grupo. La transcripción puede contener rutas y el nombre del usuario de Windows: recortar datos personales al compartirla.

## Registro que debe completar el equipo

| Campo | Resultado |
| --- | --- |
| Participante que ejecutó y explicó la práctica | PENDIENTE |
| Fecha real | PENDIENTE |
| Archivo en conflicto | mensaje.txt, en el laboratorio |
| Causa | Ediciones incompatibles de la misma línea desde main y feature/saludo |
| Decisión de resolución | Combinar los dos saludos y eliminar los marcadores |
| Commit de resolución | PENDIENTE: tomar del historial generado |
| Evidencias adjuntas y explicación oral | PENDIENTE |

Preguntas de sustentación: ¿qué causó el conflicto?, ¿cómo señaló Git el archivo?, ¿por qué la resolución conserva ambos cambios?, ¿cómo se comprobó que no quedaron archivos sin resolver?

La comprobación técnica del script por Codex se registra en el PR de preparación. No se registra como ejecución por un estudiante.
