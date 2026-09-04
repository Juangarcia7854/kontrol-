'use client'

import { useState, useRef } from 'react'
import SignatureCanvas from 'react-signature-canvas'
import { useRouter } from 'next/navigation'

export default function PublicForm({ legalText, companyOptions, logoBase64 }: { legalText: string, companyOptions: string[], logoBase64?: string | null }) {
  const router = useRouter()
  const sigCanvas = useRef<SignatureCanvas>(null)
  
  const [formData, setFormData] = useState({
    fullName: '',
    idType: 'CC',
    idNumber: '',
    company: '',
    email: '',
    phone: '',
    activityName: '',
  })
  const [consent1, setConsent1] = useState(false)
  const [consent2, setConsent2] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    let { name, value } = e.target

    // Validaciones en tiempo real para evitar caracteres inválidos
    if (name === 'fullName' || name === 'activityName') {
      // Solo letras (incluyendo acentos y ñ) y espacios
      value = value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '')
    } else if (name === 'idNumber') {
      if (formData.idType === 'PAS') {
        // Pasaporte permite letras y números
        value = value.replace(/[^a-zA-Z0-9]/g, '')
      } else {
        // Cédulas solo permiten números
        value = value.replace(/[^0-9]/g, '')
      }
    } else if (name === 'phone') {
      // Teléfono permite números, espacios, guiones y signo +
      value = value.replace(/[^0-9+\-\s]/g, '')
    }

    if (name === 'idType') {
      setFormData({ ...formData, [name]: value, idNumber: '' })
    } else {
      setFormData({ ...formData, [name]: value })
    }
  }

  const clearSignature = () => {
    sigCanvas.current?.clear()
  }

  const [showConfirm, setShowConfirm] = useState(false)

  const handleInitialSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    
    if (!consent1 || !consent2) {
      setError('Debes aceptar las dos casillas de consentimiento para continuar.')
      return
    }

    if (sigCanvas.current?.isEmpty()) {
      setError('Por favor, proporciona tu firma en el recuadro correspondiente.')
      return
    }
    
    setShowConfirm(true)
  }

  const handleFinalSubmit = async () => {
    setShowConfirm(false)
    setLoading(true)
    const signatureBase64 = sigCanvas.current?.getCanvas().toDataURL('image/png')

    try {
      const res = await fetch('/api/consent', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...formData, signature: signatureBase64 })
      })

      const result = await res.json()
      
      if (res.ok) {
        router.push(`/success?id=${result.registrationNumber}`)
      } else {
        setError(result.error || 'Ocurrió un error al enviar el formulario')
      }
    } catch (err) {
      setError('Error de conexión. Inténtalo más tarde.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 text-gray-800 flex items-center justify-center p-4 sm:p-6 relative">
      {/* Confirmation Modal */}
      {showConfirm && (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-sm w-full p-6 text-center">
            <h3 className="text-xl font-bold text-gray-900 mb-2">Confirmación de Envío</h3>
            <p className="text-gray-600 mb-6">
              ¿Está seguro de que desea enviar el consentimiento?
              <br /><br />
              Una vez enviado, la información quedará registrada y no podrá ser modificada.
            </p>
            <div className="flex gap-4 justify-center">
              <button 
                onClick={() => setShowConfirm(false)} 
                className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 font-medium"
              >
                Cancelar
              </button>
              <button 
                onClick={handleFinalSubmit} 
                className="px-4 py-2 bg-orange-600 text-white rounded-md hover:bg-orange-700 font-medium"
              >
                Confirmar Envío
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="bg-white rounded-xl shadow-lg w-full max-w-2xl overflow-hidden border border-gray-100">
        
        {/* Header */}
        <div className="bg-orange-600 px-6 py-8 text-center">
          <div className="mb-4 flex justify-center">
            <div className="w-32 h-32 bg-white rounded-lg flex items-center justify-center shadow-md text-gray-400 font-bold overflow-hidden">
              {logoBase64 ? (
                <img src={logoBase64} alt="Logo de Empresa" className="w-full h-full object-contain p-2" />
              ) : (
                'LOGO EMPRESA'
              )}
            </div>
          </div>
          <h1 className="text-2xl font-bold text-white uppercase tracking-wider">Consentimiento de Voluntariado</h1>
        </div>

        <form onSubmit={handleInitialSubmit} className="p-6 sm:p-8 space-y-6">
          {error && (
            <div className="bg-red-50 text-red-700 p-4 rounded-md border border-red-200">
              {error}
            </div>
          )}

          {/* Datos Personales */}
          <div>
            <h2 className="text-lg font-semibold text-gray-900 border-b pb-2 mb-4">Datos del Participante</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="col-span-1 md:col-span-2">
                <label className="block text-sm font-medium text-gray-700 mb-1">Nombre Completo *</label>
                <input required type="text" name="fullName" value={formData.fullName} onChange={handleChange} className="w-full rounded-md border-gray-300 border p-2 text-gray-900 bg-white focus:ring-orange-500 focus:border-orange-500 outline-none" />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Tipo de Documento *</label>
                <select name="idType" value={formData.idType} onChange={handleChange} className="w-full rounded-md border-gray-300 border p-2 text-gray-900 bg-white focus:ring-orange-500 focus:border-orange-500 outline-none">
                  <option value="CC">Cédula de Ciudadanía</option>
                  <option value="CE">Cédula de Extranjería</option>
                  <option value="PAS">Pasaporte</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Número de Documento *</label>
                <input required type="text" name="idNumber" value={formData.idNumber} onChange={handleChange} className="w-full rounded-md border-gray-300 border p-2 text-gray-900 bg-white focus:ring-orange-500 focus:border-orange-500 outline-none" />
              </div>

              <div className="col-span-1 md:col-span-2">
                <label className="block text-sm font-medium text-gray-700 mb-1">Empresa a la que pertenece *</label>
                <select required name="company" value={formData.company} onChange={handleChange} className="w-full rounded-md border-gray-300 border p-2 text-gray-900 bg-white focus:ring-orange-500 focus:border-orange-500 outline-none">
                  <option value="">Seleccione una empresa...</option>
                  {companyOptions.map((opt, i) => (
                    <option key={i} value={opt}>{opt}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Correo Electrónico *</label>
                <input required type="email" name="email" value={formData.email} onChange={handleChange} className="w-full rounded-md border-gray-300 border p-2 text-gray-900 bg-white focus:ring-orange-500 focus:border-orange-500 outline-none" />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Teléfono / Celular *</label>
                <input required type="tel" name="phone" value={formData.phone} onChange={handleChange} className="w-full rounded-md border-gray-300 border p-2 text-gray-900 bg-white focus:ring-orange-500 focus:border-orange-500 outline-none" />
              </div>
            </div>
          </div>

          {/* Datos de la Actividad */}
          <div>
            <h2 className="text-lg font-semibold text-gray-900 border-b pb-2 mb-4">Datos de la Actividad</h2>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Nombre de la Actividad de Voluntariado *</label>
              <input required type="text" name="activityName" value={formData.activityName} onChange={handleChange} className="w-full rounded-md border-gray-300 border p-2 text-gray-900 bg-white focus:ring-orange-500 focus:border-orange-500 outline-none" />
            </div>
          </div>

          {/* Declaración de Consentimiento */}
          <div>
            <h2 className="text-lg font-semibold text-gray-900 border-b pb-2 mb-4">Declaración de Consentimiento</h2>
            
            <div className="bg-gray-100 p-4 rounded-md text-sm text-gray-700 mb-4 h-32 overflow-y-auto whitespace-pre-wrap">
              {legalText}
            </div>

            <div className="space-y-3">
              <label className="flex items-start space-x-3 cursor-pointer">
                <input type="checkbox" checked={consent1} onChange={(e) => setConsent1(e.target.checked)} className="mt-1 h-4 w-4 text-orange-600 focus:ring-orange-500 border-gray-300 rounded" />
                <span className="text-sm text-gray-600">Acepto los términos y condiciones estipulados en la declaración de consentimiento.</span>
              </label>
              
              <label className="flex items-start space-x-3 cursor-pointer">
                <input type="checkbox" checked={consent2} onChange={(e) => setConsent2(e.target.checked)} className="mt-1 h-4 w-4 text-orange-600 focus:ring-orange-500 border-gray-300 rounded" />
                <span className="text-sm text-gray-600">Autorizo el tratamiento de mis datos personales de acuerdo con la política de privacidad.</span>
              </label>
            </div>
          </div>

          {/* Firma */}
          <div>
            <h2 className="text-lg font-semibold text-gray-900 border-b pb-2 mb-4">Firma del Participante</h2>
            <p className="text-sm text-gray-500 mb-2">Por favor firme en el recuadro a continuación usando su mouse o dedo:</p>
            <div className="border-2 border-dashed border-gray-300 rounded-md bg-gray-50 flex justify-center">
              <SignatureCanvas 
                ref={sigCanvas} 
                penColor="black"
                canvasProps={{width: 500, height: 200, className: 'w-full max-w-full touch-none'}}
              />
            </div>
            <div className="flex justify-end mt-2">
              <button type="button" onClick={clearSignature} className="text-sm text-gray-500 hover:text-gray-700 underline">
                Limpiar Firma
              </button>
            </div>
          </div>

          <div className="pt-4">
            <button 
              type="submit" 
              disabled={loading}
              className="w-full flex justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-lg font-medium text-white bg-orange-600 hover:bg-orange-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500 disabled:opacity-50"
            >
              {loading ? 'Enviando...' : 'ENVIAR CONSENTIMIENTO'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
