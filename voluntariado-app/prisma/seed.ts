import { PrismaClient } from '@prisma/client'
import bcrypt from 'bcryptjs'

const prisma = new PrismaClient()

async function main() {
  const email = 'admin@empresa.com'
  const password = '123456'
  
  // Check if admin already exists
  const existingAdmin = await prisma.adminUser.findUnique({
    where: { email }
  })

  if (!existingAdmin) {
    const passwordHash = await bcrypt.hash(password, 10)
    await prisma.adminUser.create({
      data: {
        email,
        passwordHash
      }
    })
    console.log(`Admin user created: ${email}`)
  } else {
    console.log(`Admin user already exists: ${email}`)
  }
}

main()
  .catch((e) => {
    console.error(e)
    process.exit(1)
  })
  .finally(async () => {
    await prisma.$disconnect()
  })
