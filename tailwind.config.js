/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    // ¡MUY IMPORTANTE! Asegúrate de que estas rutas sean correctas.
    // Escanea todos los archivos .html dentro de src/main/resources/templates/ y sus subcarpetas
    "./src/main/resources/templates/**/*.html",
    // Escanea todos los archivos .html dentro de src/main/resources/static/ y sus subcarpetas
    "./src/main/resources/static/**/*.html",
    // Si tienes archivos JavaScript que dinámicamente añaden clases de Tailwind, inclúyelos también:
    "./src/main/resources/static/js/**/*.js",
    // Si tienes clases de Tailwind en archivos Java (por ejemplo, en strings para generar HTML),
    // también podrías necesitarlas, aunque es menos común:
    // "./src/main/java/**/*.java",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}