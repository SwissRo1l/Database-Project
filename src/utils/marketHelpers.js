export function getSales24(item) {
  return Number(item?.sales24 ?? item?.sales ?? item?.volume ?? item?.tradeCount ?? item?.transactions ?? item?.sold ?? 0) || 0
}

export function getChangeVal(item) {
  return Number(item?.change ?? item?.changePercent ?? item?.percentChange ?? item?.priceChangePercent ?? 0) || 0
}

export function normalizeItemFields(item) {
  if (!item) return item
  const normalized = { ...item }
  const sales = getSales24(item)
  const change = getChangeVal(item)
  normalized.sales24 = sales
  normalized.change = change
  return normalized
}

export default {
  getSales24,
  getChangeVal,
  normalizeItemFields
}
