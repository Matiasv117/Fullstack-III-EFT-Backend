const PDFDocument = require('pdfkit');
const fs = require('fs');
const path = require('path');

const BASE = 'C:\\Users\\ibane\\Desktop\\Fullstack\\Fullstack-III-EFT-Backend';
const SERVICES = [
  { name: 'Eureka Server', dir: 'eureka-server', port: 8761 },
  { name: 'API Gateway', dir: 'api-gateway', port: 8080 },
  { name: 'BFF', dir: 'bff', port: 8097 },
  { name: 'Auth Service', dir: 'ms-auth', port: 8087 },
  { name: 'Patient Management', dir: 'ms-gestionpacientes', port: 8083 },
  { name: 'Optimization', dir: 'ms-optimizacion', port: 8084 },
  { name: 'Notifications', dir: 'ms-notificaciones', port: 8085 },
  { name: 'Progress', dir: 'ms-progreso', port: 8086 },
  { name: 'Audit', dir: 'ms-auditoria', port: 8088 },
];

function parseCSV(filePath) {
  if (!fs.existsSync(filePath)) return null;
  const content = fs.readFileSync(filePath, 'utf-8');
  const lines = content.trim().split('\n');
  if (lines.length < 2) return null;
  const headers = lines[0].split(',');
  const rows = lines.slice(1).map(l => {
    const vals = l.split(',');
    const obj = {};
    headers.forEach((h, i) => { obj[h.trim()] = vals[i]?.trim(); });
    return obj;
  });
  return rows;
}

function computeTotals(rows) {
  const totals = {
    INSTRUCTION_MISSED: 0, INSTRUCTION_COVERED: 0,
    BRANCH_MISSED: 0, BRANCH_COVERED: 0,
    LINE_MISSED: 0, LINE_COVERED: 0,
    METHOD_MISSED: 0, METHOD_COVERED: 0,
  };
  for (const r of rows) {
    for (const k of Object.keys(totals)) {
      totals[k] += parseInt(r[k] || '0', 10);
    }
  }
  return totals;
}

function pct(covered, missed) {
  const total = covered + missed;
  return total === 0 ? 0 : Math.round((covered / total) * 10000) / 100;
}

function generatePDF() {
  const doc = new PDFDocument({ margin: 40, size: 'A4' });
  const outPath = path.join(BASE, 'coverage-report-backend.pdf');
  const stream = fs.createWriteStream(outPath);
  doc.pipe(stream);

  const redNorte = '#C62828';
  const gray = '#333333';
  const lightGray = '#F5F5F5';
  const borderColor = '#DDDDDD';

  function pageHeader() {
    doc.fontSize(22).font('Helvetica-Bold').fillColor(redNorte)
       .text('RedNorte - Informe de cobertura', { align: 'center' });
    doc.fontSize(10).font('Helvetica').fillColor(gray)
       .text(`Backend - JaCoCo Coverage Report`, { align: 'center' });
    doc.fontSize(8).fillColor('#666')
       .text(`Generado: ${new Date().toLocaleDateString('es-CL', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })}`, { align: 'center' });
    doc.moveDown(0.7);
  }

  // --- Title page ---
  pageHeader();
  doc.moveDown(1.5);

  doc.fontSize(16).font('Helvetica-Bold').fillColor(redNorte)
     .text('Resumen General', { underline: false });
  doc.moveDown(0.5);

  // Draw horizontal line
  doc.moveTo(40, doc.y).lineTo(550, doc.y).strokeColor(redNorte).lineWidth(1).stroke();
  doc.moveDown(0.5);

  // Collect data
  const serviceData = [];
  let grandInstrMissed = 0, grandInstrCovered = 0;
  let grandBranchMissed = 0, grandBranchCovered = 0;
  let grandLineMissed = 0, grandLineCovered = 0;
  let grandMethodMissed = 0, grandMethodCovered = 0;

  for (const svc of SERVICES) {
    const csvPath = path.join(BASE, svc.dir, 'target', 'site', 'jacoco', 'jacoco.csv');
    const rows = parseCSV(csvPath);
    if (!rows) {
      serviceData.push({ ...svc, hasData: false });
      continue;
    }
    const t = computeTotals(rows);
    serviceData.push({ ...svc, hasData: true, ...t,
      instrPct: pct(t.INSTRUCTION_COVERED, t.INSTRUCTION_MISSED),
      branchPct: pct(t.BRANCH_COVERED, t.BRANCH_MISSED),
      linePct: pct(t.LINE_COVERED, t.LINE_MISSED),
      methodPct: pct(t.METHOD_COVERED, t.METHOD_MISSED),
    });
    grandInstrMissed += t.INSTRUCTION_MISSED;
    grandInstrCovered += t.INSTRUCTION_COVERED;
    grandBranchMissed += t.BRANCH_MISSED;
    grandBranchCovered += t.BRANCH_COVERED;
    grandLineMissed += t.LINE_MISSED;
    grandLineCovered += t.LINE_COVERED;
    grandMethodMissed += t.METHOD_MISSED;
    grandMethodCovered += t.METHOD_COVERED;
  }

  // Grand totals row
  const grandInstrPct = pct(grandInstrCovered, grandInstrMissed);
  const grandBranchPct = pct(grandBranchCovered, grandBranchMissed);
  const grandLinePct = pct(grandLineCovered, grandLineMissed);
  const grandMethodPct = pct(grandMethodCovered, grandMethodMissed);

  // Summary metrics boxes
  const boxW = 117;
  const boxH = 60;
  const yStart = doc.y;
  const metrics = [
    { label: 'Instrucciones', pct: grandInstrPct },
    { label: 'Ramas', pct: grandBranchPct },
    { label: 'Líneas', pct: grandLinePct },
    { label: 'Métodos', pct: grandMethodPct },
  ];

  metrics.forEach((m, i) => {
    const x = 40 + i * (boxW + 8);
    doc.roundedRect(x, yStart, boxW, boxH, 5).fillAndStroke('#FAFAFA', borderColor);
    doc.fontSize(9).font('Helvetica').fillColor(gray)
       .text(m.label, x + 5, yStart + 8, { width: boxW - 10, align: 'center' });
    const color = m.pct >= 85 ? '#2E7D32' : m.pct >= 70 ? '#F57F17' : '#C62828';
    doc.fontSize(22).font('Helvetica-Bold').fillColor(color)
       .text(`${m.pct}%`, x + 5, yStart + 25, { width: boxW - 10, align: 'center' });
  });

  doc.y = yStart + boxH + 10;

  // --- Table per service ---
  doc.addPage();
  pageHeader();
  doc.fontSize(14).font('Helvetica-Bold').fillColor(redNorte)
     .text('Cobertura por microservicio', { underline: false });
  doc.moveDown(0.5);
  doc.moveTo(40, doc.y).lineTo(550, doc.y).strokeColor(redNorte).lineWidth(1).stroke();
  doc.moveDown(0.5);

  // Table header
  const cols = [
    { label: 'Servicio', x: 40, w: 110 },
    { label: 'Puerto', x: 155, w: 45 },
    { label: 'Instr.', x: 205, w: 55, align: 'center' },
    { label: 'Ramas', x: 265, w: 55, align: 'center' },
    { label: 'Líneas', x: 325, w: 55, align: 'center' },
    { label: 'Métodos', x: 385, w: 55, align: 'center' },
    { label: 'Estado', x: 445, w: 100, align: 'center' },
  ];

  function drawTableRow(y, data, isHeader, bg) {
    if (bg) {
      doc.rect(40, y, 510, 18).fill(bg);
    }
    cols.forEach(c => {
      const val = data[c.label];
      doc.font(isHeader ? 'Helvetica-Bold' : 'Helvetica')
         .fontSize(isHeader ? 8 : 7.5)
         .fillColor(isHeader ? '#FFFFFF' : gray)
         .text(val || '', c.x, y + 3, { width: c.w, align: c.align || 'left' });
    });
    return y + 18;
  }

  let y = doc.y;
  // Header row
  doc.rect(40, y, 510, 18).fill(redNorte);
  const hdrData = {};
  cols.forEach(c => { hdrData[c.label] = c.label; });
  y = drawTableRow(y, hdrData, true);

  for (const svc of serviceData) {
    if (!svc.hasData) {
      const d = {};
      cols.forEach(c => { d[c.label] = c.label === 'Servicio' ? svc.name : c.label === 'Estado' ? 'Sin datos' : '-'; });
      y = drawTableRow(y, d, false, y % 36 === 0 ? lightGray : null);
      continue;
    }
    const status = svc.linePct >= 85 ? 'OK' : svc.linePct >= 70 ? 'Media' : 'Baja';
    const d = {};
    cols.forEach(c => {
      switch (c.label) {
        case 'Servicio': d[c.label] = svc.name; break;
        case 'Puerto': d[c.label] = String(svc.port); break;
        case 'Instr.': d[c.label] = `${svc.instrPct}%`; break;
        case 'Ramas': d[c.label] = `${svc.branchPct}%`; break;
        case 'Líneas': d[c.label] = `${svc.linePct}%`; break;
        case 'Métodos': d[c.label] = `${svc.methodPct}%`; break;
        case 'Estado': d[c.label] = status; break;
      }
    });
    y = drawTableRow(y, d, false, y % 36 === 0 ? lightGray : null);
  }

  // Legend
  y += 10;
  doc.fontSize(8).font('Helvetica').fillColor(gray)
     .text('Estado: OK ≥85%  |  Media 70-84%  |  Baja <70%', 40, y);

  // --- Per-service detail ---
  for (const svc of serviceData) {
    if (!svc.hasData) continue;

    doc.addPage();
    pageHeader();
    doc.fontSize(13).font('Helvetica-Bold').fillColor(redNorte)
       .text(`${svc.name} (puerto ${svc.port})`, { underline: false });
    doc.moveDown(0.3);
    doc.moveTo(40, doc.y).lineTo(550, doc.y).strokeColor(redNorte).lineWidth(1).stroke();
    doc.moveDown(0.3);

    // Metrics bars
    const metrics2 = [
      { label: 'Instrucciones', pct: svc.instrPct, missed: svc.INSTRUCTION_MISSED, covered: svc.INSTRUCTION_COVERED },
      { label: 'Ramas', pct: svc.branchPct, missed: svc.BRANCH_MISSED, covered: svc.BRANCH_COVERED },
      { label: 'Líneas', pct: svc.linePct, missed: svc.LINE_MISSED, covered: svc.LINE_COVERED },
      { label: 'Métodos', pct: svc.methodPct, missed: svc.METHOD_MISSED, covered: svc.METHOD_COVERED },
    ];

    for (const m of metrics2) {
      doc.fontSize(9).font('Helvetica').fillColor(gray)
         .text(`${m.label}: ${m.pct}% (${m.covered}/${m.covered + m.missed})`, 40, doc.y, { continued: false });
      doc.moveDown(0.15);
      const barW = 280;
      const barH = 12;
      const barX = 100;
      const barY = doc.y;
      doc.roundedRect(barX, barY, barW, barH, 3).fill('#E0E0E0');
      if (m.pct > 0) {
        const fillW = Math.max(barW * m.pct / 100, 4);
        const color = m.pct >= 85 ? '#2E7D32' : m.pct >= 70 ? '#F57F17' : '#C62828';
        doc.roundedRect(barX, barY, fillW, barH, 3).fill(color);
      }
      doc.fontSize(9).font('Helvetica-Bold').fillColor('#FFFFFF')
         .text(`${m.pct}%`, barX + 5, barY + 1, { width: 270, align: 'left' });
      doc.y = barY + barH + 8;
    }
  }

  // --- Footer ---
  doc.addPage();
  pageHeader();
  doc.moveDown(0.5);
  doc.fontSize(11).font('Helvetica').fillColor(gray)
     .text('Metodología', { underline: false });
  doc.moveDown(0.3);
  doc.fontSize(9).fillColor('#555')
     .text('• Instrucciones: cobertura del bytecode ejecutado.')
     .text('• Ramas: cobertura de caminos condicionales (if/else, switch).')
     .text('• Líneas: cobertura de líneas de código fuente ejecutadas.')
     .text('• Métodos: cobertura de métodos invocados durante los tests.')
     .text('')
     .text('Reporte generado con JaCoCo (EclEmma) + pdfkit (Node.js).')
     .text('')
     .text(`Total de servicios analizados: ${serviceData.filter(s => s.hasData).length}/${SERVICES.length}`)
     .text(`Cobertura general - Líneas: ${grandLinePct}% | Instrucciones: ${grandInstrPct}% | Ramas: ${grandBranchPct}% | Métodos: ${grandMethodPct}%`)
     .text('')
     .text('RedNorte - Sistema de Salud © 2026')
     .text(new Date().toLocaleString('es-CL'));

  doc.end();
  stream.on('finish', () => {
    console.log(`PDF generado: ${outPath}`);
    console.log(`Tamaño: ${(fs.statSync(outPath).size / 1024).toFixed(1)} KB`);
  });
}

generatePDF();
