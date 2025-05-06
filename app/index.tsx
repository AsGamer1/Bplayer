import React, { useLayoutEffect } from 'react';
import { StyleSheet, View, BackHandler, Platform } from 'react-native';
import { WebView } from 'react-native-webview';
import { StatusBar } from 'expo-status-bar';
import * as SystemUI from 'expo-system-ui';
import { useKeepAwake } from 'expo-keep-awake';

export default function App() {
  useKeepAwake();

  useLayoutEffect(() => {
    SystemUI.setBackgroundColorAsync('transparent');

    if (Platform.OS === 'android') {
      const backHandler = BackHandler.addEventListener('hardwareBackPress', () => true);
      return () => backHandler.remove();
    }
  }, []);

  const restartApp = () => {
    BackHandler.exitApp();
  }

  return (
    <View style={styles.container}>
      <StatusBar hidden={true} />
      <WebView 
        source={{ uri: 'https://player.admefy.com/' }} 
        style={styles.webview}
        javaScriptEnabled={true}
        domStorageEnabled={true}
        geolocationEnabled={true}
        onError={() => restartApp()}
        onHttpError={() => restartApp()}
      />
    </View>
  );
}

export const unstable_settings = {
  headerShown: false,
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  webview: {
    flex: 1,
  },
});