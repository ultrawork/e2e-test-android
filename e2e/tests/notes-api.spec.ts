import { test, expect } from '@playwright/test';

const apiUrl = process.env.API_URL || process.env.BASE_URL || 'http://localhost:4001';

async function getDevToken(request: any): Promise<string> {
  const response = await request.post(`${apiUrl}/api/auth/dev-token`);
  expect(response.status()).toBe(200);
  const body = await response.json();
  expect(body.token).toBeTruthy();
  return body.token;
}

test.describe('Android Notes API (Retrofit integration)', () => {

  /**
   * SC-001: Get dev token for testing
   */
  test('SC-001: POST /api/auth/dev-token returns a valid JWT token', async ({ request }) => {
    const response = await request.post(`${apiUrl}/api/auth/dev-token`);
    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body.token).toBeTruthy();
    expect(typeof body.token).toBe('string');

    // JWT format: xxxxx.yyyyy.zzzzz
    const parts = body.token.split('.');
    expect(parts.length).toBe(3);
  });

  /**
   * SC-002: GET /api/notes without authorization → 401
   */
  test('SC-002: GET /api/notes without auth returns 401', async ({ request }) => {
    const response = await request.get(`${apiUrl}/api/notes`, {
      headers: {},
    });
    expect(response.status()).toBe(401);
  });

  /**
   * SC-003: GET /api/notes with valid token → list of notes
   */
  test('SC-003: GET /api/notes with valid token returns note list', async ({ request }) => {
    const token = await getDevToken(request);

    const response = await request.get(`${apiUrl}/api/notes`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(response.status()).toBe(200);

    const notes = await response.json();
    expect(Array.isArray(notes)).toBe(true);

    // If there are notes, verify structure
    if (notes.length > 0) {
      const note = notes[0];
      expect(typeof note.id).toBe('string');
      expect(note).toHaveProperty('title');
      expect(note).toHaveProperty('content');
      expect(note).toHaveProperty('userId');
      expect(note).toHaveProperty('createdAt');
      expect(note).toHaveProperty('updatedAt');
    }
  });

  /**
   * SC-004: POST /api/notes creates a note (content = title)
   */
  test('SC-004: POST /api/notes creates a note successfully', async ({ request }) => {
    const token = await getDevToken(request);
    const headers = { Authorization: `Bearer ${token}` };

    // Create note
    const createResponse = await request.post(`${apiUrl}/api/notes`, {
      headers,
      data: { title: 'Test Note E2E', content: 'Test Note E2E' },
    });
    expect(createResponse.status()).toBe(201);

    const created = await createResponse.json();
    expect(typeof created.id).toBe('string');
    expect(created.title).toBe('Test Note E2E');
    expect(created.content).toBe('Test Note E2E');
    expect(created).toHaveProperty('userId');

    // Verify note appears in list
    const listResponse = await request.get(`${apiUrl}/api/notes`, { headers });
    expect(listResponse.status()).toBe(200);

    const notes = await listResponse.json();
    const found = notes.find((n: any) => n.title === 'Test Note E2E');
    expect(found).toBeTruthy();
  });

  /**
   * SC-005: DELETE /api/notes/:id deletes a note → 204
   */
  test('SC-005: DELETE /api/notes/:id deletes a note', async ({ request }) => {
    const token = await getDevToken(request);
    const headers = { Authorization: `Bearer ${token}` };

    // Create a note to delete
    const createResponse = await request.post(`${apiUrl}/api/notes`, {
      headers,
      data: { title: 'To Delete E2E', content: 'To Delete E2E' },
    });
    expect(createResponse.status()).toBe(201);
    const created = await createResponse.json();
    const noteId = created.id;

    // Delete it
    const deleteResponse = await request.delete(`${apiUrl}/api/notes/${noteId}`, { headers });
    expect(deleteResponse.status()).toBe(204);

    // Verify note is gone
    const listResponse = await request.get(`${apiUrl}/api/notes`, { headers });
    const notes = await listResponse.json();
    const found = notes.find((n: any) => n.id === noteId);
    expect(found).toBeFalsy();
  });

  /**
   * SC-006: POST /api/notes without required fields → 400
   */
  test('SC-006: POST /api/notes without required fields returns 400', async ({ request }) => {
    const token = await getDevToken(request);

    const response = await request.post(`${apiUrl}/api/notes`, {
      headers: { Authorization: `Bearer ${token}` },
      data: {},
    });
    expect(response.status()).toBe(400);
  });

  /**
   * SC-007: DELETE /api/notes/:id for non-existent note → 404
   */
  test('SC-007: DELETE non-existent note returns 404', async ({ request }) => {
    const token = await getDevToken(request);

    const response = await request.delete(`${apiUrl}/api/notes/nonexistent-id-99999`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(response.status()).toBe(404);
  });
});
