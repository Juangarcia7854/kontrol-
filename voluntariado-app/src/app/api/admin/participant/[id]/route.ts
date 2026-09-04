import { NextResponse } from 'next/server'
import { PrismaClient } from '@prisma/client'
import { getIronSession } from 'iron-session'
import { sessionOptions, SessionData } from '@/lib/session'
import { cookies } from 'next/headers'

const prisma = new PrismaClient()

export async function DELETE(req: Request, { params }: { params: { id: string } }) {
  const session = await getIronSession<SessionData>(cookies(), sessionOptions)
  if (!session.isLoggedIn) return new NextResponse('Unauthorized', { status: 401 })

  try {
    const participant = await prisma.participant.findUnique({ where: { id: params.id } })
    
    if (participant) {
      await prisma.participant.delete({
        where: { id: params.id }
      })

      // Log the deletion
      await prisma.auditLog.create({
        data: {
          userEmail: session.email || 'unknown',
          action: 'DELETE_PARTICIPANT',
          description: `Eliminó el registro ${participant.registrationNumber} de ${participant.fullName}`
        }
      })
    }

    return NextResponse.json({ success: true })
  } catch (error) {
    console.error('Error deleting participant:', error)
    return new NextResponse('Internal Server Error', { status: 500 })
  }
}
