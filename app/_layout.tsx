import { Stack } from 'expo-router';

export default function Layout() {
  return (
    <Stack screenOptions={{ headerShown: false }}>
      {/* Esto asegura que el header esté oculto en todas las pantallas */}
    </Stack>
  );
}