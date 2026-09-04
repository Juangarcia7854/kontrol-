import { PrismaClient } from '@prisma/client'

const prisma = new PrismaClient()

async function main() {
  // Seed Settings
  const settings = await prisma.settings.findUnique({ where: { id: 'default' } })
  if (!settings) {
    await prisma.settings.create({
      data: {
        id: 'default',
        legalText: 'Por medio de la presente, confirmo que la información suministrada es correcta y acepto mi participación voluntaria en la actividad descrita. Autorizo el tratamiento de mis datos personales conforme a la política de privacidad de la empresa.'
      }
    })
    console.log('Default settings created')
  }

  // Seed default companies
  const companies = ['INDUSTRIES & SERVICE', 'SOLTRECH']
  for (const name of companies) {
    await prisma.companyOption.upsert({
      where: { name },
      update: {},
      create: { name }
    })
  }
  console.log('Default companies created')
}

main()
  .catch((e) => {
    console.error(e)
    process.exit(1)
  })
  .finally(async () => {
    await prisma.$disconnect()
  })
