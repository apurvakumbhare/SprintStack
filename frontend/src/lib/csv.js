export function downloadCsv(filename, headers, rows) {
  const escapeCell = value => {
    let text = String(value ?? '')
    if (/^[\s]*[=+@\-\t\r]/.test(text)) text = `'${text}`
    return `"${text.replaceAll('"', '""')}"`
  }
  const content = [headers, ...rows].map(row => row.map(escapeCell).join(',')).join('\r\n')
  const url = URL.createObjectURL(new Blob([`\uFEFF${content}`], { type: 'text/csv;charset=utf-8' }))
  const link = document.createElement('a')
  const safeName = filename.normalize('NFKD').replace(/[^a-zA-Z0-9.-]+/g, '-').replace(/^-|-$/g, '').toLowerCase() || 'export.csv'
  link.href = url
  link.download = safeName.endsWith('.csv') ? safeName : `${safeName}.csv`
  document.body.append(link)
  link.click()
  link.remove()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}
