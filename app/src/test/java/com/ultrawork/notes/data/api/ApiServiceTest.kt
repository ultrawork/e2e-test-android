package com.ultrawork.notes.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

class ApiServiceTest {

    private val methods = ApiService::class.java.declaredMethods

    @Test
    fun `interface has getNotes method with GET notes annotation`() {
        val method = methods.find { it.name == "getNotes" }
        assertNotNull("getNotes method should exist", method)

        val annotation = method!!.getAnnotation(GET::class.java)
        assertNotNull("getNotes should have @GET annotation", annotation)
        assertEquals("notes", annotation!!.value)
    }

    @Test
    fun `interface has getNote method with GET notes id annotation`() {
        val method = methods.find { it.name == "getNote" }
        assertNotNull("getNote method should exist", method)

        val annotation = method!!.getAnnotation(GET::class.java)
        assertNotNull("getNote should have @GET annotation", annotation)
        assertEquals("notes/{id}", annotation!!.value)
    }

    @Test
    fun `getNote has Path id parameter`() {
        val method = methods.find { it.name == "getNote" }
        assertNotNull(method)

        val pathAnnotation = method!!.parameterAnnotations.flatten()
            .filterIsInstance<Path>()
            .firstOrNull()
        assertNotNull("getNote should have @Path parameter", pathAnnotation)
        assertEquals("id", pathAnnotation!!.value)
    }

    @Test
    fun `interface has createNote method with POST notes annotation`() {
        val method = methods.find { it.name == "createNote" }
        assertNotNull("createNote method should exist", method)

        val annotation = method!!.getAnnotation(POST::class.java)
        assertNotNull("createNote should have @POST annotation", annotation)
        assertEquals("notes", annotation!!.value)
    }

    @Test
    fun `createNote has Body parameter`() {
        val method = methods.find { it.name == "createNote" }
        assertNotNull(method)

        val bodyAnnotation = method!!.parameterAnnotations.flatten()
            .filterIsInstance<Body>()
            .firstOrNull()
        assertNotNull("createNote should have @Body parameter", bodyAnnotation)
    }

    @Test
    fun `interface has updateNote method with PUT notes id annotation`() {
        val method = methods.find { it.name == "updateNote" }
        assertNotNull("updateNote method should exist", method)

        val annotation = method!!.getAnnotation(PUT::class.java)
        assertNotNull("updateNote should have @PUT annotation", annotation)
        assertEquals("notes/{id}", annotation!!.value)
    }

    @Test
    fun `updateNote has Path and Body parameters`() {
        val method = methods.find { it.name == "updateNote" }
        assertNotNull(method)

        val annotations = method!!.parameterAnnotations.flatten()
        assertTrue("updateNote should have @Path parameter",
            annotations.any { it is Path && it.value == "id" })
        assertTrue("updateNote should have @Body parameter",
            annotations.any { it is Body })
    }

    @Test
    fun `interface has deleteNote method with DELETE notes id annotation`() {
        val method = methods.find { it.name == "deleteNote" }
        assertNotNull("deleteNote method should exist", method)

        val annotation = method!!.getAnnotation(DELETE::class.java)
        assertNotNull("deleteNote should have @DELETE annotation", annotation)
        assertEquals("notes/{id}", annotation!!.value)
    }

    @Test
    fun `interface has getCategories method with GET categories annotation`() {
        val method = methods.find { it.name == "getCategories" }
        assertNotNull("getCategories method should exist", method)

        val annotation = method!!.getAnnotation(GET::class.java)
        assertNotNull("getCategories should have @GET annotation", annotation)
        assertEquals("categories", annotation!!.value)
    }

    @Test
    fun `interface has exactly 6 API methods`() {
        val apiMethods = methods.filter { method ->
            method.getAnnotation(GET::class.java) != null ||
                method.getAnnotation(POST::class.java) != null ||
                method.getAnnotation(PUT::class.java) != null ||
                method.getAnnotation(DELETE::class.java) != null
        }

        assertEquals("ApiService should have exactly 6 annotated methods", 6, apiMethods.size)
    }
}
