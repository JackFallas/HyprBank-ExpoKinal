# HyprBank-ExpoKinal
Proyecto de Simulacion de una banca en linea hecha por alumnos de kinal

Guia compilacion de Maven

cd C:\GitHub\ExpoKinal\HyprBank-ExpoKinal\HyprBank-ExpoKinal 

verificar que la aplicacion este sin errores de compilacion
mvn clean install

correr la aplicacion
java -jar target/hyprbank-0.0.1-SNAPSHOT.jar

datos de inicio de sesion

correo:
adminhyprbank@gmail.com o admin@admin.com

contraseña:
HyprCodeBank17$ o admin123

guia de instalacion de Tailwind

npm install tailwindcss @tailwindcss/cli

Crear un input.css en resourse/static/css
colocar al input.css
@import "tailwindcss";

ejecutar tailwind local:
npx tailwindcss -i ./src/main/resources/static/css/input.css -o ./src/main/resources/static/css/output.css --watch
