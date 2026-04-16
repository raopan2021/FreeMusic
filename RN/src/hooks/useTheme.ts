/**
 * 主题 Hook
 */

import {useMemo} from 'react';
import {useColorScheme} from 'react-native';
import {useMusicStore} from '../store/musicStore';

export interface ThemeColors {
  background: string;
  surface: string;
  surfaceVariant: string;
  primary: string;
  text: string;
  textSecondary: string;
  textTertiary: string;
  border: string;
  accent: string;
}

const darkColors: ThemeColors = {
  background: '#1a1a1a',
  surface: '#252525',
  surfaceVariant: '#2a2a2a',
  primary: '#6366F1',
  text: '#ffffff',
  textSecondary: '#888888',
  textTertiary: '#666666',
  border: '#333333',
  accent: '#6366F1',
};

const lightColors: ThemeColors = {
  background: '#f5f5f5',
  surface: '#ffffff',
  surfaceVariant: '#f0f0f0',
  primary: '#6366F1',
  text: '#1a1a1a',
  textSecondary: '#666666',
  textTertiary: '#999999',
  border: '#e0e0e0',
  accent: '#6366F1',
};

export function useTheme(): ThemeColors {
  const systemColorScheme = useColorScheme();
  const settings = useMusicStore(state => state.settings);

  return useMemo(() => {
    const theme = settings.theme;

    if (theme === 'system') {
      return systemColorScheme === 'dark' ? darkColors : lightColors;
    }

    return theme === 'dark' ? darkColors : lightColors;
  }, [settings.theme, systemColorScheme]);
}

export function useIsDarkMode(): boolean {
  const systemColorScheme = useColorScheme();
  const theme = useMusicStore(state => state.settings.theme);

  if (theme === 'system') {
    return systemColorScheme === 'dark';
  }

  return theme === 'dark';
}
