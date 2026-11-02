import { expect, test } from '@playwright/test'

test('landing usa tema claro (OpenCode-inspired)', async ({ page }) => {
  await page.goto('/')
  const bodyBg = await page.evaluate(() => {
    return window.getComputedStyle(document.body).backgroundColor
  })
  // rgb claro — evita fundo escuro tipo slate (#0f172a ≈ rgb(15, 23, 42))
  const match = bodyBg.match(/rgb\((\d+),\s*(\d+),\s*(\d+)\)/)
  expect(match).toBeTruthy()
  const r = Number(match![1])
  const g = Number(match![2])
  const b = Number(match![3])
  expect(r + g + b).toBeGreaterThan(400)
})
