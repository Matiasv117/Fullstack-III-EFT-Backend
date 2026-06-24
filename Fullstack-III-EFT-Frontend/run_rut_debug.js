function dvFromNumber(numeros) {
  let suma = 0;
  let multiplicador = 2;
  for (let i = numeros.length - 1; i >= 0; i--) {
    const digit = parseInt(numeros[i], 10);
    suma += digit * multiplicador;
    multiplicador = multiplicador === 9 ? 2 : multiplicador + 1;
  }
  const dvCalculado = 11 - (suma % 11);
  const dvEsperado = dvCalculado === 11 ? '0' : dvCalculado === 10 ? 'K' : String(dvCalculado);
  return { suma, dvCalculado, dvEsperado };
}

const nums = ['21437351','12345678','2143735'];
for (const n of nums) {
  console.log('number:', n, dvFromNumber(n));
}

