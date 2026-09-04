'use client'

import { useRouter } from 'next/navigation'

export default function LogoutButton() {
  const router = useRouter()

  const handleLogout = async () => {
    await fetch('/api/auth/logout', { method: 'POST' })
    router.push('/admin/login')
    router.refresh()
  }

  return (
    <button 
      onClick={handleLogout}
      className="text-sm font-medium text-gray-500 hover:text-gray-900 border border-gray-300 px-3 py-1 rounded-md"
    >
      Cerrar Sesión
    </button>
  )
}
