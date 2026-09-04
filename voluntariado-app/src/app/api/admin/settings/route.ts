import { NextResponse } from 'next/server'
import { PrismaClient } from '@prisma/client'
import { getIronSession } from 'iron-session'
import { sessionOptions, SessionData } from '@/lib/session'
import { cookies } from 'next/headers'

const prisma = new PrismaClient()

export async function POST(req: Request) {
  const session = await getIronSession<SessionData>(cookies(), sessionOptions)
  if (!session.isLoggedIn) return new NextResponse('Unauthorized', { status: 401 })

  try {
    const data = await req.json()
    
    if (data.action === 'updateLegalText') {
      await prisma.settings.update({
        where: { id: 'default' },
        data: { legalText: data.legalText }
      })
      await prisma.auditLog.create({
        data: { userEmail: session.email || 'unknown', action: 'UPDATE_SETTINGS', description: 'Actualizó el texto legal.' }
      })
      return NextResponse.json({ success: true })
    }

    if (data.action === 'updateLogo') {
      await prisma.settings.update({
        where: { id: 'default' },
        data: { logoBase64: data.logoBase64 }
      })
      await prisma.auditLog.create({
        data: { userEmail: session.email || 'unknown', action: 'UPDATE_LOGO', description: 'Actualizó el logo de la empresa.' }
      })
      return NextResponse.json({ success: true })
    }

    if (data.action === 'addCompany') {
      const company = await prisma.companyOption.create({
        data: { name: data.name }
      })
      await prisma.auditLog.create({
        data: { userEmail: session.email || 'unknown', action: 'ADD_COMPANY', description: `Añadió la empresa: ${data.name}` }
      })
      return NextResponse.json({ success: true, company })
    }

    if (data.action === 'deleteCompany') {
      const company = await prisma.companyOption.findUnique({ where: { id: data.id } })
      await prisma.companyOption.delete({
        where: { id: data.id }
      })
      await prisma.auditLog.create({
        data: { userEmail: session.email || 'unknown', action: 'DELETE_COMPANY', description: `Eliminó la empresa: ${company?.name || data.id}` }
      })
      return NextResponse.json({ success: true })
    }

    return new NextResponse('Invalid action', { status: 400 })
  } catch (error) {
    return new NextResponse('Internal Server Error', { status: 500 })
  }
}
