// Expo Router használata esetén a bejegyzés fájl csak a provide-erekért felel
import { AuthProvider } from 'contexts/AuthContext';
import { ToastProvider } from 'components/ToastProvider';
import { usePushNotifications } from 'features/notification/usePushNotifications';
import 'styles/App.css';
import { Slot } from 'expo-router';

export default function RootAppProviders() {
  usePushNotifications();
  return (
    <AuthProvider>
      <ToastProvider>
        {/* Slot rendereli az aktuális route-ot a file-based routing szerint */}
        <Slot />
      </ToastProvider>
    </AuthProvider>
  );
}