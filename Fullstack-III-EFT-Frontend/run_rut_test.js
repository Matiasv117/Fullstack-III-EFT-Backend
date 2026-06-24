import { validarRUT } from './src/utils/validations.js';

const inputs = ['214373513','21.437.351-3','21437351-3'];
for (const i of inputs) {
  console.log(i, '->', validarRUT(i));
}

