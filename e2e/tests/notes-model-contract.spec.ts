import { test, expect } from '@playwright/test';

const ISO_8601_REGEX = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}.\d{3}Z$/;
const UUID_REGEX = /^[0-9a-f-]{36}$/i;

function getApiUrl(): string {
  return process.env.API_URL || process.env.BASE_URL || 'http://localhost:4000';
}

test.describe('Note and Category API contract', () => {

  test('SC-001: Create note — response matches Note model structure', async ({ request }) => {
    const apiUrl = getApiUrl();
    const response = await request.post(`${apiUrl}/api/notes`, {
      data: { title: 'Test Note', content: 'Test content' },
    });

    expect(response.status()).toBe(201);

    const note = await response.json();
    expect(typeof note.id).toBe('string');
    expect(note.id).toMatch(UUID_REGEX);
    expect(note.title).toBe('Test Note');
    expect(note.content).toBe('Test content');
    expect(typeof note.createdAt).toBe('string');
    expect(note.createdAt).toMatch(ISO_8601_REGEX);
    expect(typeof note.updatedAt).toBe('string');
    expect(note.updatedAt).toMatch(ISO_8601_REGEX);
    expect(Array.isArray(note.categories)).toBe(true);
    expect(note.categories).toHaveLength(0);
  });

  test('SC-002: Create note with category — categories contains Category objects', async ({ request }) => {
    const apiUrl = getApiUrl();

    // Create a category first
    const catResponse = await request.post(`${apiUrl}/api/categories`, {
      data: { name: 'Work', color: '#FF5733' },
    });
    expect(catResponse.status()).toBe(201);
    const category = await catResponse.json();
    const categoryId = category.id;

    // Create a note with the category
    const noteResponse = await request.post(`${apiUrl}/api/notes`, {
      data: { title: 'Task', content: 'Do work', categoryIds: [categoryId] },
    });
    expect(noteResponse.status()).toBe(201);

    const note = await noteResponse.json();
    expect(typeof note.id).toBe('string');
    expect(typeof note.title).toBe('string');
    expect(typeof note.content).toBe('string');
    expect(typeof note.createdAt).toBe('string');
    expect(note.createdAt).toMatch(ISO_8601_REGEX);
    expect(typeof note.updatedAt).toBe('string');
    expect(note.updatedAt).toMatch(ISO_8601_REGEX);

    expect(Array.isArray(note.categories)).toBe(true);
    expect(note.categories).toHaveLength(1);
    const cat = note.categories[0];
    expect(typeof cat.id).toBe('string');
    expect(cat.name).toBe('Work');
    expect(cat.color).toBe('#FF5733');
  });

  test('SC-003: List notes — all notes contain categories field', async ({ request }) => {
    const apiUrl = getApiUrl();

    // Create a category
    const catResponse = await request.post(`${apiUrl}/api/categories`, {
      data: { name: 'Personal', color: '#3366FF' },
    });
    expect(catResponse.status()).toBe(201);
    const category = await catResponse.json();

    // Create note without category
    const noteAResponse = await request.post(`${apiUrl}/api/notes`, {
      data: { title: 'Note A', content: 'No categories' },
    });
    expect(noteAResponse.status()).toBe(201);

    // Create note with category
    const noteBResponse = await request.post(`${apiUrl}/api/notes`, {
      data: { title: 'Note B', content: 'With category', categoryIds: [category.id] },
    });
    expect(noteBResponse.status()).toBe(201);

    // Get all notes
    const listResponse = await request.get(`${apiUrl}/api/notes`);
    expect(listResponse.status()).toBe(200);

    const notes = await listResponse.json();
    expect(Array.isArray(notes)).toBe(true);
    expect(notes.length).toBeGreaterThanOrEqual(2);

    for (const note of notes) {
      expect(typeof note.id).toBe('string');
      expect(typeof note.title).toBe('string');
      expect(typeof note.content).toBe('string');
      expect(typeof note.createdAt).toBe('string');
      expect(note.createdAt).toMatch(ISO_8601_REGEX);
      expect(typeof note.updatedAt).toBe('string');
      expect(note.updatedAt).toMatch(ISO_8601_REGEX);
      expect(Array.isArray(note.categories)).toBe(true);
    }

    // Verify note without categories
    const noteA = notes.find((n: any) => n.title === 'Note A');
    expect(noteA).toBeDefined();
    expect(noteA.categories).toHaveLength(0);

    // Verify note with category
    const noteB = notes.find((n: any) => n.title === 'Note B');
    expect(noteB).toBeDefined();
    expect(noteB.categories.length).toBeGreaterThanOrEqual(1);
    const cat = noteB.categories.find((c: any) => c.name === 'Personal');
    expect(cat).toBeDefined();
    expect(typeof cat.id).toBe('string');
    expect(cat.name).toBe('Personal');
    expect(cat.color).toBe('#3366FF');
  });

  test('SC-004: Category API structure matches Category model', async ({ request }) => {
    const apiUrl = getApiUrl();

    // Create a category
    const createResponse = await request.post(`${apiUrl}/api/categories`, {
      data: { name: 'Urgent', color: '#FF0000' },
    });
    expect(createResponse.status()).toBe(201);

    const created = await createResponse.json();
    expect(typeof created.id).toBe('string');
    expect(created.id).toMatch(UUID_REGEX);
    expect(created.name).toBe('Urgent');
    expect(created.color).toBe('#FF0000');

    // List categories
    const listResponse = await request.get(`${apiUrl}/api/categories`);
    expect(listResponse.status()).toBe(200);

    const categories = await listResponse.json();
    expect(Array.isArray(categories)).toBe(true);
    expect(categories.length).toBeGreaterThanOrEqual(1);

    const urgent = categories.find((c: any) => c.name === 'Urgent');
    expect(urgent).toBeDefined();
    expect(typeof urgent.id).toBe('string');
    expect(typeof urgent.name).toBe('string');
    expect(typeof urgent.color).toBe('string');
    expect(urgent.color).toMatch(/^#[0-9A-Fa-f]{6}$/);
  });

  test('SC-005: Create note with non-existent category — returns 400', async ({ request }) => {
    const apiUrl = getApiUrl();

    const response = await request.post(`${apiUrl}/api/notes`, {
      data: { title: 'Bad Note', content: 'Invalid category', categoryIds: ['non-existent-id'] },
    });

    expect(response.status()).toBe(400);

    const body = await response.json();
    expect(body.error).toBe('One or more categories not found');
  });

});
