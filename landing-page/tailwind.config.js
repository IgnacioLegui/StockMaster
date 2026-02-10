/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                background: '#09090b', // zinc-950
                surface: '#18181b', // zinc-900
                primary: '#2563eb', // blue-600
                secondary: '#f97316', // orange-500
                text: '#f4f4f5', // zinc-100
                textMuted: '#a1a1aa', // zinc-400
            },
            fontFamily: {
                sans: ['Inter', 'system-ui', 'sans-serif'],
            },
        },
    },
    plugins: [],
}
