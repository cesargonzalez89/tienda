#!/bin/zsh
# Mata cualquier proceso que esté usando el puerto 8080
echo "Buscando procesos en el puerto 8080..."
PIDS=$(lsof -ti :8080)
if [ -n "$PIDS" ]; then
  echo "Matando procesos: $PIDS"
  kill -9 $PIDS
  echo "Puerto 8080 liberado."
else
  echo "No hay procesos usando el puerto 8080."
fi
