import { NextResponse } from 'next/server'
import { PrismaClient } from '@prisma/client'
import { renderToStream } from '@react-pdf/renderer'
import React from 'react'
import { Document, Page, Text, View, StyleSheet, Image } from '@react-pdf/renderer'

const prisma = new PrismaClient()

const styles = StyleSheet.create({
  page: { padding: 40, fontFamily: 'Helvetica' },
  header: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 20, borderBottom: '1px solid #ccc', paddingBottom: 10, alignItems: 'center' },
  logo: { width: 80, height: 80, objectFit: 'contain' },
  logoPlaceholder: { width: 80, height: 80, backgroundColor: '#f3f4f6', justifyContent: 'center', alignItems: 'center' },
  headerText: { flex: 1, marginLeft: 20, textAlign: 'right' },
  title: { fontSize: 16, fontWeight: 'bold', color: '#ea580c' },
  subtitle: { fontSize: 12, color: '#4b5563', marginTop: 4 },
  section: { margin: 10, padding: 10 },
  row: { flexDirection: 'row', marginBottom: 5 },
  label: { fontSize: 10, fontWeight: 'bold', width: 120 },
  value: { fontSize: 10 },
  legalText: { fontSize: 9, textAlign: 'justify', marginTop: 20, marginBottom: 20, lineHeight: 1.5, color: '#374151' },
  signatureBox: { marginTop: 30, alignItems: 'center' },
  signatureImage: { width: 150, height: 60, marginBottom: 5 },
  signatureLine: { borderTop: '1px solid black', width: 200, marginTop: 5 },
  signatureText: { fontSize: 10, marginTop: 5 },
  footer: { position: 'absolute', bottom: 30, left: 40, right: 40, fontSize: 8, textAlign: 'center', color: '#9ca3af' }
})

const ConsentPDF = ({ participant, legalText, settings }: { participant: any, legalText: string, settings: any }) => (
  <Document>
    <Page size="A4" style={styles.page}>
      <View style={styles.header}>
        {settings?.logoBase64 ? (
          <Image src={settings.logoBase64} style={styles.logo} />
        ) : (
          <View style={styles.logoPlaceholder}>
            <Text style={{ fontSize: 10, color: '#9ca3af' }}>LOGO</Text>
          </View>
        )}
        <View style={styles.headerText}>
          <Text style={styles.title}>CONSENTIMIENTO DE VOLUNTARIADO</Text>
          <Text style={styles.subtitle}>{participant.company}</Text>
          <Text style={{ fontSize: 10, marginTop: 4 }}>Fecha: {participant.createdAt.toLocaleDateString()}</Text>
        </View>
      </View>

      <View style={styles.section}>
        <Text style={{ fontSize: 12, fontWeight: 'bold', marginBottom: 10, backgroundColor: '#f3f4f6', padding: 5 }}>DATOS DEL PARTICIPANTE</Text>
        <View style={styles.row}><Text style={styles.label}>Nombre Completo:</Text><Text style={styles.value}>{participant.fullName}</Text></View>
        <View style={styles.row}><Text style={styles.label}>Identificación:</Text><Text style={styles.value}>{participant.idType} {participant.idNumber}</Text></View>
        <View style={styles.row}><Text style={styles.label}>Email:</Text><Text style={styles.value}>{participant.email}</Text></View>
        <View style={styles.row}><Text style={styles.label}>Teléfono:</Text><Text style={styles.value}>{participant.phone}</Text></View>
      </View>

      <View style={styles.section}>
        <Text style={{ fontSize: 12, fontWeight: 'bold', marginBottom: 10, backgroundColor: '#f3f4f6', padding: 5 }}>DATOS DE LA ACTIVIDAD</Text>
        <View style={styles.row}><Text style={styles.label}>Actividad:</Text><Text style={styles.value}>{participant.activityName}</Text></View>
      </View>

      <View style={styles.section}>
        <Text style={{ fontSize: 12, fontWeight: 'bold', marginBottom: 10, backgroundColor: '#f3f4f6', padding: 5 }}>DECLARACIÓN DE CONSENTIMIENTO</Text>
        <Text style={styles.legalText}>
          {legalText}
        </Text>
      </View>

      <View style={styles.signatureBox}>
        {participant.signature && (
          <Image src={participant.signature} style={styles.signatureImage} />
        )}
        <View style={styles.signatureLine} />
        <Text style={styles.signatureText}>FIRMA DEL PARTICIPANTE</Text>
      </View>

      <Text style={styles.footer}>
        Documento generado automáticamente por el Sistema de Gestión de Voluntariado.
      </Text>
    </Page>
  </Document>
)

export async function GET(req: Request, { params }: { params: { id: string } }) {
  try {
    const participant = await prisma.participant.findUnique({
      where: { id: params.id }
    })

    if (!participant) {
      return new NextResponse('Not Found', { status: 404 })
    }

    const settings = await prisma.settings.findUnique({ where: { id: 'default' } })
    const legalText = settings?.legalText || 'Texto legal no disponible'

    const stream = await renderToStream(<ConsentPDF participant={participant} legalText={legalText} settings={settings} />)
    
    return new NextResponse(stream as unknown as ReadableStream, {
      headers: {
        'Content-Type': 'application/pdf',
        'Content-Disposition': `inline; filename="consentimiento_${participant.registrationNumber}.pdf"`
      }
    })
  } catch (error) {
    console.error('PDF Generation Error:', error)
    return new NextResponse('Internal Server Error', { status: 500 })
  }
}
