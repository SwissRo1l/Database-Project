<<<<<<< HEAD
export function formatPrice(cents) {
  if (cents == null) return '-'
  return (cents / 100).toFixed(2)
}

export function timeAgo(ts) {
  const diff = Date.now() - new Date(ts).getTime()
  const minutes = Math.floor(diff / 60000)
  if (minutes < 60) return `${minutes}m`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h`
  const days = Math.floor(hours / 24)
  return `${days}d`
}
=======
export function formatPrice(cents) {
  if (cents == null) return '-'
  return (cents / 100).toFixed(2)
}

export function timeAgo(ts) {
  const diff = Date.now() - new Date(ts).getTime()
  const minutes = Math.floor(diff / 60000)
  if (minutes < 60) return `${minutes}m`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h`
  const days = Math.floor(hours / 24)
  return `${days}d`
}
>>>>>>> 7e7d29547309042be8bf78076eb4c461e2e49e60
