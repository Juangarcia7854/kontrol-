import { getIronSession } from 'iron-session'
import { sessionOptions, SessionData } from '@/lib/session'
import { cookies } from 'next/headers'
import { redirect } from 'next/navigation'
import { PrismaClient } from '@prisma/client'
import LogoutButton from './LogoutButton'
import Link from 'next/link'
import AdminSettings from './AdminSettings'
import { FileText, Building2, Calendar, Download, LayoutDashboard } from 'lucide-react'
import DeleteButton from './DeleteButton'

const prisma = new PrismaClient()

export default async function Dashboard() {
  const session = await getIronSession<SessionData>(cookies(), sessionOptions)

  if (!session.isLoggedIn) {
    redirect('/admin/login')
  }

  const participants = await prisma.participant.findMany({
    orderBy: { createdAt: 'desc' }
  })

  const settings = await prisma.settings.findUnique({ where: { id: 'default' } })
  const companies = await prisma.companyOption.findMany({ orderBy: { name: 'asc' } })

  return (
    <div className="min-h-screen bg-gray-50 pb-12">
      <nav className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <LayoutDashboard className="h-6 w-6 text-orange-600 mr-2" />
              <span className="text-xl font-bold text-gray-900">Panel de Administración</span>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-gray-500">{session.email}</span>
              <LogoutButton />
            </div>
          </div>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="mb-4 flex justify-between items-center">
            <h2 className="text-2xl font-bold text-gray-800">Registros de Voluntariado</h2>
            <a href="/api/export" className="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 text-sm font-medium">
              Exportar CSV
            </a>
          </div>

          <div className="bg-white shadow-lg overflow-hidden sm:rounded-xl border border-gray-100">
            <ul className="divide-y divide-gray-100">
              {participants.length === 0 ? (
                <li className="px-6 py-12 flex flex-col items-center justify-center text-gray-500 text-center">
                  <div className="bg-gray-50 p-4 rounded-full mb-3">
                    <FileText className="w-8 h-8 text-gray-400" />
                  </div>
                  <p className="text-lg font-medium text-gray-900">No hay registros aún</p>
                  <p className="text-sm">Los formularios completados aparecerán aquí.</p>
                </li>
              ) : (
                participants.map((p) => (
                  <li key={p.id} className="hover:bg-orange-50/30 transition-colors">
                    <div className="px-6 py-5 flex items-center justify-between">
                      <div className="flex items-center space-x-4">
                        <div className="flex-shrink-0">
                          <div className="h-10 w-10 rounded-full bg-orange-100 flex items-center justify-center text-orange-600 font-bold">
                            {p.fullName.charAt(0).toUpperCase()}
                          </div>
                        </div>
                        <div>
                          <p className="text-sm font-bold text-gray-900">{p.fullName}</p>
                          <div className="flex items-center space-x-2 mt-1">
                            <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800">
                              {p.registrationNumber}
                            </span>
                            <span className="text-xs text-gray-500 flex items-center">
                              <Building2 className="w-3 h-3 mr-1" />
                              {p.company}
                            </span>
                          </div>
                        </div>
                      </div>
                      <div className="flex items-center space-x-4">
                        <div className="text-right hidden md:block">
                          <p className="text-sm text-gray-900 font-medium">{p.activityName}</p>
                          <p className="text-xs text-gray-500 flex items-center justify-end mt-1">
                            <Calendar className="w-3 h-3 mr-1" />
                            {p.createdAt.toLocaleDateString()}
                          </p>
                        </div>
                        <Link 
                          href={`/api/pdf/${p.id}`} 
                          target="_blank" 
                          className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500 transition-colors"
                        >
                          <Download className="w-4 h-4 mr-2 text-gray-400" />
                          PDF
                        </Link>
                        <DeleteButton id={p.id} name={p.fullName} />
                      </div>
                    </div>
                  </li>
                ))
              )}
            </ul>
          </div>

          {/* Seccion de Configuracion */}
          <AdminSettings 
            initialLegalText={settings?.legalText || ''} 
            initialCompanies={companies} 
            initialLogo={settings?.logoBase64 || null}
          />
        </div>
      </main>
    </div>
  )
}
