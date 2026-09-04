import { NextResponse } from 'next/server'
import { PrismaClient } from '@prisma/client'
import { z } from 'zod'

const prisma = new PrismaClient()

const consentSchema = z.object({
  fullName: z.string().min(2).regex(/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/),
  idType: z.enum(['CC', 'CE', 'PAS']),
  idNumber: z.string().min(4).regex(/^[a-zA-Z0-9]+$/),
  company: z.string().min(1),
  email: z.string().email(),
  phone: z.string().min(5).regex(/^[0-9+\-\s]+$/),
  activityName: z.string().min(2).regex(/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/),
  signature: z.string().startsWith('data:image/'),
})

export async function POST(req: Request) {
  try {
    const body = await req.json()
    
    // 1. Backend Validation
    const validation = consentSchema.safeParse(body)
    if (!validation.success) {
      return NextResponse.json({ success: false, error: 'Datos inválidos. Intente nuevamente.' }, { status: 400 })
    }
    const data = validation.data

    // 2. Prevent Duplicates
    const existing = await prisma.participant.findFirst({
      where: {
        idNumber: data.idNumber,
        activityName: data.activityName
      }
    })
    
    if (existing) {
      return NextResponse.json({ success: false, error: 'Ya existe un registro para esta persona en esta actividad.' }, { status: 400 })
    }

    // Generate a unique registration number (VOL-XXXXXX)
    const count = await prisma.participant.count()
    const registrationNumber = `VOL-${String(count + 1).padStart(6, '0')}`

    const participant = await prisma.participant.create({
      data: {
        registrationNumber,
        fullName: data.fullName,
        idType: data.idType,
        idNumber: data.idNumber,
        company: data.company,
        email: data.email,
        phone: data.phone,
        activityName: data.activityName,
        signature: data.signature,
        consentAccepted: true,
      }
    })

    return NextResponse.json({ success: true, registrationNumber: participant.registrationNumber })
  } catch (error) {
    console.error('Error in /api/consent:', error)
    return NextResponse.json({ success: false, error: 'Error interno del servidor' }, { status: 500 })
  }
}
