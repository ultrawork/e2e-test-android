import { test, expect } from '@playwright/test';

/**
 * Helper: adds a note via the UI form.
 */
async function addNote(page: import('@playwright/test').Page, text: string) {
  await page.getByLabel('New note').fill(text);
  await page.getByRole('button', { name: 'Add' }).click();
  await expect(page.getByText(text)).toBeVisible();
}

test.describe('Notes Favorites & Filter', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/notes');
    await expect(page.getByRole('heading', { name: 'Notes' })).toBeVisible();
  });

  /**
   * SC-1: Notes list displays favorite buttons on each note card.
   */
  test('SC-1: each note card has a favorite button with unfilled star', async ({ page }) => {
    await addNote(page, 'Shopping List');
    await addNote(page, 'Meeting Notes');
    await addNote(page, 'Ideas');

    // Verify all 3 notes are visible
    await expect(page.getByText('Всего заметок: 3')).toBeVisible();

    // Verify each note has a favorite button with unfilled star
    for (const noteText of ['Shopping List', 'Meeting Notes', 'Ideas']) {
      const noteItem = page.locator('li', { hasText: noteText });
      await expect(noteItem).toBeVisible();
      const favBtn = noteItem.getByRole('button', { name: 'Toggle favorite' });
      await expect(favBtn).toBeVisible();
      await expect(favBtn).toHaveText('☆');
    }

    // Verify the favorites filter button exists
    await expect(page.getByRole('button', { name: 'Только избранные' })).toBeVisible();
  });

  /**
   * SC-2: Toggling favorite changes the star icon.
   */
  test('SC-2: toggle favorite changes star from unfilled to filled and back', async ({ page }) => {
    await addNote(page, 'Test Note');

    const noteItem = page.locator('li', { hasText: 'Test Note' });
    const favBtn = noteItem.getByRole('button', { name: 'Toggle favorite' });

    // Initially unfilled star
    await expect(favBtn).toHaveText('☆');

    // Click to favorite — should become filled star
    await favBtn.click();
    await expect(favBtn).toHaveText('★');

    // Click again to unfavorite — should return to unfilled star
    await favBtn.click();
    await expect(favBtn).toHaveText('☆');
  });

  /**
   * SC-3: Favorites filter shows only favorited notes, toggling off restores all.
   */
  test('SC-3: favorites filter shows only favorited notes', async ({ page }) => {
    await addNote(page, 'Note A');
    await addNote(page, 'Note B');
    await addNote(page, 'Note C');
    await addNote(page, 'Note D');
    await addNote(page, 'Note E');

    await expect(page.getByText('Всего заметок: 5')).toBeVisible();

    // Favorite notes A and C
    await page.locator('li', { hasText: 'Note A' }).getByRole('button', { name: 'Toggle favorite' }).click();
    await expect(page.locator('li', { hasText: 'Note A' }).getByRole('button', { name: 'Toggle favorite' })).toHaveText('★');

    await page.locator('li', { hasText: 'Note C' }).getByRole('button', { name: 'Toggle favorite' }).click();
    await expect(page.locator('li', { hasText: 'Note C' }).getByRole('button', { name: 'Toggle favorite' })).toHaveText('★');

    // Activate favorites filter
    await page.getByRole('button', { name: 'Только избранные' }).click();

    // Only A and C should be visible
    await expect(page.locator('li', { hasText: 'Note A' })).toBeVisible();
    await expect(page.locator('li', { hasText: 'Note C' })).toBeVisible();
    await expect(page.locator('li', { hasText: 'Note B' })).not.toBeVisible();
    await expect(page.locator('li', { hasText: 'Note D' })).not.toBeVisible();
    await expect(page.locator('li', { hasText: 'Note E' })).not.toBeVisible();

    // Counter should show filtered count
    await expect(page.getByText('Найдено: 2 из 5')).toBeVisible();

    // Deactivate favorites filter — all 5 should reappear
    await page.getByRole('button', { name: 'Только избранные' }).click();

    await expect(page.locator('li', { hasText: 'Note A' })).toBeVisible();
    await expect(page.locator('li', { hasText: 'Note B' })).toBeVisible();
    await expect(page.locator('li', { hasText: 'Note C' })).toBeVisible();
    await expect(page.locator('li', { hasText: 'Note D' })).toBeVisible();
    await expect(page.locator('li', { hasText: 'Note E' })).toBeVisible();
    await expect(page.getByText('Всего заметок: 5')).toBeVisible();
  });

  /**
   * SC-4: Empty state when favorites filter is active but no notes are favorited.
   */
  test('SC-4: favorites filter with no favorited notes shows empty list', async ({ page }) => {
    await addNote(page, 'Unfavorited Note');

    await expect(page.getByText('Всего заметок: 1')).toBeVisible();

    // Activate favorites filter with no favorited notes
    await page.getByRole('button', { name: 'Только избранные' }).click();

    // The unfavorited note should disappear
    await expect(page.locator('li', { hasText: 'Unfavorited Note' })).not.toBeVisible();

    // Counter should show 0 filtered
    await expect(page.getByText('Найдено: 0 из 1')).toBeVisible();
  });
});
