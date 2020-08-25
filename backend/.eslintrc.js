module.exports = {
  env: {
    commonjs: true,
    es6: true,
    node: true,
  },
  parser: "@typescript-eslint/parser",
  extends: [
    "eslint:recommended",
    "plugin:@typescript-eslint/recommended",
    "plugin:@typescript-eslint/recommended-requiring-type-checking",
    "prettier/@typescript-eslint",
    "plugin:prettier/recommended",
  ],
  globals: {
    Atomics: "readonly",
    SharedArrayBuffer: "readonly",
  },
  parserOptions: {
    ecmaVersion: 2020,
    tsconfigRootDir: __dirname,
    project: ["./tsconfig.json"],
  },
  rules: {
    "@typescript-eslint/explicit-function-return-type": ["error"],
    "@typescript-eslint/array-type": ["error", {"default":"array-simple"}],
    "@typescript-eslint/explicit-member-accessibility": ["error"],
    "@typescript-eslint/prefer-includes": ["error"],
    "@typescript-eslint/prefer-readonly": ["error"],
    "@typescript-eslint/type-annotation-spacing": ["error", {"before": false, "after": true}]
  },
};
