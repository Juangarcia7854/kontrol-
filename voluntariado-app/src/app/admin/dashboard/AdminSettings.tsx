'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { QRCodeSVG } from 'qrcode.react'
import { AlertTriangle, Upload, X } from 'lucide-react'

export default function AdminSettings({ initialLegalText, initialCompanies, initialLogo }: { initialLegalText: string, initialCompanies: any[], initialLogo: string | null }) {
  const router = useRouter()
  const [legalText, setLegalText] = useState(initialLegalText)
  const [logo, setLogo] = useState<string | null>(initialLogo)
  const [newCompany, setNewCompany] = useState('')
  const [loading, setLoading] = useState(false)
  const [publicUrl, setPublicUrl] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [companyToDelete, setCompanyToDelete] = useState<{id: string, name: string} | null>(null)

  useEffect(() => {
    // Si estamos en localhost, buscamos la IP real de la computadora para que el QR funcione en el celular
    if (window.location.hostname === 'localhost') {
      fetch('/api/admin/ip')
        .then(res => res.json())
        .then(data => {
          setPublicUrl(`http://${data.ip}:${window.location.port || 3000}`)
        })
        .catch(() => setPublicUrl(window.location.origin))
    } else {
      setPublicUrl(window.location.origin)
    }
  }, [])

  const showSuccess = (msg: string) => {
    setSuccessMessage(msg)
    setTimeout(() => setSuccessMessage(''), 3000)
  }

  const handleUpdateLegalText = async () => {
    setLoading(true)
    await fetch('/api/admin/settings', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'updateLegalText', legalText })
    })
    setLoading(false)
    showSuccess('Texto legal actualizado correctamente.')
    router.refresh()
  }

  const handleAddCompany = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!newCompany.trim()) return
    setLoading(true)
    await fetch('/api/admin/settings', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'addCompany', name: newCompany.trim() })
    })
    setNewCompany('')
    setLoading(false)
    showSuccess(`Empresa "${newCompany}" añadida con éxito.`)
    router.refresh()
  }

  const confirmDeleteCompany = async () => {
    if (!companyToDelete) return
    setLoading(true)
    await fetch('/api/admin/settings', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'deleteCompany', id: companyToDelete.id })
    })
    setLoading(false)
    setCompanyToDelete(null)
    showSuccess('Empresa eliminada correctamente.')
    router.refresh()
  }

  return (
    <div className="bg-white shadow-lg sm:rounded-xl p-6 md:p-8 border border-gray-100">
      <h2 className="text-xl font-bold text-gray-900 mb-6">Configuración del Formulario</h2>
      
      {/* Toast Notification */}
      {successMessage && (
        <div className="mb-6 p-4 bg-green-50 border-l-4 border-green-500 rounded text-green-700 text-sm flex items-center transition-all animate-in fade-in slide-in-from-top-4">
          <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7"></path></svg>
          {successMessage}
        </div>
      )}

      <div className="mb-8">
        <label className="block text-sm font-medium text-gray-700 mb-2">Logo de la Empresa</label>
        <div className="flex items-center gap-4">
          <div className="w-32 h-32 border-2 border-dashed border-gray-300 rounded-lg flex items-center justify-center bg-gray-50 overflow-hidden relative">
            {logo ? (
              <>
                <img src={logo} alt="Logo" className="w-full h-full object-contain p-2" />
                <button 
                  onClick={async () => {
                    setLogo(null)
                    setLoading(true)
                    await fetch('/api/admin/settings', {
                      method: 'POST',
                      headers: { 'Content-Type': 'application/json' },
                      body: JSON.stringify({ action: 'updateLogo', logoBase64: null })
                    })
                    setLoading(false)
                    showSuccess('Logo eliminado.')
                    router.refresh()
                  }}
                  className="absolute top-1 right-1 bg-red-500 text-white p-1 rounded-full hover:bg-red-600"
                >
                  <X className="w-4 h-4" />
                </button>
              </>
            ) : (
              <div className="text-center p-4">
                <Upload className="w-6 h-6 text-gray-400 mx-auto mb-1" />
                <span className="text-xs text-gray-500">Subir Logo</span>
              </div>
            )}
          </div>
          <div className="flex-1">
            <input 
              type="file" 
              accept="image/*"
              className="text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded file:border-0 file:text-sm file:font-semibold file:bg-orange-50 file:text-orange-700 hover:file:bg-orange-100 cursor-pointer mb-2"
              onChange={(e) => {
                const file = e.target.files?.[0]
                if (file) {
                  const reader = new FileReader()
                  reader.onload = async (evt) => {
                    const base64 = evt.target?.result as string
                    setLogo(base64)
                    
                    setLoading(true)
                    await fetch('/api/admin/settings', {
                      method: 'POST',
                      headers: { 'Content-Type': 'application/json' },
                      body: JSON.stringify({ action: 'updateLogo', logoBase64: base64 })
                    })
                    setLoading(false)
                    showSuccess('Logo actualizado correctamente.')
                    router.refresh()
                  }
                  reader.readAsDataURL(file)
                }
              }}
            />
            <p className="text-xs text-gray-500">Formato: PNG o JPG. Tamaño ideal: 200x200px. Este logo aparecerá en el formulario de registro y en los PDF generados.</p>
          </div>
        </div>
      </div>

      <div className="mb-8">
        <label className="block text-sm font-medium text-gray-700 mb-2">Texto Legal del Consentimiento</label>
        <textarea
          value={legalText}
          onChange={(e) => setLegalText(e.target.value)}
          rows={5}
          className="w-full rounded-md border border-gray-300 p-3 text-gray-900 bg-white outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500"
        />
        <button 
          onClick={handleUpdateLegalText}
          disabled={loading}
          className="mt-2 bg-gray-900 text-white px-4 py-2 rounded hover:bg-gray-800 text-sm"
        >
          Guardar Texto
        </button>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Empresas (Opciones del Formulario)</label>
        
        <ul className="mb-4 border rounded-md divide-y">
          {initialCompanies.map(c => (
            <li key={c.id} className="p-3 flex justify-between items-center text-sm text-gray-700 hover:bg-gray-50">
              {c.name}
              <button onClick={() => setCompanyToDelete({ id: c.id, name: c.name })} className="text-red-500 hover:text-red-700 text-xs font-bold">
                ELIMINAR
              </button>
            </li>
          ))}
          {initialCompanies.length === 0 && <li className="p-3 text-sm text-gray-500">No hay empresas.</li>}
        </ul>

        <form onSubmit={handleAddCompany} className="flex gap-2">
          <input 
            type="text" 
            value={newCompany} 
            onChange={(e) => setNewCompany(e.target.value)}
            placeholder="Nueva empresa..." 
            className="flex-1 rounded-md border border-gray-300 p-2 text-sm text-gray-900 bg-white outline-none focus:border-orange-500 focus:ring-1 focus:ring-orange-500"
          />
          <button type="submit" disabled={loading} className="bg-orange-600 text-white px-4 py-2 rounded hover:bg-orange-700 text-sm">
            Añadir
          </button>
        </form>
      </div>

      {/* Delete Company Modal */}
      {companyToDelete && (
        <div className="fixed inset-0 bg-black/60 z-[100] flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6 text-center transform transition-all">
            <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-red-100 mb-6">
              <AlertTriangle className="h-8 w-8 text-red-600" />
            </div>
            <h3 className="text-xl font-bold text-gray-900 mb-2">Eliminar Empresa</h3>
            <p className="text-gray-600 mb-8 text-sm leading-relaxed">
              ¿Estás seguro de que deseas eliminar la empresa <strong className="text-gray-900">{companyToDelete.name}</strong>? Ya no aparecerá en el formulario para los nuevos voluntarios.
            </p>
            <div className="flex gap-3 justify-center w-full">
              <button 
                onClick={() => setCompanyToDelete(null)} 
                disabled={loading}
                className="flex-1 px-4 py-2.5 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 font-medium transition-colors"
              >
                Cancelar
              </button>
              <button 
                onClick={confirmDeleteCompany} 
                disabled={loading}
                className="flex-1 px-4 py-2.5 bg-red-600 text-white rounded-lg hover:bg-red-700 font-medium transition-colors flex justify-center items-center"
              >
                {loading ? 'Eliminando...' : 'Sí, eliminar'}
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="mt-8 pt-6 border-t">
        <h3 className="text-lg font-bold text-gray-800 mb-4">Compartir Formulario</h3>
        <p className="text-sm text-gray-600 mb-4">
          Los voluntarios pueden escanear este código QR con sus celulares para acceder directamente al formulario de consentimiento.
        </p>
        <div className="flex flex-col sm:flex-row items-center gap-6">
          <div className="bg-white p-4 border rounded-lg shadow-sm">
            {publicUrl ? (
              <QRCodeSVG value={publicUrl} size={150} />
            ) : (
              <div className="w-[150px] h-[150px] bg-gray-100 flex items-center justify-center text-xs text-gray-400">Cargando QR...</div>
            )}
          </div>
          <div>
            <p className="text-sm font-medium text-gray-700 mb-2">Enlace directo:</p>
            <a href={publicUrl} target="_blank" className="text-orange-600 hover:underline font-medium break-all">
              {publicUrl}
            </a>
          </div>
        </div>
      </div>
    </div>
  )
}
