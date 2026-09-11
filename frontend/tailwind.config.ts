import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        brand: {
          50: "#eef7ff",
          100: "#d9ecff",
          200: "#baddff",
          300: "#8bc8ff",
          400: "#54a8ff",
          500: "#2d82f3",
          600: "#1e63d1",
          700: "#194eaa",
          800: "#194388",
          900: "#1b396f",
        },
        surface: {
          DEFAULT: "#f4f8fc",
          muted: "#e8eff7",
          strong: "#d7e4f3",
        },
      },
      boxShadow: {
        soft: "0 18px 40px rgba(25, 66, 123, 0.08)",
        card: "0 14px 32px rgba(13, 39, 80, 0.08)",
      },
      borderRadius: {
        "4xl": "2rem",
      },
      fontFamily: {
        sans: ["'Space Grotesk'", "'Segoe UI Variable Display'", "sans-serif"],
        display: ["'Fraunces'", "'Georgia'", "serif"],
      },
      keyframes: {
        float: {
          "0%, 100%": { transform: "translateY(0px)" },
          "50%": { transform: "translateY(-10px)" },
        },
        shimmer: {
          "0%": { backgroundPosition: "0% 50%" },
          "100%": { backgroundPosition: "100% 50%" },
        },
      },
      animation: {
        float: "float 4s ease-in-out infinite",
        shimmer: "shimmer 10s linear infinite alternate",
      },
    },
  },
  plugins: [],
} satisfies Config;
