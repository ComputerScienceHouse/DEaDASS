const ESLintPlugin = require("eslint-webpack-plugin");
const webpack = require("webpack");

module.exports = {
  target: "node",
  entry: "./src/main.ts",
  mode: "development",
  plugins: [
    new ESLintPlugin({
      extensions: [".ts", ".tsx"],
    }),
    new webpack.IgnorePlugin({ resourceRegExp: /^pg-native$/ }),
  ],
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        exclude: /node_modules/,
        use: {
          loader: "ts-loader",
          options: {
            transpileOnly: true,
          },
        },
      },
    ],
  },
  resolve: {
    extensions: [".tsx", ".ts", ".js"],
  },
  output: {
    path: __dirname + "/dist",
    filename: "bundle.js",
  },
  externals: {
    "saslprep": "require('saslprep')"
  },
};
