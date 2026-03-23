import { test, expect } from '@playwright/test';

const apiUrl = process.env.API_URL || process.env.BASE_URL || 'http://localhost:4000';

test.describe('Categories API', () => {
  // SC-001: Create category — positive path
  test('SC-001: should create a category with all required fields', async ({ request }) => {
    const response = await request.post(`${apiUrl}/api/categories`, {
      data: { name: 'Работа', color: '#FF5733' },
    });

    expect(response.status()).toBe(201);

    const body = await response.json();
    expect(typeof body.id).toBe('string');
    expect(body.id.length).toBeGreaterThan(0);
    expect(body.name).toBe('Работа');
    expect(body.color).toBe('#FF5733');
    expect(typeof body.createdAt).toBe('string');
    expect(body.createdAt.length).toBeGreaterThan(0);
  });

  // SC-002: Get list of categories
  test('SC-002: should return list of categories including created one', async ({ request }) => {
    const createResponse = await request.post(`${apiUrl}/api/categories`, {
      data: { name: 'Личное', color: '#3366FF' },
    });
    expect(createResponse.status()).toBe(201);

    const listResponse = await request.get(`${apiUrl}/api/categories`);
    expect(listResponse.status()).toBe(200);

    const categories = await listResponse.json();
    expect(Array.isArray(categories)).toBe(true);

    const found = categories.find((c: any) => c.name === 'Личное');
    expect(found).toBeTruthy();
    expect(found.id).toBeTruthy();
    expect(found.color).toBe('#3366FF');
    expect(found.createdAt).toBeTruthy();
  });

  // SC-003: Get category by ID
  test('SC-003: should return category by ID', async ({ request }) => {
    const createResponse = await request.post(`${apiUrl}/api/categories`, {
      data: { name: 'Учёба', color: '#00CC99' },
    });
    expect(createResponse.status()).toBe(201);
    const created = await createResponse.json();

    const getResponse = await request.get(`${apiUrl}/api/categories/${created.id}`);
    expect(getResponse.status()).toBe(200);

    const body = await getResponse.json();
    expect(body.id).toBe(created.id);
    expect(body.name).toBe('Учёба');
    expect(body.color).toBe('#00CC99');
    expect(body.createdAt).toBe(created.createdAt);
  });

  // SC-004: Update category
  test('SC-004: should update category name and color', async ({ request }) => {
    const createResponse = await request.post(`${apiUrl}/api/categories`, {
      data: { name: 'Старое имя', color: '#111111' },
    });
    expect(createResponse.status()).toBe(201);
    const created = await createResponse.json();

    const updateResponse = await request.put(`${apiUrl}/api/categories/${created.id}`, {
      data: { name: 'Новое имя', color: '#222222' },
    });
    expect(updateResponse.status()).toBe(200);

    const updated = await updateResponse.json();
    expect(updated.id).toBe(created.id);
    expect(updated.name).toBe('Новое имя');
    expect(updated.color).toBe('#222222');
  });

  // SC-005: Delete category
  test('SC-005: should delete category and return 404 on subsequent get', async ({ request }) => {
    const createResponse = await request.post(`${apiUrl}/api/categories`, {
      data: { name: 'Удаляемая', color: '#FF0000' },
    });
    expect(createResponse.status()).toBe(201);
    const created = await createResponse.json();

    const deleteResponse = await request.delete(`${apiUrl}/api/categories/${created.id}`);
    expect(deleteResponse.status()).toBe(204);

    const getResponse = await request.get(`${apiUrl}/api/categories/${created.id}`);
    expect(getResponse.status()).toBe(404);
  });

  // SC-006: Validation — invalid data
  test('SC-006: should reject creation with empty name', async ({ request }) => {
    const response = await request.post(`${apiUrl}/api/categories`, {
      data: { name: '', color: '#FF5733' },
    });
    expect(response.status()).toBe(400);
  });

  // SC-007: Unique category name
  test('SC-007: should reject duplicate category name', async ({ request }) => {
    const uniqueName = `Работа-${Date.now()}`;

    const first = await request.post(`${apiUrl}/api/categories`, {
      data: { name: uniqueName, color: '#FF5733' },
    });
    expect(first.status()).toBe(201);

    const second = await request.post(`${apiUrl}/api/categories`, {
      data: { name: uniqueName, color: '#00FF00' },
    });
    expect(second.status()).toBe(400);
  });

  // SC-008: Operations on non-existent category
  test('SC-008: should return 404 for non-existent category operations', async ({ request }) => {
    const fakeId = '00000000-0000-0000-0000-000000000000';

    const getResponse = await request.get(`${apiUrl}/api/categories/${fakeId}`);
    expect(getResponse.status()).toBe(404);

    const putResponse = await request.put(`${apiUrl}/api/categories/${fakeId}`, {
      data: { name: 'Тест', color: '#FF5733' },
    });
    expect(putResponse.status()).toBe(404);

    const deleteResponse = await request.delete(`${apiUrl}/api/categories/${fakeId}`);
    expect(deleteResponse.status()).toBe(404);
  });
});
