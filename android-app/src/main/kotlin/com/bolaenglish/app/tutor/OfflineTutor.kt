package com.bolaenglish.app.tutor

import com.bolaenglish.app.data.Bridge
import com.bolaenglish.app.data.NativeLanguage
import com.bolaenglish.app.data.ProficiencyLevel

enum class Scenario(val title: String, val opening: String, val followUp: String) {
    DAILY("Daily Chat", "What did you do this morning?", "What will you do next?"),
    INTERVIEW("Job Interview", "What is one skill you could bring to this role?", "Can you give an example from your last project?"),
    TRAVEL("Travel & Directions", "How would you ask someone for directions?", "How would you ask how long the trip takes?"),
    OFFICE("Office Standup", "What did you work on yesterday?", "What help do you need to finish your next task?")
}

data class Feedback(val reply: String, val suggestion: String, val tip: String, val question: String)
data class Turn(val sentence: String, val feedback: Feedback, val live: Boolean)
private data class Rule(val pattern: Regex, val replacement: String, val explanation: Bridge)
private fun phrase(text: String) = Regex("\\b$text\\b", RegexOption.IGNORE_CASE)

object OfflineTutor {
    private val rules = listOf(
        Rule(phrase("I am agree"), "I agree", Bridge("Agree is a verb. Say ‘I agree’ on its own.",
            "Agree खुद एक क्रिया है। सीधे I agree कहें।", "Agree हे क्रियापद आहे. थेट I agree म्हणा.")),
        Rule(phrase("yesterday I go"), "Yesterday I went", Bridge("Use the simple past for a finished time.",
            "बीते हुए समय के लिए साधा भूतकाल इस्तेमाल करें।", "संपलेल्या वेळेसाठी साधा भूतकाळ वापरा.")),
        Rule(phrase("didn't went"), "didn't go", Bridge("After didn’t, use the base verb go.",
            "Didn’t के बाद क्रिया का मूल रूप go रखें।", "Didn’t नंतर मूळ क्रियापद go वापरा.")),
        Rule(Regex("^(he|she) go\\b", RegexOption.IGNORE_CASE), "$1 goes", Bridge("In the simple present, he and she take goes.",
            "सामान्य वर्तमान काल में he या she के साथ goes कहें।", "साध्या वर्तमानकाळात he किंवा she सोबत goes म्हणा.")),
        Rule(phrase("can able to"), "can", Bridge("Can already expresses ability. Follow it with a base verb.",
            "Can क्षमता बताता है। इसके बाद सीधे मूल क्रिया रखें।", "Can मधून क्षमता कळते. त्यानंतर मूळ क्रियापद वापरा.")),
        Rule(phrase("discuss about"), "discuss", Bridge("Put the topic directly after discuss.",
            "Discuss के तुरंत बाद विषय रखें।", "Discuss नंतर थेट विषय सांगा.")),
        Rule(phrase("where is the station is"), "where the station is", Bridge("Use subject before is inside an indirect question.",
            "अप्रत्यक्ष सवाल में the station के बाद is रखें।", "अप्रत्यक्ष प्रश्नात the station नंतर is वापरा."))
    )
    fun reply(input: String, scenario: Scenario, language: NativeLanguage, level: ProficiencyLevel, previous: Int): Feedback {
        val sentence = input.trim().replace('’', '\'')
        val rule = rules.firstOrNull { it.pattern.containsMatchIn(sentence) && it.pattern.replaceFirst(sentence, it.replacement) != sentence }
        val wording = (rule?.pattern?.replaceFirst(sentence, rule.replacement) ?: sentence).replaceFirstChar { it.uppercase() }
        val tip = rule?.explanation ?: when (level) {
            ProficiencyLevel.BEGINNER -> Bridge("Try saying who did what in one short sentence.", "एक छोटे वाक्य में बताएं कि किसने क्या किया।", "कोणी काय केले ते एका छोट्या वाक्यात सांगा.")
            ProficiencyLevel.ELEMENTARY -> Bridge("Add a time or place so the listener can picture it.", "समय या जगह जोड़कर बात स्पष्ट करें।", "वेळ किंवा ठिकाण सांगून वाक्य अधिक स्पष्ट करा.")
            ProficiencyLevel.INTERMEDIATE -> Bridge("Connect your point to a reason or a specific example.", "अपनी बात के साथ एक कारण या उदाहरण दें।", "तुमच्या मुद्द्याला कारण किंवा उदाहरण जोडा.")
        }
        val question = when {
            scenario == Scenario.DAILY && phrase("breakfast").containsMatchIn(sentence) -> "What do you usually have for breakfast?"
            scenario == Scenario.TRAVEL && phrase("station").containsMatchIn(sentence) -> "What would you ask at the ticket counter?"
            previous % 2 == 1 -> when (scenario) {
                Scenario.DAILY -> "Who usually joins you?"
                Scenario.INTERVIEW -> "What did you learn from that experience?"
                Scenario.TRAVEL -> "How would you check that you understood the directions?"
                Scenario.OFFICE -> "When do you expect to finish?"
            }
            else -> scenario.followUp
        }
        return Feedback(if (rule == null) "Let’s build on that." else "Try this small change.", wording, tip.forLanguage(language), question)
    }
}
