package com.example.data

import android.content.Context
import org.json.JSONObject

data class Answer(
    val label: String,
    val nextNodeId: String
)

data class TriageNode(
    val nodeId: String,
    val type: String, // "question" or "action"
    val text: String,
    val answers: List<Answer>
)

data class EmergencyScenario(
    val id: String,
    val title: String,
    val icon: String,
    val nodes: List<TriageNode>
)

class TriageRepository(private val context: Context) {

    fun loadTriageData(): List<EmergencyScenario> {
        return try {
            val jsonString = context.assets.open("triage.json").bufferedReader().use { it.readText() }
            val rootObject = JSONObject(jsonString)
            val jsonArray = rootObject.getJSONArray("emergencies")
            val list = mutableListOf<EmergencyScenario>()

            for (i in 0 until jsonArray.length()) {
                val emergencyObj = jsonArray.getJSONObject(i)
                val id = emergencyObj.getString("id")
                val title = emergencyObj.getString("title")
                val icon = emergencyObj.optString("icon", "🩹")

                val nodesArray = emergencyObj.getJSONArray("nodes")
                val nodesList = mutableListOf<TriageNode>()

                for (j in 0 until nodesArray.length()) {
                    val nodeObj = nodesArray.getJSONObject(j)
                    val nodeId = nodeObj.getString("node_id")
                    val type = nodeObj.getString("type")
                    val text = nodeObj.getString("text")

                    val answersArray = nodeObj.optJSONArray("answers")
                    val answersList = mutableListOf<Answer>()

                    if (answersArray != null) {
                        for (k in 0 until answersArray.length()) {
                            val answerObj = answersArray.getJSONObject(k)
                            val label = answerObj.getString("label")
                            val nextNodeId = answerObj.getString("next_node_id")
                            answersList.add(Answer(label, nextNodeId))
                        }
                    }

                    nodesList.add(TriageNode(nodeId, type, text, answersList))
                }

                list.add(EmergencyScenario(id, title, icon, nodesList))
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
