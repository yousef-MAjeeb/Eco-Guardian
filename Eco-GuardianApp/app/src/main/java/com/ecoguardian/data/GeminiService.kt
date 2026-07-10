package com.ecoguardian.data

import android.util.Base64
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class GeminiService(private val client: HttpClient) {

    suspend fun generateReport(imageBytes: ByteArray, apiKey: String): String {
        return try {
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val requestBody = """
                {
                  "contents": [
                    {
                      "parts": [
                        {
                          "text": "no name included also never include ** its not markdown its text, put date: on top and leave it empty for the user to fill, You are an environmental inspector. Analyze this photo and write a report covering: 1) Type of waste visible, 2) Estimated quantity, 3) Severity level (Low/Medium/High). make sure its short and simple yet professional."
                        },
                        {
                          "inline_data": {
                            "mime_type": "image/jpeg",
                            "data": "$base64Image"
                          }
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()

            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            val responseBody = response.bodyAsText()
            println("Gemini Response: $responseBody")
            
            if (response.status.value == 404) {
                val listModelsUrl = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
                val listResponse = client.get(listModelsUrl)
                println("Available Models: ${listResponse.bodyAsText()}")
            }

            val json = Json.parseToJsonElement(responseBody)
            
            val text = json.jsonObject["candidates"]
                ?.jsonArray?.getOrNull(0)
                ?.jsonObject?.get("content")
                ?.jsonObject?.get("parts")
                ?.jsonArray?.getOrNull(0)
                ?.jsonObject?.get("text")
                ?.jsonPrimitive?.content

            text ?: "Could not parse AI response. Status: ${response.status}. Body: $responseBody"
        } catch (e: Exception) {
            e.printStackTrace()
            "Error generating report: ${e.localizedMessage}"
        }
    }
}
