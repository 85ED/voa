import { expect, test, type APIRequestContext } from '@playwright/test'

const adminEmail = 'admin@jlturismo.local'
const adminPassword = 'AdminSenhaSegura12'
let partnerEmail = 'e2e.partner@jlturismo.local'

let partnerPassword = ''

async function csrfHeaders(api: APIRequestContext): Promise<Record<string, string>> {
  const state = await api.storageState()
  const xsrf = state.cookies.find((c) => c.name === 'XSRF-TOKEN')?.value
  return xsrf ? { 'X-XSRF-TOKEN': xsrf } : {}
}

test.describe.serial('VOA Docker smoke', () => {
  let api: APIRequestContext

  test.beforeAll(async ({ playwright, baseURL }) => {
    api = await playwright.request.newContext({ baseURL })
    const login = await api.post('/api/v1/auth/login', {
      data: { email: adminEmail, password: adminPassword },
    })
    expect(login.ok()).toBeTruthy()

    const partners = await api.get('/api/v1/parceiros', { params: { size: 50 } })
    expect(partners.ok()).toBeTruthy()
    let partner = (await partners.json()).content.find(
      (item: { slug: string }) => item.slug === 'e2e-smoke',
    )
    if (!partner) {
      const created = await api.post('/api/v1/parceiros', {
        headers: await csrfHeaders(api),
        data: {
          nome: 'Parceiro E2E',
          slug: 'e2e-smoke',
          nomeComercial: 'Parceiro E2E',
          subtitulo: 'Canal de testes automatizados',
          ativo: true,
        },
      })
      expect(created.ok()).toBeTruthy()
      partner = await created.json()
    }

    partnerEmail = `e2e.partner+${Date.now()}@jlturismo.local`
    const access = await api.post(`/api/v1/parceiros/${partner.id}/acesso`, {
      headers: await csrfHeaders(api),
      data: { email: partnerEmail, nome: 'Usuário E2E' },
    })
    expect(access.ok()).toBeTruthy()
    partnerPassword = (await access.json()).senhaTemporaria
  })

  test.afterAll(async () => {
    await api.dispose()
  })

  test('health e landing sem 502', async ({ page, request }) => {
    const health = await request.get('/actuator/health')
    expect(health.ok()).toBeTruthy()
    expect((await health.json()).status).toBe('UP')

    const response = await page.goto('/')
    expect(response?.status()).toBe(200)
    await expect(page.getByRole('heading', { name: /sua viagem começa aqui/i })).toBeVisible()
  })

  test('login da agência', async ({ page }) => {
    await page.goto('/login')
    await page.getByLabel('E-mail').fill(adminEmail)
    await page.getByLabel('Senha').fill(adminPassword)
    await page.getByRole('button', { name: 'Entrar' }).click()
    await expect(page).toHaveURL(/\/dashboard$/)
  })

  test('login do parceiro', async ({ page }) => {
    test.skip(!partnerPassword, 'Acesso de parceiro não foi criado nesta execução')
    await page.goto('/login')
    await page.getByLabel('E-mail').fill(partnerEmail)
    await page.getByLabel('Senha').fill(partnerPassword)
    await page.getByRole('button', { name: 'Entrar' }).click()
    await expect(page).toHaveURL(/\/parceiro$/)
    await expect(page.getByRole('heading', { name: 'Minhas solicitações' })).toBeVisible()
  })

  test('smoke parceiro e POST de solicitação em /p/slug', async ({ page }) => {
    await page.goto('/p/e2e-smoke')
    await expect(page.getByRole('heading', { name: 'Parceiro E2E' })).toBeVisible()
    await page.getByLabel('Origem').fill('São Paulo')
    await page.getByLabel('Destino').fill('Recife')
    await page.getByLabel('Data ida').fill('2030-01-15')
    await page.getByLabel('Nome').fill('Cliente E2E')
    await page.getByLabel('Telefone / WhatsApp').fill('11999999999')
    await page.getByLabel('E-mail (opcional)').fill('cliente.e2e@jlturismo.local')
    await page.getByLabel(/Autorizo o tratamento/).check()
    await page.getByRole('button', { name: 'Enviar solicitação' }).click()
    await expect(page.getByText(/Solicitação recebida/)).toBeVisible()
  })
})
