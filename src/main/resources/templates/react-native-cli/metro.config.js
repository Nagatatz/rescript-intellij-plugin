const { getDefaultConfig, mergeConfig } = require("@react-native/metro-config");

const defaultConfig = getDefaultConfig(__dirname);

// Extend the resolver so Metro picks up ReScript-compiled output (.res.mjs).
const config = {
  resolver: {
    sourceExts: [...defaultConfig.resolver.sourceExts, "mjs"],
  },
};

module.exports = mergeConfig(defaultConfig, config);
