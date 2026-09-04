'use client'

import { useSearchParams } from 'next/navigation'
import { CheckCircle2 } from 'lucide-react'
import Link from 'next/link'

export default function SuccessPage() {
  const searchParams = useSearchParams()
  const id = searchParams.get('id') || 'N/A'

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4">
      <div className="bg-white p-8 rounded-xl shadow-lg max-w-md w-full text-center border border-gray-100">
        <div className="flex justify-center mb-4">
          <CheckCircle2 className="h-20 w-20 text-green-500" />
        </div>
        <h1 className="text-2xl font-bold text-gray-900 mb-2">¡Consentimiento enviado correctamente!</h1>
        <p className="text-gray-600 mb-6">Su registro ha sido recibido y esta información ha sido registrada exitosamente.</p>
        
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-4 mb-6">
          <p className="text-sm text-gray-500 uppercase tracking-wide">Número de consentimiento</p>
          <p className="text-xl font-bold text-orange-600">{id}</p>
        </div>

        <Link href="/" className="inline-block w-full py-3 px-4 bg-gray-900 text-white font-medium rounded-md hover:bg-gray-800 transition-colors">
          Volver al Inicio
        </Link>
      </div>
    </div>
  )
}
