import { NextResponse } from 'next/server'
import { PrismaClient } from '@prisma/client'
import { getIronSession } from 'iron-session'
import { sessionOptions, SessionData } from '@/lib/session'
import { cookies } from 'next/headers'
import Papa from 'papaparse'

const prisma = new PrismaClient()

export async function GET() {
  const session = await getIronSession<SessionData>(cookies(), sessionOptions)
  
  if (!session.isLoggedIn) {
    return new NextResponse('Unauthorized', { status: 401 })
  }

  try {
    const participants = await prisma.participant.findMany({
      orderBy: { createdAt: 'desc' }
    })

    const data = participants.map(p => ({
      Registro: p.registrationNumber,
      Fecha: p.createdAt.toLocaleString(),
      Nombre: p.fullName,
      'Tipo Doc': p.idType,
      'Documento': p.idNumber,
      Empresa: p.company,
      Email: p.email,
      Teléfono: p.phone,
      Actividad: p.activityName,
      Aceptó: p.consentAccepted ? 'Sí' : 'No'
    }))

    const csv = Papa.unparse(data)

    // Log the export action
    await prisma.auditLog.create({
      data: {
        userEmail: session.email || 'unknown',
        action: 'EXPORT_CSV',
        description: `Exportó ${participants.length} registros a CSV.`
      }
    })

    return new NextResponse(csv, {
      headers: {
        'Content-Type': 'text/csv',
        'Content-Disposition': `attachment; filename="voluntariado_export_${new Date().toISOString().split('T')[0]}.csv"`
      }
    })
  } catch (error) {
    return new NextResponse('Internal Server Error', { status: 500 })
  }
}
