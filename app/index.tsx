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

  return (
    <View style={styles.container} pointerEvents='none'>
      <StatusBar hidden={true} />
      <WebView 
        source={{ uri: 'https://galaxy.signage.me/installplayer' }} 
        style={styles.webview}
        javaScriptEnabled={true}
        domStorageEnabled={true}
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