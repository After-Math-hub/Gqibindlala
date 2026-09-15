/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        accent: "#B45309",
        accentLight: "#FEF3C7",
      },
    },
  },
  plugins: [],
};
