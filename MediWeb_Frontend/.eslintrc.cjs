module.exports = {
  root: true,
  env: { es2021: true, node: true, jest: true },
  extends: [
    'eslint:recommended',
    'plugin:react/recommended',
    'plugin:react-hooks/recommended',
    'plugin:react-native/all',
    'plugin:@typescript-eslint/recommended',
    'prettier'
  ],
  parser: '@typescript-eslint/parser',
  parserOptions: { ecmaFeatures: { jsx: true }, ecmaVersion: 'latest', sourceType: 'module' },
  plugins: ['react', 'react-hooks', 'react-native', '@typescript-eslint', 'import'],
  settings: {
    react: { version: 'detect' },
    'import/resolver': {
      node: { extensions: ['.js', '.jsx', '.ts', '.tsx'] },
    },
  },
  rules: {
    'react/prop-types': 'off',
    'react-native/no-inline-styles': 'warn',
    '@typescript-eslint/no-explicit-any': 'warn',
    'import/order': [
      'warn',
      {
        groups: ['builtin', 'external', 'internal', 'parent', 'sibling', 'index'],
        'newlines-between': 'always',
        alphabetize: { order: 'asc', caseInsensitive: true },
      },
    ],
  },
  ignorePatterns: ['dist', 'node_modules']
};
