import { PrismaClient } from '@prisma/client'
import PublicForm from './PublicForm'

const prisma = new PrismaClient()

export const dynamic = 'force-dynamic'

export default async function Home() {
  const settings = await prisma.settings.findUnique({ where: { id: 'default' } })
  const companies = await prisma.companyOption.findMany({ orderBy: { name: 'asc' } })

  const legalText = settings?.legalText || 'Cargando texto legal...'
  const companyOptions = companies.map(c => c.name)

  return <PublicForm legalText={legalText} companyOptions={companyOptions} logoBase64={settings?.logoBase64} />
}
