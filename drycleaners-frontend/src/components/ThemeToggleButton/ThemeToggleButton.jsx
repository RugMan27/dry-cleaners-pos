import { useTheme } from '../../hooks/useTheme.js';

export default function ThemeToggleButton() {
    const { theme, toggleTheme } = useTheme();

    return (
        <button onClick={toggleTheme}>
            {theme === 'light' ? '🌙 Dark Mode' : '☀️ Light Mode'}
        </button>
    );
}
