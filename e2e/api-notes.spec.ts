import { test, expect } from '@playwright/test';

const apiBase = (process.env.API_URL || process.env.BASE_URL || 'http://localhost:4000') + '/api';

// SC-4: Get notes list
test('SC-4: GET /api/notes returns 200 with array of notes', async ({ request }) => {
  const response = await request.get(`${apiBase}/notes`);
  expect(response.status()).toBe(200);

  const body = await response.json();
  expect(Array.isArray(body)).toBe(true);

  if (body.length > 0) {
    const note = body[0];
    expect(note).toHaveProperty('id');
    expect(note).toHaveProperty('title');
    expect(note).toHaveProperty('content');
    expect(note).toHaveProperty('createdAt');
    expect(note).toHaveProperty('updatedAt');
  }
});

// SC-5: Create note with valid data
test('SC-5: POST /api/notes creates a note and it appears in GET', async ({ request }) => {
  const createResponse = await request.post(`${apiBase}/notes`, {
    data: {
      title: 'Test Note',
      content: 'Test content for E2E',
    },
  });
  expect(createResponse.status()).toBe(201);

  const created = await createResponse.json();
  expect(created).toHaveProperty('id');
  expect(created.title).toBe('Test Note');
  expect(created.content).toBe('Test content for E2E');
  expect(created).toHaveProperty('createdAt');
  expect(created).toHaveProperty('updatedAt');

  // Verify it appears in the list
  const listResponse = await request.get(`${apiBase}/notes`);
  expect(listResponse.status()).toBe(200);
  const notes = await listResponse.json();
  const found = notes.find((n: { id: string }) => n.id === created.id);
  expect(found).toBeTruthy();
  expect(found.title).toBe('Test Note');
});

// SC-6: Create note without required fields
test('SC-6: POST /api/notes with empty body returns 400', async ({ request }) => {
  const response = await request.post(`${apiBase}/notes`, {
    data: {},
  });
  expect(response.status()).toBe(400);

  const body = await response.json();
  expect(body.error).toBe('title and content are required');
});

// SC-7: Delete an existing note
test('SC-7: DELETE /api/notes/:id removes the note', async ({ request }) => {
  // First create a note to delete
  const createResponse = await request.post(`${apiBase}/notes`, {
    data: {
      title: 'Note to delete',
      content: 'This note will be deleted',
    },
  });
  expect(createResponse.status()).toBe(201);
  const created = await createResponse.json();
  const noteId = created.id;

  // Delete it
  const deleteResponse = await request.delete(`${apiBase}/notes/${noteId}`);
  expect(deleteResponse.status()).toBe(204);

  // Verify it's gone
  const listResponse = await request.get(`${apiBase}/notes`);
  const notes = await listResponse.json();
  const found = notes.find((n: { id: string }) => n.id === noteId);
  expect(found).toBeFalsy();
});

// SC-8: Delete non-existent note
test('SC-8: DELETE /api/notes/:id with invalid id returns 404', async ({ request }) => {
  const response = await request.delete(`${apiBase}/notes/non-existent-id-12345`);
  expect(response.status()).toBe(404);

  const body = await response.json();
  expect(body.error).toBe('Note not found');
});
